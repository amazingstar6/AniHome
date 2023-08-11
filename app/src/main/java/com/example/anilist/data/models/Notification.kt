package com.example.anilist.data.models

data class Notification(
    val type: AniNotificationType = AniNotificationType.UNKNOWN,
    val context: List<String?>? = emptyList(),
    val createdAt: Int = -1,
    val image: String = "",
    val airedEpisode: Int = -1,
    val title: String = "",
    val animeId: Int = -1,
    val userId: Int = -1,
    val userName: String = "",
    val message: String = "",
    val activityId: Int = -1
)

enum class AniNotificationType {
    AiringNotification,
    FollowingNotification,
    ActivityMessageNotification,
    ActivityMentionNotification,
    ActivityReplyNotification,
    ActivityReplySubscribedNotification,
    ActivityLikeNotification,
    ActivityReplyLikeNotification,
    ThreadCommentMentionNotification,
    ThreadCommentReplyNotification,
    ThreadCommentSubscribedNotification,
    ThreadCommentLikeNotification,
    ThreadLikeNotification,
    RelatedMediaAdditionNotification,
    MediaDataChangeNotification,
    MediaMergeNotification,
    MediaDeletionNotification,
    UNKNOWN;
}