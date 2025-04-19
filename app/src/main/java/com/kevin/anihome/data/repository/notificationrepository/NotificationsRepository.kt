package com.kevin.anihome.data.repository.notificationrepository

import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.exception.ApolloException
import com.kevin.anihome.GetNotificationsQuery
import com.kevin.anihome.GetUnReadNotificationCountQuery
import com.kevin.anihome.MarkAllAsReadQuery
import com.kevin.anihome.data.models.AniNotification
import com.kevin.anihome.data.models.AniNotificationType
import com.kevin.anihome.data.models.AniResult
import com.kevin.anihome.data.toAni
import com.kevin.anihome.utils.Apollo
import com.kevin.anihome.utils.Utils.Companion.orMinusOne
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsRepository
    @Inject
    constructor() {
        suspend fun getNotifications(
            page: Int,
            pageSize: Int,
        ): AniResult<List<AniNotification>> {
            try {
                val result =
                    Apollo.apolloClient.newBuilder().fetchPolicy(FetchPolicy.NetworkFirst).build()
                        .query(GetNotificationsQuery(page = page, perPage = pageSize))
                        .execute()
                if (result.hasErrors()) {
                    // these errors are related to GraphQL errors
                    return AniResult.Failure(
                        buildString {
                            result.errors?.forEach { appendLine(it.message) }
                        },
                    )
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

        suspend fun getUnReadNotificationCount(): AniResult<Int> {
            try {
                val result =
                    Apollo.apolloClient.newBuilder().fetchPolicy(FetchPolicy.NetworkFirst).build()
                        .query(GetUnReadNotificationCountQuery())
                        .execute()
                if (result.hasErrors()) {
                    // these errors are related to GraphQL errors
                    return AniResult.Failure(
                        buildString {
                            result.errors?.forEach { appendLine(it.message) }
                        },
                    )
                }
                val data = result.data
                return if (data?.Viewer?.unreadNotificationCount == null) {
                    AniResult.Failure("Network error")
                } else {
                    AniResult.Success(
                        data.Viewer.unreadNotificationCount,
                    )
                }
            } catch (exception: ApolloException) {
                // handle exception here,, these are mainly for network errors
                return AniResult.Failure(exception.message ?: "No error message")
            }
        }

        suspend fun markAllAsRead(): AniResult<Int> {
            try {
                val result =
                    Apollo.apolloClient.newBuilder().fetchPolicy(FetchPolicy.NetworkFirst).build()
                        .query(MarkAllAsReadQuery())
                        .execute()
                if (result.hasErrors()) {
                    // these errors are related to GraphQL errors
                    return AniResult.Failure(
                        buildString {
                            result.errors?.forEach { appendLine(it.message) }
                        },
                    )
                }
                val data = result.data
                return if (data?.Viewer?.unreadNotificationCount == null) {
                    AniResult.Failure("Network error")
                } else {
                    AniResult.Success(
                        data.Viewer.unreadNotificationCount,
                    )
                }
            } catch (exception: ApolloException) {
                // handle exception here,, these are mainly for network errors
                return AniResult.Failure(exception.message ?: "No error message")
            }
        }

        private fun parseNotification(data: GetNotificationsQuery.Data?): List<AniNotification> {
            val list = mutableListOf<AniNotification>()
            for (notification in data?.Page?.notifications.orEmpty()) {
                notification?.onAiringNotification?.let { onAiringNotification ->
                    list.add(
                        AniNotification(
                            type = onAiringNotification.type?.toAni() ?: AniNotificationType.UNKNOWN,
                            context = buildString { onAiringNotification.contexts?.forEach { append(" $it") } },
                            image =
                                onAiringNotification.media?.coverImage?.extraLarge
                                    ?: "",
                            animeId = onAiringNotification.animeId ?: -1,
                            airedEpisode = onAiringNotification.episode ?: -1,
                            createdAt = onAiringNotification.createdAt ?: -1,
                            mediaTitle = onAiringNotification.media?.title?.userPreferred ?: ",",
                        ),
                    )
                }
                notification?.onFollowingNotification?.let { onFollowingNotification ->
                    list.add(
                        AniNotification(
                            type = onFollowingNotification.type.toAni(),
                            context = onFollowingNotification.context.orEmpty(),
                            createdAt = onFollowingNotification.createdAt ?: -1,
                            image = onFollowingNotification.user?.avatar?.large ?: "",
                            userId = onFollowingNotification.user?.id ?: -1,
                            userName = onFollowingNotification.user?.name ?: "",
                        ),
                    )
                }
                notification?.onActivityMessageNotification?.let { onActivityMessageNotification ->
                    list.add(
                        AniNotification(
                            type = onActivityMessageNotification.type.toAni(),
                            createdAt = onActivityMessageNotification.createdAt ?: -1,
                            userId = onActivityMessageNotification.userId,
                            context = onActivityMessageNotification.context.orEmpty(),
                            image = onActivityMessageNotification.user?.avatar?.large ?: "",
                            userName = onActivityMessageNotification.user?.name ?: "",
                            message = onActivityMessageNotification.message?.message ?: "",
                            activityId = onActivityMessageNotification.activityId,
                        ),
                    )
                }
                notification?.onActivityMentionNotification?.let { onActivityMentionNotification ->
                    list.add(
                        AniNotification(
                            type = onActivityMentionNotification.type.toAni(),
                            createdAt = onActivityMentionNotification.createdAt ?: -1,
                            userId = onActivityMentionNotification.userId,
                            userName = onActivityMentionNotification.user?.name ?: "",
                            image = onActivityMentionNotification.user?.avatar?.large ?: "",
                            activityId = onActivityMentionNotification.activityId,
                            context = onActivityMentionNotification.context.orEmpty(),
                        ),
                    )
                }
                notification?.onActivityReplyNotification?.let { onActivityReplyNotification ->
                    list.add(
                        AniNotification(
                            type = onActivityReplyNotification.type.toAni(),
                            createdAt = onActivityReplyNotification.createdAt ?: -1,
                            context = onActivityReplyNotification.context.orEmpty(),
                            activityId = onActivityReplyNotification.activityId,
                        ),
                    )
                }
                notification?.onActivityReplySubscribedNotification?.let { onActivityReplySubscribedNotification ->
                    list.add(
                        AniNotification(
                            type = onActivityReplySubscribedNotification.type.toAni(),
                            createdAt = onActivityReplySubscribedNotification.createdAt ?: -1,
                            context = onActivityReplySubscribedNotification.context.orEmpty(),
                            userId = onActivityReplySubscribedNotification.userId,
                            activityId = onActivityReplySubscribedNotification.activityId,
                        ),
                    )
                }
                notification?.onActivityLikeNotification?.let { onActivityLikeNotification ->
                    list.add(
                        AniNotification(
                            type = onActivityLikeNotification.type.toAni(),
                            createdAt = onActivityLikeNotification.createdAt ?: -1,
                            image = onActivityLikeNotification.user?.avatar?.large ?: "",
                            activityId = onActivityLikeNotification.activityId,
                            userId = onActivityLikeNotification.userId,
                            context = onActivityLikeNotification.context.orEmpty(),
                            userName = onActivityLikeNotification.user?.name ?: "",
                        ),
                    )
                }
                notification?.onActivityReplyLikeNotification?.let { onActivityReplyLikeNotification ->
                    list.add(
                        AniNotification(
                            type = onActivityReplyLikeNotification.type.toAni(),
                            createdAt = onActivityReplyLikeNotification.createdAt ?: -1,
                            image = onActivityReplyLikeNotification.user?.avatar?.large ?: "",
                            activityId = onActivityReplyLikeNotification.activityId,
                            context = onActivityReplyLikeNotification.context.orEmpty(),
                            userName = onActivityReplyLikeNotification.user?.name ?: "",
                            userId = onActivityReplyLikeNotification.userId,
                        ),
                    )
                }
                notification?.onThreadCommentMentionNotification?.let { onThreadCommentMentionNotification ->
                    list.add(
                        AniNotification(
                            type = onThreadCommentMentionNotification.type.toAni(),
                            createdAt = onThreadCommentMentionNotification.createdAt ?: -1,
                            image = onThreadCommentMentionNotification.user?.avatar?.large ?: "",
                            userId = onThreadCommentMentionNotification.userId,
                            userName = onThreadCommentMentionNotification.user?.name ?: "",
                            context = onThreadCommentMentionNotification.context.orEmpty(),
                            threadCommentId = onThreadCommentMentionNotification.commentId,
                            threadId = onThreadCommentMentionNotification.thread?.id ?: -1,
                        ),
                    )
                }
                notification?.onThreadCommentReplyNotification?.let { onThreadCommentReplyNotification ->
                    list.add(
                        AniNotification(
                            type = onThreadCommentReplyNotification.type.toAni(),
                            createdAt = onThreadCommentReplyNotification.createdAt ?: -1,
                            image = onThreadCommentReplyNotification.user?.avatar?.large ?: "",
                            userId = onThreadCommentReplyNotification.userId,
                            userName = onThreadCommentReplyNotification.user?.name ?: "",
                            threadCommentId = onThreadCommentReplyNotification.commentId,
                            threadId = onThreadCommentReplyNotification.thread?.id ?: -1,
                            context = onThreadCommentReplyNotification.context.orEmpty(),
                        ),
                    )
                }
                notification?.onThreadCommentSubscribedNotification?.let { onThreadCommentSubscribedNotification ->
                    list.add(
                        AniNotification(
                            type = onThreadCommentSubscribedNotification.type.toAni(),
                            createdAt = onThreadCommentSubscribedNotification.createdAt.orMinusOne(),
                            context = onThreadCommentSubscribedNotification.context.orEmpty(),
                            threadCommentId = onThreadCommentSubscribedNotification.commentId,
                            userName = onThreadCommentSubscribedNotification.user?.name.orEmpty(),
                            userId = onThreadCommentSubscribedNotification.userId,
                            threadId = onThreadCommentSubscribedNotification.thread?.id.orMinusOne(),
                            image = onThreadCommentSubscribedNotification.user?.avatar?.large.orEmpty(),
                        ),
                    )
                }
                notification?.onThreadCommentLikeNotification?.let { onThreadCommentLikeNotification ->
                    list.add(
                        AniNotification(
                            type = onThreadCommentLikeNotification.type.toAni(),
                            createdAt = onThreadCommentLikeNotification.createdAt ?: -1,
                            context = onThreadCommentLikeNotification.context.orEmpty(),
                            threadCommentId = onThreadCommentLikeNotification.commentId,
                            threadId = onThreadCommentLikeNotification.thread?.id ?: -1,
                            userId = onThreadCommentLikeNotification.userId,
                            userName = onThreadCommentLikeNotification.user?.name ?: "",
                            image = onThreadCommentLikeNotification.user?.avatar?.large ?: "",
                        ),
                    )
                }
                notification?.onThreadLikeNotification?.let { onThreadLikeNotification ->
                    list.add(
                        AniNotification(
                            type = onThreadLikeNotification.type.toAni(),
                            createdAt = onThreadLikeNotification.createdAt ?: -1,
                            context = onThreadLikeNotification.context.orEmpty(),
                            threadId = onThreadLikeNotification.threadId,
                            threadCommentId = onThreadLikeNotification.comment?.id ?: -1,
                        ),
                    )
                }
                notification?.onRelatedMediaAdditionNotification?.let { onRelatedMediaAdditionNotification ->
                    list.add(
                        AniNotification(
                            type = onRelatedMediaAdditionNotification.type.toAni(),
                            createdAt = onRelatedMediaAdditionNotification.createdAt ?: -1,
                            context = onRelatedMediaAdditionNotification.context.orEmpty(),
                            mediaId = onRelatedMediaAdditionNotification.mediaId,
                            mediaTitle =
                                onRelatedMediaAdditionNotification.media?.title?.userPreferred
                                    ?: "",
                            image =
                                onRelatedMediaAdditionNotification.media?.coverImage?.extraLarge
                                    ?: "",
                        ),
                    )
                }
                notification?.onMediaDataChangeNotification?.let { onMediaDataChangeNotification ->
                    list.add(
                        AniNotification(
                            type = onMediaDataChangeNotification.type.toAni(),
                            context = onMediaDataChangeNotification.context.orEmpty(),
                            createdAt = onMediaDataChangeNotification.createdAt ?: -1,
                            mediaId = onMediaDataChangeNotification.mediaId,
                            image = onMediaDataChangeNotification.media?.coverImage?.extraLarge ?: "",
                            mediaTitle =
                                onMediaDataChangeNotification.media?.title?.userPreferred
                                    ?: "",
                            mediaChangeReason = onMediaDataChangeNotification.reason ?: "",
                        ),
                    )
                }
                notification?.onMediaMergeNotification?.let { onMediaMergeNotification ->
                    list.add(
                        AniNotification(
                            type = onMediaMergeNotification.type.toAni(),
                            context = onMediaMergeNotification.context.orEmpty(),
                            createdAt = onMediaMergeNotification.createdAt.orMinusOne(),
                            mediaChangeReason = onMediaMergeNotification.reason.orEmpty(),
                            deletedMediaTitles =
                                onMediaMergeNotification.deletedMediaTitles?.filterNotNull()
                                    .orEmpty(),
                            mediaTitle = onMediaMergeNotification.media?.title?.userPreferred.orEmpty(),
                            image = onMediaMergeNotification.media?.coverImage?.extraLarge.orEmpty(),
                            mediaId = onMediaMergeNotification.mediaId,
                        ),
                    )
                }
                notification?.onMediaDeletionNotification?.let { onMediaDeletionNotification ->
                    list.add(
                        AniNotification(
                            type = onMediaDeletionNotification.type.toAni(),
                            context = onMediaDeletionNotification.context.orEmpty(),
                            createdAt = onMediaDeletionNotification.createdAt.orMinusOne(),
                            deletedMediaTitle = onMediaDeletionNotification.deletedMediaTitle.orEmpty(),
                            mediaChangeReason = onMediaDeletionNotification.reason.orEmpty(),
                        ),
                    )
                }
            }
            return list
        }
    }
