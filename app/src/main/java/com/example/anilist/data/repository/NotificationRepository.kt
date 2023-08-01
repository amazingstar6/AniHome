package com.example.anilist.data.repository

import com.apollographql.apollo3.exception.ApolloException
import com.example.anilist.utils.Apollo
import com.example.anilist.GetNotificationsQuery
import com.example.anilist.utils.ResultData
import com.example.anilist.utils.ResultStatus
import com.example.anilist.data.models.Notification
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor() {
    fun getNotifications() = flow {
        try {
            val response = Apollo.apolloClient.query(GetNotificationsQuery()).toFlow()
        } catch (exception: ApolloException) {
            // handle exception here,, these are mainly for network errors
            emit(ResultData(ResultStatus.ERROR, exception.message ?: "No error message"))
        }
        val result = Apollo.apolloClient.query(GetNotificationsQuery()).execute()
        if (result.hasErrors()) {
            // these errors are related to GraphQL errors
            emit(ResultData(ResultStatus.ERROR, result.errors.toString()))
        }
        val data = result.data
        emit(
            if (data == null) {
                ResultData(ResultStatus.NO_DATA)
            } else {
                ResultData(
                    ResultStatus.SUCCESSFUL,
                    data = parseNotification(data),
                )
            },
        )
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
