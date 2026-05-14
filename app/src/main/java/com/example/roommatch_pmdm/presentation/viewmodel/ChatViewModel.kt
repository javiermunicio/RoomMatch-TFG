package com.example.roommatch_pmdm.presentation.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roommatch_pmdm.data.repositories.AuthRepository
import com.example.roommatch_pmdm.data.repositories.BlockRepository
import com.example.roommatch_pmdm.data.repositories.ChatRepository
import com.example.roommatch_pmdm.data.repositories.MatchRepository
import com.example.roommatch_pmdm.domain.model.ChatMessage
import com.example.roommatch_pmdm.domain.model.ChatUser
import com.example.roommatch_pmdm.domain.usecase.BlockUserUseCase
import com.example.roommatch_pmdm.domain.usecase.DeleteConversationUseCase
import com.example.roommatch_pmdm.domain.usecase.SendMessageUseCase
import com.example.roommatch_pmdm.notifications.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

private const val MAX_REALTIME_CHATS = 30

class ChatListViewModel(
    private val matchRepository: MatchRepository,
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository,
    private val blockRepository: BlockRepository,
) : ViewModel() {

    private val _chatUsers = MutableStateFlow<List<ChatUser>>(emptyList())
    val chatUsers: StateFlow<List<ChatUser>> = _chatUsers

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _refreshTick = MutableStateFlow(0)
    private var chatsJob: Job? = null

    init { startObserving() }

    fun refresh() { _refreshTick.value++ }

    private fun startObserving() {
        chatsJob?.cancel()
        chatsJob = viewModelScope.launch {
            val currentUserId = authRepository.currentUser?.uid ?: return@launch
            _refreshTick
                .flatMapLatest { _ ->
                    flow {
                        emit(emptyList<ChatUser>())
                        _isLoading.value = true

                        val matchedIds = matchRepository.getMatchedUserIds(currentUserId).toSet()
                        val activeIds  = chatRepository.getActiveConversationUserIds(currentUserId).toSet()
                        val allUserIds = (matchedIds + activeIds).toList()

                        if (allUserIds.isEmpty()) {
                            _isLoading.value = false
                            emit(emptyList())
                            return@flow
                        }

                        val blockedByMe   = blockRepository.getBlockedUserIds(currentUserId).toSet()
                        val blockedByThem = blockRepository.getUsersWhoBlockedMe(currentUserId).toSet()
                        val allBlocked    = blockedByMe + blockedByThem

                        val filteredUserIds = allUserIds.filter { it !in allBlocked }

                        val realtimeIds = filteredUserIds.take(MAX_REALTIME_CHATS)
                        val staticIds   = filteredUserIds.drop(MAX_REALTIME_CHATS)

                        val realtimeFlows = realtimeIds.map { userId ->
                            combine(
                                chatRepository.getLastMessageFlow(currentUserId, userId),
                                chatRepository.getUnreadCountFlow(currentUserId, userId)
                            ) { lastMsg, _ ->
                                buildChatUser(currentUserId, userId, lastMsg)
                            }
                        }

                        val staticFlow = flowOf(
                            staticIds.mapNotNull { userId ->
                                val lastMsg = chatRepository.getLastMessage(currentUserId, userId)
                                buildChatUser(currentUserId, userId, lastMsg)
                            }
                        )

                        val combinedFlow = if (realtimeFlows.isEmpty()) {
                            staticFlow
                        } else {
                            combine(
                                combine(realtimeFlows) { it.filterNotNull().toList() },
                                staticFlow
                            ) { realtimeList, staticList ->
                                (realtimeList + staticList).sortedByDescending { it.timestamp }
                            }
                        }

                        combinedFlow.collect { users ->
                            _isLoading.value = false
                            emit(users)
                        }
                    }
                }
                .collect { users -> _chatUsers.value = users }
        }
    }

    private suspend fun buildChatUser(
        currentUserId: String,
        otherUserId: String,
        lastMsg: ChatMessage?
    ): ChatUser? {
        val user = chatRepository.getUserData(otherUserId) ?: return null
        return ChatUser(
            id                  = otherUserId,
            username            = user.username.ifEmpty { user.email },
            profileImage        = user.profileImage,
            lastMessage         = lastMsg?.content ?: "Toca para chatear",
            timestamp           = lastMsg?.timestamp ?: 0L,
            isRead              = lastMsg?.isRead ?: true,
            lastMessageSenderId = lastMsg?.senderId ?: ""
        )
    }

    override fun onCleared() {
        super.onCleared()
        chatsJob?.cancel()
    }
}

class ChatDetailViewModel(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val blockRepository: BlockRepository,
    private val sendMessageUseCase: SendMessageUseCase,
    private val deleteConversationUseCase: DeleteConversationUseCase,
    private val blockUserUseCase: BlockUserUseCase,
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

    private val _isBlocked = MutableStateFlow(false)
    val isBlocked: StateFlow<Boolean> = _isBlocked

    private val _isBlockedByOther = MutableStateFlow(false)
    val isBlockedByOther: StateFlow<Boolean> = _isBlockedByOther

    private val _actionDone = MutableStateFlow<String?>(null)
    val actionDone: StateFlow<String?> = _actionDone

    // Flag para evitar notificaciones mientras el chat está abierto
    private val _isChatActive = MutableStateFlow(false)

    private var messagesJob: Job? = null

    fun onMessageInputChanged(text: String) { _messageInput.value = text }

    fun onChatOpened() { _isChatActive.value = true }
    fun onChatClosed() { _isChatActive.value = false }

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
            _isBlocked.value        = blockRepository.isBlocked(uid, otherUserId)
            _isBlockedByOther.value = blockRepository.isBlockedByOther(uid, otherUserId)
        }

        messagesJob?.cancel()
        _messages.value = emptyList()

        messagesJob = viewModelScope.launch {
            chatRepository.getMessages(uid, otherUserId).collect { msgs ->
                val sorted = msgs.sortedBy { it.timestamp }
                val previousIds = _messages.value.map { it.id }.toSet()
                val newIncoming = sorted.filter { it.id !in previousIds && it.senderId != uid }

                // Solo notificar si hay mensajes nuevos Y el chat no está en primer plano
                if (_messages.value.isNotEmpty() && newIncoming.isNotEmpty() && !_isChatActive.value) {
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
        if (_isBlocked.value || _isBlockedByOther.value) return
        val uid     = authRepository.currentUser?.uid ?: return
        val content = _messageInput.value.trim()
        if (content.isEmpty()) return
        _messageInput.value = ""
        viewModelScope.launch {
            sendMessageUseCase(uid, otherUserId, content).onFailure {
                _messageInput.value = content
                it.printStackTrace()
            }
        }
    }

    fun markMessagesAsRead(otherUserId: String) {
        val currentUid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            chatRepository.markMessagesAsRead(currentUid, otherUserId)
        }
    }

    fun deleteConversation(otherUserId: String, onDone: () -> Unit) {
        val uid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            deleteConversationUseCase(uid, otherUserId)
            _actionDone.value = "chat_deleted"
            onDone()
        }
    }

    fun blockUser(otherUserId: String, onDone: () -> Unit) {
        val uid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            blockUserUseCase(uid, otherUserId)
            _isBlocked.value  = true
            _actionDone.value = "user_blocked"
            onDone()
        }
    }

    fun unblockUser(otherUserId: String) {
        val uid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            blockRepository.unblockUser(uid, otherUserId)
            _isBlocked.value = false
        }
    }

    fun clearActionDone() { _actionDone.value = null }

    override fun onCleared() {
        super.onCleared()
        messagesJob?.cancel()
    }
}