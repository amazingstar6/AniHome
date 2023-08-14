package com.example.anilist.data.models

data class AniReview(
    val id: Int = -1,
    val title: String = "",
    val userName: String = "",
    val createdAt: Int = -1,
    val body: String = "",
    val score: Int = -1,
    // upvotes is called rating and total votes is called ratingAmount
    val upvotes: Int = -1,
    val totalVotes: Int = -1,
    val userRating: AniReviewRatingStatus = AniReviewRatingStatus.NO_VOTE,
    val userAvatar: String = "",
)

enum class AniReviewRatingStatus {
    NO_VOTE,
    UP_VOTE,
    DOWN_VOTE,
    UNKNOWN,
}
