package com.example.anilist.ui.home.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.example.anilist.data.models.AniNotification
import com.example.anilist.data.repository.notificationrepository.NotificationsPagingSource
import com.example.anilist.data.repository.notificationrepository.NotificationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 50

@HiltViewModel
class NotificationsViewModel
    @Inject
    constructor(private val notificationsRepository: NotificationsRepository) :
    ViewModel() {
        val notifications =
            Pager(
                config =
                    PagingConfig(
                        pageSize = PAGE_SIZE,
                        prefetchDistance = 5,
                        enablePlaceholders = true,
                    ),
                pagingSourceFactory = {
                    NotificationsPagingSource(
                        notificationsRepository,
                    )
                },
            ).flow.cachedIn(viewModelScope)

//    private val _notificationUnReadCount = MutableStateFlow(-1)
//    val notificationUnReadCount: StateFlow<Int> = _notificationUnReadCount

        private val _toastMessage = MutableSharedFlow<String>()
        val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

        private fun sendMessage(message: String) {
            viewModelScope.launch {
                _toastMessage.emit(message)
            }
        }

//    init {
//        viewModelScope.launch {
//            fetchNotificationUnReadCount()
//        }
//    }

//    private suspend fun fetchNotificationUnReadCount() {
//        when (val result = notificationsRepository.getUnReadNotificationCount()) {
//            is AniResult.Success -> {
//                _notificationUnReadCount.value = result.data
//            }
//
//            is AniResult.Failure -> {
//                sendMessage("Failed to load notifications unread count")
//            }
//        }
//    }

//    fun markAllNotificationsAsRead() {
//        viewModelScope.launch {
//            when (val result = notificationsRepository.markAllAsRead()) {
//                is AniResult.Success -> {
//                    _notificationUnReadCount.value = result.data
//                }
//
//                is AniResult.Failure -> {
//                    sendMessage("Failed to mark notifications as read")
//                }
//            }
//        }
//    }
    }

sealed interface NotificationsUiState {
    object Loading : NotificationsUiState

    data class Success(val notifications: List<AniNotification>) : NotificationsUiState

    data class Error(val message: String) : NotificationsUiState
}
