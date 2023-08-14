package com.example.anilist.ui.details.reviewdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anilist.data.models.AniResult
import com.example.anilist.data.models.AniReview
import com.example.anilist.data.models.AniReviewRatingStatus
import com.example.anilist.data.repository.ReviewDetailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewDetailViewModel @Inject constructor(
    private val reviewDetailRepository: ReviewDetailRepository
): ViewModel() {

    private val _review = MutableLiveData<AniReview>()
    val review: LiveData<AniReview> = _review

    fun fetchReview(reviewId: Int) {
        viewModelScope.launch {
            when (val data = reviewDetailRepository.fetchReview(reviewId)) {
                is AniResult.Success -> {
                    _review.value = data.data
                }
                is AniResult.Failure -> TODO()
            }
        }
    }

    fun rateReview(id: Int, rating: AniReviewRatingStatus) {
        viewModelScope.launch {
            when (val data = reviewDetailRepository.rateReview(id, rating)) {
                is AniResult.Success -> {
                    _review.value = _review.value?.copy(userRating = data.data)
                }
                is AniResult.Failure -> {
                    //todo send toast
                }
            }
        }
    }
}