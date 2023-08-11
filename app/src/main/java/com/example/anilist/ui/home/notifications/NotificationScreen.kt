package com.example.anilist.ui.home.notifications

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.apollographql.apollo3.api.not
import com.example.anilist.MainActivity
import com.example.anilist.R
import com.example.anilist.data.models.AniNotificationType
import com.example.anilist.utils.Utils
import com.example.anilist.data.models.Notification
import com.example.anilist.ui.Dimens
import com.example.anilist.ui.PleaseLogin
import com.example.anilist.ui.mediadetails.LoadingCircle

@Composable
fun NotificationScreen(
    notificationsViewModel: NotificationsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    navigateToMediaDetails: (Int) -> Unit,
    onNavigateToActivity: (Int) -> Unit,
    onNavigateToUser: (Int) -> Unit
) {
    val notifications = notificationsViewModel.notifications.collectAsLazyPagingItems()
    Notifications(
        notifications,
        notificationsViewModel::markAllNotificationsAsRead,
        onNavigateBack = onNavigateBack,
        navigateToMediaDetails = navigateToMediaDetails,
        onNavigateToUser = onNavigateToUser,
        onNavigateToActivity = onNavigateToActivity
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
private fun Notifications(
    notifications: LazyPagingItems<Notification>,
    markAllAsRead: () -> Unit,
    onNavigateBack: () -> Unit,
    navigateToMediaDetails: (Int) -> Unit,
    onNavigateToUser: (Int) -> Unit,
    onNavigateToActivity: (Int) -> Unit
) {
    var currentIndex by remember {
        mutableStateOf(NotificationFilterList.ALL)
    }
    val notificationFilterList = NotificationFilterList.values()
    Scaffold(topBar = {
        TopAppBar(title = { Text("Notifications") }, navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "back",
                )
            }
        })
    }) { padding ->
        if (MainActivity.accessCode != "") {
            when (notifications.loadState.refresh) {
                is LoadState.Error -> {
                    Text(text = "There was an error loading your notifications")
                }

                is LoadState.Loading -> {
                    LoadingCircle()
                }

                is LoadState.NotLoading -> {
                    Column(modifier = Modifier.padding(top = padding.calculateTopPadding())) {
                        FlowRow(modifier = Modifier.padding(Dimens.PaddingNormal)) {
                            notificationFilterList.forEachIndexed { _, filter ->
                                val selected = filter == currentIndex
                                FilterChip(
                                    selected = selected,
                                    onClick = { currentIndex = filter },
                                    leadingIcon = {
                                        if (selected) {
                                            Icon(
                                                Icons.Outlined.Check,
                                                contentDescription = null,
                                            )
                                        }
                                    },
                                    label = { Text(text = filter.toString(LocalContext.current)) },
                                    modifier = Modifier.padding(end = Dimens.PaddingSmall),
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = markAllAsRead,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.PaddingNormal),
                        ) {
                            Text(text = stringResource(R.string.mark_all_as_read))
                        }
                        LazyColumn {
                            items(
                                notifications.itemCount
//                        notifications.filter {
//                            when (currentIndex) {
//                                NotificationFilterList.ALL -> {
//                                    true
//                                }
//
//                                NotificationFilterList.AIRING -> {
//                                    it.type == "AiringNotification"
//                                }
//
//                                NotificationFilterList.ACTIVITY -> {
//                                    it.type == "Activity"
//                                }
//
//                                NotificationFilterList.FORUM -> {
//                                    it.type == "ThreadCommentSubscribedNotification"
//                                }
//
//                                NotificationFilterList.FOLLOWS -> {
//                                    it.type == "Follows"
//                                }
//
//                                NotificationFilterList.MEDIA -> {
//                                    it.type == "Media"
//                                }
//                            }
//                        },
                            ) { index ->
                                val notification = notifications[index]
                                if (notification != null) {
                                    when (notification.type) {
                                        AniNotificationType.AiringNotification -> {
                                            NotificationComponent(
                                                { navigateToMediaDetails(notification.animeId) },
                                                notification,
                                                context = "Episode ${notification.airedEpisode} of ${notification.title} aired"
                                            )
                                        }
                                        AniNotificationType.FollowingNotification -> {
                                            NotificationComponent(
                                                onClick = { onNavigateToActivity(notification.activityId) },
                                                notification = notification,
                                                context = "${notification.userName} followed you"
                                            )
                                        }

                                        AniNotificationType.ActivityMessageNotification -> {
                                            NotificationComponent(
                                                onClick = { onNavigateToActivity(notification.activityId) },
                                                notification =notification ,
                                                context = "Activity message from ${notification.userName}: ${notification.message}"
                                            )
                                        }
                                        AniNotificationType.ActivityMentionNotification -> {
                                            NotificationComponent(
                                                onClick = { onNavigateToActivity(notification.activityId) },
                                                notification = notification,
                                                context = "${notification.userName} mentioned you in an activity or reply"
                                            )
                                        }
                                        AniNotificationType.ActivityReplyNotification -> {
                                            NotificationComponent(
                                                onClick = { onNavigateToActivity(notification.activityId) },
                                                notification = notification,
                                                context = "${notification.userName} replied to your activity"
                                            )
                                        }
                                        AniNotificationType.ActivityReplySubscribedNotification -> {
                                            NotificationComponent(
                                                onClick = { onNavigateToActivity(notification.activityId) },
                                                notification = notification,
                                                context = "${notification.userName} replied to your reply on an activity"
                                            )
                                        }
                                        AniNotificationType.ActivityLikeNotification -> {
                                            NotificationComponent(
                                                onClick = { onNavigateToActivity(notification.activityId) },
                                                notification = notification,
                                                context = "${notification.userName} liked your activity"
                                            )
                                        }
                                        AniNotificationType.ActivityReplyLikeNotification -> {}
                                        AniNotificationType.ThreadCommentMentionNotification -> {}
                                        AniNotificationType.ThreadCommentReplyNotification -> {}
                                        AniNotificationType.ThreadCommentSubscribedNotification -> {}
                                        AniNotificationType.ThreadCommentLikeNotification -> {}
                                        AniNotificationType.ThreadLikeNotification -> {}
                                        AniNotificationType.RelatedMediaAdditionNotification -> {}
                                        AniNotificationType.MediaDataChangeNotification -> {}
                                        AniNotificationType.MediaMergeNotification -> {}
                                        AniNotificationType.MediaDeletionNotification -> {}
                                        AniNotificationType.UNKNOWN -> {}
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            PleaseLogin()
        }
    }
}

@Composable
private fun NotificationComponent(
    onClick: () -> Unit,
    notification: Notification,
    context: String
) {
    Column(modifier = Modifier.clickable {
        onClick()
    }) {
        Row {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(notification.image)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(id = R.drawable.no_image),
                fallback = painterResource(id = R.drawable.no_image),
                contentDescription = "notification cover",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(Dimens.PaddingNormal)
                    .clip(MaterialTheme.shapes.medium),
            )
            Column(modifier = Modifier.padding(Dimens.PaddingNormal)) {
                Text(
                    text = Utils.getRelativeTime(notification.createdAt.toLong()),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = context,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text("Type is: ${notification.type}")
                Text("Context given is ${notification.context}")
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(locale = "nl")
@Composable
fun NotificationScreenPreview() {
//    Notifications(
//        notifications = listOf(
//            Notification(
//                type = "Airing",
//                airedEpisode = 2,
//                title = "时光代理人 第二季",
//                createdAt = 1689303604,
//                image = "",
//            ),
//        ),
//        markAllAsRead = { },
//    ) { }
}

enum class NotificationFilterList {
    ALL,
    AIRING,
    ACTIVITY,
    FORUM,
    FOLLOWS,
    MEDIA, ;

    fun toString(context: Context): String {
        return when (this) {
            ALL -> context.getString(R.string.all)
            AIRING -> context.getString(R.string.airing)
            ACTIVITY -> context.getString(R.string.activity)
            FORUM -> context.getString(R.string.forum)
            FOLLOWS -> context.getString(R.string.follows)
            MEDIA -> context.getString(R.string.media)
        }
    }
}
