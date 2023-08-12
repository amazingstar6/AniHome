package com.example.anilist.data.repository

import com.apollographql.apollo3.exception.ApolloException
import com.example.anilist.GetReviewDetailQuery
import com.example.anilist.RateReviewMutation
import com.example.anilist.data.models.AniResult
import com.example.anilist.data.models.Review
import com.example.anilist.data.models.ReviewRatingStatus
import com.example.anilist.type.ReviewRating
import com.example.anilist.utils.Apollo
import javax.inject.Inject

class ReviewDetailRepository @Inject constructor() {
    suspend fun fetchReview(reviewId: Int): AniResult<Review> {
        try {
            val result =
                Apollo.apolloClient.query(
                    GetReviewDetailQuery(reviewId),
                )
                    .execute()
            if (result.hasErrors()) {
                return AniResult.Failure(buildString {
                    result.errors?.forEach { appendLine(it.message) }
                })
            }
            val review = result.data?.Review
            return if (review != null) {
                AniResult.Success(parseReview(review))
            } else {
                AniResult.Failure("Network error")
            }
        } catch (exception: ApolloException) {
            return AniResult.Failure(exception.localizedMessage ?: "No exception message given")
        }
    }


    /**
     * Returns whether the operation was successful //todo change to return new rating?
     */
    suspend fun rateReview(id: Int, rating: ReviewRatingStatus): AniResult<Boolean> {
        try {
            val apiRating = when (rating) {
                ReviewRatingStatus.NO_VOTE -> ReviewRating.NO_VOTE
                ReviewRatingStatus.UP_VOTE -> ReviewRating.UP_VOTE
                ReviewRatingStatus.DOWN_VOTE -> ReviewRating.DOWN_VOTE
            }
            val result =
                Apollo.apolloClient.mutation(
                    RateReviewMutation(
                        rating = apiRating,
                        id = id
                    ),
                )
                    .execute()
            if (result.hasErrors()) {
                return AniResult.Failure(buildString {
                    result.errors?.forEach { appendLine(it.message) }
                })
            }
            // the result is a list of all the things you've already liked of the same type
            return AniResult.Success(result.data?.RateReview?.userRating == apiRating)
        } catch (exception: ApolloException) {
            return AniResult.Failure(exception.localizedMessage ?: "No exception message given")
        }
    }

    private fun parseReview(review: GetReviewDetailQuery.Review?): Review {
        return Review(
            id = review?.id ?: -1,
            title = review?.summary ?: "",
            userName = review?.user?.name ?: "",
            createdAt = review?.createdAt ?: -1,
            body = review?.body ?: "",
            score = review?.score ?: -1,
            upvotes = review?.rating ?: -1,
            totalVotes = review?.ratingAmount ?: -1,
            userRating = when (review?.userRating) {
                ReviewRating.NO_VOTE -> ReviewRatingStatus.NO_VOTE
                ReviewRating.UP_VOTE -> ReviewRatingStatus.UP_VOTE
                ReviewRating.DOWN_VOTE -> ReviewRatingStatus.DOWN_VOTE
                else -> ReviewRatingStatus.NO_VOTE
            },
            userAvatar = review?.user?.avatar?.large ?: "",
        )
    }
}