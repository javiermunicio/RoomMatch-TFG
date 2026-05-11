package com.example.roommatch_pmdm.presentation.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roommatch_pmdm.data.repositories.AuthRepository
import com.example.roommatch_pmdm.data.repositories.ChatRepository
import com.example.roommatch_pmdm.data.repositories.MatchRepository
import com.example.roommatch_pmdm.domain.model.ChatMessage
import com.example.roommatch_pmdm.domain.model.ChatUser
import com.example.roommatch_pmdm.notifications.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// Número máximo de conversaciones que combinamos en tiempo real.
// Por encima de este umbral se muestra igualmente la lista pero sin
// reactividad ultra-fina (se refresca solo al volver a la pantalla).
// ---------------------------------------------------------------------------
private const val MAX_REALTIME_CHATS = 30

class ChatListViewModel(
    private val matchRepository: MatchRepository,
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _chatUsers = MutableStateFlow<List<ChatUser>>(emptyList())
    val chatUsers: StateFlow<List<ChatUser>> = _chatUsers

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // ── Trigger de refresco ─────────────────────────────────────────────────
    // Incrementar este valor cancela el flow anterior y arranca uno nuevo,
    // evitando coroutines duplicadas sin necesidad de gestionar Job a mano.
    private val _refreshTick = MutableStateFlow(0)

    // ── Job único para la pipeline de chats ────────────────────────────────
    private var chatsJob: Job? = null

    init {
        startObserving()
    }

    // Cancela el job activo y arranca uno nuevo limpio.
    // Llamar a refresh() es seguro desde cualquier punto del ciclo de vida.
    fun refresh() {
        _refreshTick.value++          // dispara flatMapLatest → cancela el flow anterior
    }

    private fun startObserving() {
        // Cancelamos cualquier job previo antes de crear uno nuevo
        chatsJob?.cancel()
        chatsJob = viewModelScope.launch {
            val currentUserId = authRepository.currentUser?.uid ?: return@launch

            // flatMapLatest sobre _refreshTick: cada vez que refresh() incrementa
            // el tick, el bloque interno se cancela y se re-ejecuta desde cero.
            _refreshTick
                .flatMapLatest { _ ->
                    flow {
                        emit(emptyList<ChatUser>())          // limpia UI mientras carga
                        _isLoading.value = true

                        // 1. Obtener todos los IDs con los que hay conversación
                        val matchedIds = matchRepository.getMatchedUserIds(currentUserId).toSet()
                        val activeIds  = chatRepository.getActiveConversationUserIds(currentUserId).toSet()
                        val allUserIds = (matchedIds + activeIds).toList()

                        if (allUserIds.isEmpty()) {
                            _isLoading.value = false
                            emit(emptyList())
                            return@flow
                        }

                        // 2. Separamos en dos grupos:
                        //    · realtimeIds  → combinamos flows en tiempo real (≤ MAX_REALTIME_CHATS)
                        //    · staticIds    → carga one-shot (escalan sin problema)
                        val realtimeIds = allUserIds.take(MAX_REALTIME_CHATS)
                        val staticIds   = allUserIds.drop(MAX_REALTIME_CHATS)

                        // 3. Construimos los flows reactivos solo para realtimeIds
                        val realtimeFlows = realtimeIds.map { userId ->
                            combine(
                                chatRepository.getLastMessageFlow(currentUserId, userId),
                                chatRepository.getUnreadCountFlow(currentUserId, userId)
                            ) { lastMsg, _ ->
                                buildChatUser(currentUserId, userId, lastMsg)
                            }
                        }

                        // 4. Flow para los IDs estáticos (one-shot, sin listener Firestore)
                        val staticFlow = flowOf(
                            staticIds.mapNotNull { userId ->
                                val lastMsg = chatRepository.getLastMessage(currentUserId, userId)
                                buildChatUser(currentUserId, userId, lastMsg)
                            }
                        )

                        // 5. Combinar ambos grupos y emitir la lista ordenada
                        val combinedFlow = if (realtimeFlows.isEmpty()) {
                            staticFlow
                        } else {
                            combine(
                                combine(realtimeFlows) { it.filterNotNull().toList() },
                                staticFlow
                            ) { realtimeList, staticList ->
                                (realtimeList + staticList)
                                    .sortedByDescending { it.timestamp }
                            }
                        }

                        combinedFlow.collect { users ->
                            _isLoading.value = false
                            emit(users)
                        }
                    }
                }
                .collect { users ->
                    _chatUsers.value = users
                }
        }
    }

    // Construye un ChatUser a partir de los datos del usuario y el último mensaje.
    // Retorna null si el usuario no existe en Firestore (conversación huérfana).
    private suspend fun buildChatUser(
        currentUserId: String,
        otherUserId:   String,
        lastMsg:       ChatMessage?
    ): ChatUser? {
        val user = chatRepository.getUserData(otherUserId) ?: return null
        return ChatUser(
            id                  = otherUserId,
            username            = user.username.ifEmpty { user.email },
            profileImage        = user.profileImage,
            lastMessage         = lastMsg?.content ?: "Toca para chatear",
            timestamp           = lastMsg?.timestamp ?: 0L,
            // isRead refleja el estado REAL del mensaje en Firestore.
            // · Si yo soy el receptor  → isRead indica si lo leí yo.
            // · Si yo soy el emisor    → isRead indica si lo leyó el otro.
            // En ambos casos usamos el valor del documento sin alterar.
            isRead              = lastMsg?.isRead ?: true,
            lastMessageSenderId = lastMsg?.senderId ?: ""
        )
    }

    override fun onCleared() {
        super.onCleared()
        chatsJob?.cancel()
    }
}

// ---------------------------------------------------------------------------
// ChatDetailViewModel — sin cambios funcionales respecto al original,
// incluido aquí para mantener el archivo unificado.
// ---------------------------------------------------------------------------

class ChatDetailViewModel(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val context: Context
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _messageInput = MutableStateFlow("")
    val messageInput: StateFlow<String> = _messageInput

    private val _currentUserIdFlow = MutableStateFlow(
        authRepository.currentUser?.uid ?: ""
    )
    val currentUserIdFlow: StateFlow<String> = _currentUserIdFlow

    private val _otherUser = MutableStateFlow<ChatUser?>(null)
    val otherUser: StateFlow<ChatUser?> = _otherUser

    private var messagesJob: Job? = null

    fun onMessageInputChanged(text: String) {
        _messageInput.value = text
    }

    fun loadMessages(otherUserId: String) {
        val uid = authRepository.currentUser?.uid ?: return
        _currentUserIdFlow.value = uid

        viewModelScope.launch {
            val user = chatRepository.getUserData(otherUserId)
            _otherUser.value = ChatUser(
                id           = otherUserId,
                username     = user?.username?.ifEmpty { user.email } ?: otherUserId,
                profileImage = user?.profileImage ?: ""
            )
        }

        // Cancelamos el job anterior antes de crear uno nuevo
        messagesJob?.cancel()
        _messages.value = emptyList()

        messagesJob = viewModelScope.launch {
            chatRepository.getMessages(uid, otherUserId).collect { msgs ->
                val sorted = msgs.sortedBy { it.timestamp }

                val previousIds = _messages.value.map { it.id }.toSet()
                val newIncoming = sorted.filter { it.id !in previousIds && it.senderId != uid }

                if (_messages.value.isNotEmpty() && newIncoming.isNotEmpty()) {
                    val canNotify = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ContextCompat.checkSelfPermission(
                            context, Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    } else true

                    if (canNotify) {
                        newIncoming.forEach { msg ->
                            NotificationHelper.showChatNotification(
                                context    = context,
                                senderName = _otherUser.value?.username ?: "Nuevo mensaje",
                                message    = msg.content
                            )
                        }
                    }
                }

                _messages.value = sorted
            }
        }
    }

    fun sendMessage(otherUserId: String) {
        val uid     = authRepository.currentUser?.uid ?: return
        val content = _messageInput.value.trim()
        if (content.isEmpty()) return
        _messageInput.value = ""
        viewModelScope.launch {
            try {
                chatRepository.sendMessage(uid, otherUserId, content)
            } catch (e: Exception) {
                _messageInput.value = content
                e.printStackTrace()
            }
        }
    }

    fun markMessagesAsRead(otherUserId: String) {
        val currentUid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            chatRepository.markMessagesAsRead(currentUid, otherUserId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        messagesJob?.cancel()
    }
}