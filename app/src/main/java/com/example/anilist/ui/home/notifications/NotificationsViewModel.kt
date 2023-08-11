package com.example.anilist.ui.home.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.example.anilist.data.models.Notification
import com.example.anilist.data.repository.NotificationsPagingSource
import com.example.anilist.data.repository.NotificationsRepository
import com.example.anilist.data.repository.homerepository.HomeMedia
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

private const val PAGE_SIZE = 50

@HiltViewModel
class NotificationsViewModel @Inject constructor(private val notificationsRepository: NotificationsRepository) :
    ViewModel() {

//    private val _notifications = MutableStateFlow(NotificationsUiState.Loading)

    //    val notifications: StateFlow<NotificationsUiState> = _notifications
//    @OptIn(ExperimentalCoroutinesApi::class)
//    val notifications: StateFlow<NotificationsUiState> =
//        notificationsRepository.getNotifications().mapLatest {
//            when (it) {
//                is AniResult.Failure -> NotificationsUiState.Error(it.error)
//                is AniResult.Success -> NotificationsUiState.Success(it.data)
//            }
//        }.stateIn(
//            viewModelScope,
//            SharingStarted.WhileSubscribed(),
//            NotificationsUiState.Loading
//        )

    val notifications = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            prefetchDistance = 5,
            enablePlaceholders = true
        ),
        pagingSourceFactory = {
            NotificationsPagingSource(
                notificationsRepository
            )
        }
    ).flow.cachedIn(viewModelScope)


    private val _media = MutableLiveData<HomeMedia>()
    val media: LiveData<HomeMedia> = _media

    fun markAllNotificationsAsRead() {
        //todo
    }

}

sealed interface NotificationsUiState {
    object Loading : NotificationsUiState
    data class Success(val notifications: List<Notification>) : NotificationsUiState
    data class Error(val message: String) : NotificationsUiState
}