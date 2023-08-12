package com.example.anilist.data.models

import com.example.anilist.ui.home.notifications.NotificationFilterList

data class Notification(
    val type: AniNotificationType = AniNotificationType.UNKNOWN,
    val context: String = "",
    val createdAt: Int = -1,
    val image: String = "",
    val airedEpisode: Int = -1,
    val mediaTitle: String = "",
    val animeId: Int = -1,
    val userId: Int = -1,
    val userName: String = "",
    val message: String = "",
    val activityId: Int = -1,
    val threadCommentId: Int = -1,
    val threadId: Int = -1,
    val mediaId: Int = -1,
    val mediaChangeReason: String = "",
    val deletedMediaTitles: List<String> = emptyList(),
    val deletedMediaTitle: String = ""
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

    fun isInFilter(filter: NotificationFilterList): Boolean {
        if (filter == NotificationFilterList.ALL) return true
        return when (this) {
            AiringNotification -> filter == NotificationFilterList.AIRING
            FollowingNotification -> filter == NotificationFilterList.FOLLOWS
            ActivityMessageNotification -> filter == NotificationFilterList.ACTIVITY
            ActivityMentionNotification -> filter == NotificationFilterList.ACTIVITY
            ActivityReplyNotification -> filter == NotificationFilterList.ACTIVITY
            ActivityReplySubscribedNotification -> filter == NotificationFilterList.ACTIVITY
            ActivityLikeNotification -> filter == NotificationFilterList.ACTIVITY
            ActivityReplyLikeNotification -> filter == NotificationFilterList.ACTIVITY
            ThreadCommentMentionNotification -> filter == NotificationFilterList.FORUM
            ThreadCommentReplyNotification -> filter == NotificationFilterList.FORUM
            ThreadCommentSubscribedNotification -> filter == NotificationFilterList.FORUM
            ThreadCommentLikeNotification -> filter == NotificationFilterList.FORUM
            ThreadLikeNotification -> filter == NotificationFilterList.FORUM
            RelatedMediaAdditionNotification -> filter == NotificationFilterList.MEDIA
            MediaDataChangeNotification -> filter == NotificationFilterList.MEDIA
            MediaMergeNotification -> filter == NotificationFilterList.MEDIA
            MediaDeletionNotification -> filter == NotificationFilterList.MEDIA
            UNKNOWN -> false
        }
    }
}