package com.example.anilist.data.repository

import com.apollographql.apollo3.exception.ApolloException
import com.example.anilist.utils.Apollo
import com.example.anilist.GetNotificationsQuery
import com.example.anilist.data.models.AniResult
import com.example.anilist.utils.ResultData
import com.example.anilist.utils.ResultStatus
import com.example.anilist.data.models.Notification
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsRepository @Inject constructor() {
    suspend fun getNotifications(page: Int, pageSize: Int): AniResult<List<Notification>> {
        try {
            val result =
                Apollo.apolloClient.query(GetNotificationsQuery(page = page, perPage = pageSize))
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
            notification?.onAiringNotification.let {
                val onAiringNotification = notification?.onAiringNotification
                list.add(
                    Notification(
                        type = notification?.__typename ?: "",
                        context = onAiringNotification?.contexts,
                        image = onAiringNotification?.media?.coverImage?.extraLarge
                            ?: "",
                        airedEpisode = onAiringNotification?.episode ?: -1,
                        createdAt = onAiringNotification?.createdAt ?: -1,
                        title = onAiringNotification?.media?.title?.userPreferred ?: ",",
                    ),
                )
            }
            notification?.onThreadCommentSubscribedNotification.let {
                val onThreadCommentSubscribedNotification =
                    notification?.onThreadCommentSubscribedNotification
                list.add(
                    Notification(
                        type = notification?.__typename ?: "",
                        image = onThreadCommentSubscribedNotification?.user?.avatar?.large ?: "",
                        createdAt = onThreadCommentSubscribedNotification?.createdAt ?: -1,
                    ),
                )
            }
        }
        return list
    }
}
