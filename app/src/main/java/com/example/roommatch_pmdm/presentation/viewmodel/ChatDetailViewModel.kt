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
import com.example.roommatch_pmdm.domain.model.ChatMessage
import com.example.roommatch_pmdm.domain.model.ChatUser
import com.example.roommatch_pmdm.domain.usecase.BlockUserUseCase
import com.example.roommatch_pmdm.domain.usecase.DeleteConversationUseCase
import com.example.roommatch_pmdm.domain.usecase.SendMessageUseCase
import com.example.roommatch_pmdm.notifications.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.text.ifEmpty

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