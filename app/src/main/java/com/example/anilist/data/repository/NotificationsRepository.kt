package com.example.anilist.data.repository

import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.exception.ApolloException
import com.example.anilist.utils.Apollo
import com.example.anilist.GetNotificationsQuery
import com.example.anilist.data.models.AniNotificationType
import com.example.anilist.data.models.AniResult
import com.example.anilist.data.models.Notification
import com.example.anilist.type.NotificationType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsRepository @Inject constructor() {
    suspend fun getNotifications(page: Int, pageSize: Int): AniResult<List<Notification>> {
        try {
            val result =
                Apollo.apolloClient.newBuilder().fetchPolicy(FetchPolicy.NetworkFirst).build()
                    .query(GetNotificationsQuery(page = page, perPage = pageSize))
                    .execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
                return AniResult.Failure(buildString {
                    result.errors?.forEach { appendLine(it.message) }
                })
            }
            val data = result.data
            return if (data == null) {
                AniResult.Failure("Network error")
            } else {
                AniResult.Success(
                    parseNotification(data),
                )
            }
        } catch (exception: ApolloException) {
            // handle exception here,, these are mainly for network errors
            return AniResult.Failure(exception.message ?: "No error message")
        }

    }

    private fun parseNotification(data: GetNotificationsQuery.Data?): List<Notification> {
        val list = mutableListOf<Notification>()
        for (notification in data?.Page?.notifications.orEmpty()) {
            notification?.onAiringNotification?.let { onAiringNotification ->
                list.add(
                    Notification(
                        type = onAiringNotification.type?.toAni() ?: AniNotificationType.UNKNOWN,
                        context = onAiringNotification.contexts,
                        image = onAiringNotification.media?.coverImage?.extraLarge
                            ?: "",
                        animeId = onAiringNotification.animeId ?: -1,
                        airedEpisode = onAiringNotification.episode ?: -1,
                        createdAt = onAiringNotification.createdAt ?: -1,
                        title = onAiringNotification.media?.title?.userPreferred ?: ",",
                    ),
                )
            }
            notification?.onFollowingNotification?.let { onFollowingNotification ->
                list.add(
                    Notification(
                        type = onFollowingNotification.type.toAni(),
                        context = listOf(onFollowingNotification.context),
                        createdAt = onFollowingNotification.createdAt ?: -1,
                        image = onFollowingNotification.user?.avatar?.large ?: "",
                        userId = onFollowingNotification.user?.id ?: -1,
                        userName = onFollowingNotification.user?.name ?: "",
                    )
                )
            }
            notification?.onActivityMessageNotification?.let { onActivityMessageNotification ->
                list.add(
                    Notification(
                        type = onActivityMessageNotification.type.toAni(),
                        createdAt = onActivityMessageNotification.createdAt ?: -1,
                        userId = onActivityMessageNotification.userId,
                        image = onActivityMessageNotification.user?.avatar?.large ?: "",
                        userName = onActivityMessageNotification.user?.name ?: "",
                        message = onActivityMessageNotification.message?.message ?: "",
                        activityId = onActivityMessageNotification.activityId
                    )
                )
            }
            notification?.onActivityMentionNotification?.let { onActivityMentionNotification ->
                list.add(
                    Notification(
                        type = onActivityMentionNotification.type.toAni(),
                        createdAt = onActivityMentionNotification.createdAt ?: -1,
                        userId = onActivityMentionNotification.userId,
                        userName = onActivityMentionNotification.user?.name ?: "",
                        image = onActivityMentionNotification.user?.avatar?.large ?: "",
                        activityId = onActivityMentionNotification.activityId
                    )
                )
            }
            notification?.onActivityReplyNotification?.let { onActivityReplyNotification ->
                list.add(
                    Notification(
                        type = onActivityReplyNotification.type.toAni(),
                        createdAt = onActivityReplyNotification.createdAt ?: -1,
                        context = listOf(onActivityReplyNotification.context),
                        activityId = onActivityReplyNotification.activityId,
                    )
                )
            }
            notification?.onActivityReplySubscribedNotification?.let { onActivityReplySubscribedNotification ->
                list.add(
                    Notification(
                        type = onActivityReplySubscribedNotification.type.toAni(),
                        createdAt = onActivityReplySubscribedNotification.createdAt ?: -1,
                        context = listOf(onActivityReplySubscribedNotification.context),
                        userId = onActivityReplySubscribedNotification.userId,
                        activityId = onActivityReplySubscribedNotification.activityId
                    )
                )
            }
            notification?.onActivityLikeNotification?.let {
                onActivityLikeNotification ->
                list.add(
                    Notification(
                        type = onActivityLikeNotification.type.toAni(),
                        createdAt = onActivityLikeNotification.createdAt ?: -1,
                        image = onActivityLikeNotification.user?.avatar?.large ?: "",
                        activityId = onActivityLikeNotification.activityId,
                        userId = onActivityLikeNotification.userId,
                        context = listOf(onActivityLikeNotification.context),
                        userName = onActivityLikeNotification.user?.name ?: ""
                    )
                )
            }
//            notification?.onThreadCommentSubscribedNotification.let {
//                val onThreadCommentSubscribedNotification =
//                    notification?.onThreadCommentSubscribedNotification
//                list.add(
//                    Notification(
//                        type = notification?.__typename ?: "",
//                        image = onThreadCommentSubscribedNotification?.user?.avatar?.large ?: "",
//                        createdAt = onThreadCommentSubscribedNotification?.createdAt ?: -1,
//                    ),
//                )
//            }
        }
        return list
    }
}

private fun NotificationType?.toAni(): AniNotificationType {
    return when (this) {
        NotificationType.ACTIVITY_MESSAGE -> AniNotificationType.ActivityMessageNotification
        NotificationType.ACTIVITY_REPLY -> AniNotificationType.ActivityReplyNotification
        NotificationType.FOLLOWING -> AniNotificationType.FollowingNotification
        NotificationType.ACTIVITY_MENTION -> AniNotificationType.ActivityMentionNotification
        NotificationType.THREAD_COMMENT_MENTION -> AniNotificationType.ThreadCommentMentionNotification
        NotificationType.THREAD_SUBSCRIBED -> AniNotificationType.ThreadCommentSubscribedNotification
        NotificationType.THREAD_COMMENT_REPLY -> AniNotificationType.ThreadCommentReplyNotification
        NotificationType.AIRING -> AniNotificationType.AiringNotification
        NotificationType.ACTIVITY_LIKE -> AniNotificationType.ActivityLikeNotification
        NotificationType.ACTIVITY_REPLY_LIKE -> AniNotificationType.ActivityReplyLikeNotification
        NotificationType.THREAD_LIKE -> AniNotificationType.ThreadLikeNotification
        NotificationType.THREAD_COMMENT_LIKE -> AniNotificationType.ThreadCommentLikeNotification
        NotificationType.ACTIVITY_REPLY_SUBSCRIBED -> AniNotificationType.ActivityReplySubscribedNotification
        NotificationType.RELATED_MEDIA_ADDITION -> AniNotificationType.RelatedMediaAdditionNotification
        NotificationType.MEDIA_DATA_CHANGE -> AniNotificationType.MediaDataChangeNotification
        NotificationType.MEDIA_MERGE -> AniNotificationType.MediaMergeNotification
        NotificationType.MEDIA_DELETION -> AniNotificationType.MediaDeletionNotification
        NotificationType.UNKNOWN__ -> AniNotificationType.UNKNOWN
        null -> AniNotificationType.UNKNOWN
    }
}


