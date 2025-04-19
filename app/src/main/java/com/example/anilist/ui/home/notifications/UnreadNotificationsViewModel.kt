package com.example.anilist.ui.home.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anilist.data.models.AniResult
import com.example.anilist.data.repository.notificationrepository.NotificationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UnreadNotificationsViewModel
    @Inject
    constructor(
        private val notificationsRepository: NotificationsRepository,
    ) : ViewModel() {
        private val _notificationUnReadCount = MutableStateFlow(-1)
        val notificationUnReadCount: StateFlow<Int> = _notificationUnReadCount

        private val _toast = MutableSharedFlow<String>()
        val toast = _toast.asSharedFlow()

        private fun sendMessage(text: String) {
            viewModelScope.launch {
                _toast.emit(text)
            }
        }

        init {
            fetchNotificationUnReadCount()
        }

        fun fetchNotificationUnReadCount() {
            viewModelScope.launch {
                when (val result = notificationsRepository.getUnReadNotificationCount()) {
                    is AniResult.Success -> {
                        _notificationUnReadCount.value = result.data
                    }

                    is AniResult.Failure -> {
                        sendMessage("Failed to load notifications unread count")
                    }
                }
            }
        }

        fun markAllNotificationsAsRead() {
            viewModelScope.launch {
                when (val result = notificationsRepository.markAllAsRead()) {
                    is AniResult.Success -> {
                        _notificationUnReadCount.value = result.data
                    }

                    is AniResult.Failure -> {
                        sendMessage("Failed to mark notifications as read")
                    }
                }
            }
        }
    }
