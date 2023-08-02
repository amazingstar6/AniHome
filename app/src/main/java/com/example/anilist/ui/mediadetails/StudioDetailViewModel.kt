package com.example.anilist.ui.mediadetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.example.anilist.data.models.AniStudio
import com.example.anilist.data.repository.StudioDetailRepository
import com.example.anilist.data.repository.StudioMediaPagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudioDetailViewModel @Inject constructor(
    private val studioDetailRepository: StudioDetailRepository
) : ViewModel() {
    private val _studio: MutableStateFlow<AniStudio> = MutableStateFlow(AniStudio())
    val studio: StateFlow<AniStudio> = _studio

    @OptIn(ExperimentalCoroutinesApi::class)
    val mediaOfStudio = _studio.flatMapLatest {studio ->
        Pager(
            config = PagingConfig(pageSize = 25, prefetchDistance = 5, enablePlaceholders = false),
            pagingSourceFactory = {
                StudioMediaPagingSource(studioDetailRepository, studio.id)
            }
        ).flow.cachedIn(viewModelScope)
    }

    fun getStudioDetails(id: Int) {
        viewModelScope.launch {
            studioDetailRepository.getStudioDetails(id)
        }
    }
}

