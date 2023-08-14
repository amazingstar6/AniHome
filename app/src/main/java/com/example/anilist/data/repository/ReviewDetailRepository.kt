package com.example.anilist.data.repository

import com.apollographql.apollo3.exception.ApolloException
import com.example.anilist.GetReviewDetailQuery
import com.example.anilist.RateReviewMutation
import com.example.anilist.data.models.AniResult
import com.example.anilist.data.models.AniReview
import com.example.anilist.data.models.AniReviewRatingStatus
import com.example.anilist.data.toAni
import com.example.anilist.type.ReviewRating
import com.example.anilist.utils.Apollo
import javax.inject.Inject

class ReviewDetailRepository @Inject constructor() {
    suspend fun fetchReview(reviewId: Int): AniResult<AniReview> {
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
    suspend fun rateReview(id: Int, rating: AniReviewRatingStatus): AniResult<AniReviewRatingStatus> {
        try {
            val apiRating = when (rating) {
                AniReviewRatingStatus.NO_VOTE -> ReviewRating.NO_VOTE
                AniReviewRatingStatus.UP_VOTE -> ReviewRating.UP_VOTE
                AniReviewRatingStatus.DOWN_VOTE -> ReviewRating.DOWN_VOTE
                AniReviewRatingStatus.UNKNOWN -> return AniResult.Failure("Wrong rating given to rate")
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
            return AniResult.Success(result.data?.RateReview?.userRating.toAni())
        } catch (exception: ApolloException) {
            return AniResult.Failure(exception.localizedMessage ?: "No exception message given")
        }
    }

    private fun parseReview(review: GetReviewDetailQuery.Review?): AniReview {
        return AniReview(
            id = review?.id ?: -1,
            title = review?.summary ?: "",
            userName = review?.user?.name ?: "",
            createdAt = review?.createdAt ?: -1,
            body = review?.body ?: "",
            score = review?.score ?: -1,
            upvotes = review?.rating ?: -1,
            totalVotes = review?.ratingAmount ?: -1,
            userRating = when (review?.userRating) {
                ReviewRating.NO_VOTE -> AniReviewRatingStatus.NO_VOTE
                ReviewRating.UP_VOTE -> AniReviewRatingStatus.UP_VOTE
                ReviewRating.DOWN_VOTE -> AniReviewRatingStatus.DOWN_VOTE
                else -> AniReviewRatingStatus.NO_VOTE
            },
            userAvatar = review?.user?.avatar?.large ?: "",
        )
    }
}