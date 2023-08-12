package com.example.anilist.ui.details.staffdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anilist.data.models.AniResult
import com.example.anilist.data.models.StaffDetail
import com.example.anilist.data.repository.StaffDetailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StaffDetailViewModel @Inject constructor(
    private val staffDetailRepository: StaffDetailRepository
) : ViewModel() {
    private val _staff: MutableStateFlow<StaffDetailUiState> =
        MutableStateFlow(StaffDetailUiState.Loading)
    val staff: StateFlow<StaffDetailUiState> = _staff

    fun fetchStaff(id: Int) {
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
    data class Success(val staff: StaffDetail) : StaffDetailUiState
}