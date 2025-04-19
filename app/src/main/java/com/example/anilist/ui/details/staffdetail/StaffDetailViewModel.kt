package com.example.anilist.ui.details.staffdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.example.anilist.data.models.AniResult
import com.example.anilist.data.models.AniStaffDetail
import com.example.anilist.data.repository.staffdetail.StaffDetailRepository
import com.example.anilist.data.repository.staffdetail.StaffMediaPagingSource
import com.example.anilist.ui.home.PREFETCH_DISTANCE
import com.example.anilist.ui.navigation.AniListRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StaffDetailViewModel
    @Inject
    constructor(
        private val staffDetailRepository: StaffDetailRepository,
        private val savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val _staff: MutableStateFlow<StaffDetailUiState> =
            MutableStateFlow(StaffDetailUiState.Loading)
        val staff: StateFlow<StaffDetailUiState> = _staff

        private var staffId = -1

        init {
            savedStateHandle.get<Int>(AniListRoute.STAFF_DETAIL_ID_KEY)?.let {
                staffId = it
                fetchStaff(it)
            }
        }

        val staffRoleMedia =
            Pager(
                config =
                    PagingConfig(
                        pageSize = 25,
                        prefetchDistance = PREFETCH_DISTANCE,
                        enablePlaceholders = false,
                    ),
                pagingSourceFactory = {
                    StaffMediaPagingSource(
                        staffId = staffId,
                        staffDetailRepository = staffDetailRepository,
                    )
                },
            ).flow.cachedIn(viewModelScope)

        private fun fetchStaff(id: Int) {
            viewModelScope.launch {
                when (val data = staffDetailRepository.fetchStaffDetail(id)) {
                    is AniResult.Success -> {
                        _staff.value = StaffDetailUiState.Success(data.data)
                    }

                    is AniResult.Failure -> TODO()
                }
            }
        }

        fun toggleFavourite(id: Int) {
            viewModelScope.launch {
                when (val isFavourite = staffDetailRepository.toggleFavourite(id)) {
                    is AniResult.Success -> {
                        (_staff.value as? StaffDetailUiState.Success)?.staff?.copy(isFavourite = isFavourite.data)
                            ?.let {
                                _staff.value = StaffDetailUiState.Success(it)
                            }
                    }

                    is AniResult.Failure -> {
                        _staff.value = StaffDetailUiState.Error(isFavourite.error)
                    }
                }
            }
        }
    }

sealed interface StaffDetailUiState {
    object Loading : StaffDetailUiState

    data class Error(val message: String) : StaffDetailUiState

    data class Success(val staff: AniStaffDetail) : StaffDetailUiState
}
