package com.example.roommatch_pmdm.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roommatch_pmdm.data.repositories.AuthRepository
import com.example.roommatch_pmdm.data.repositories.BlockRepository
import com.example.roommatch_pmdm.data.repositories.ChatRepository
import com.example.roommatch_pmdm.data.repositories.MatchRepository
import com.example.roommatch_pmdm.domain.model.ChatMessage
import com.example.roommatch_pmdm.domain.model.ChatUser
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

