package com.example.anilist.ui.home.notifications

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.anilist.MainActivity
import com.example.anilist.R
import com.example.anilist.data.models.AniNotificationType
import com.example.anilist.utils.Utils
import com.example.anilist.data.models.AniNotification
import com.example.anilist.ui.Dimens
import com.example.anilist.ui.PleaseLogin
import com.example.anilist.utils.LoadingCircle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun NotificationScreen(
    notificationsViewModel: NotificationsViewModel = hiltViewModel(),
    unreadNotificationsViewModel: UnreadNotificationsViewModel,
    onNavigateBack: () -> Unit,
    navigateToMediaDetails: (Int) -> Unit,
    onNavigateToActivity: (Int) -> Unit,
    onNavigateToUser: (Int) -> Unit,
    onNavigateToThread: (Int) -> Unit,
    onNavigateToThreadComment: (Int) -> Unit
) {
    val notifications = notificationsViewModel.notifications.collectAsLazyPagingItems()
    val unReadNotificationsCount by unreadNotificationsViewModel.notificationUnReadCount.collectAsStateWithLifecycle()

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        notificationsViewModel
            .toastMessage
            .collect { message ->
                Toast.makeText(
                    context,
                    message,
                    Toast.LENGTH_SHORT,
                ).show()
            }
    }

    Notifications(
        notifications = notifications,
        unReadNotificationsCount = unReadNotificationsCount,
        markAllAsRead = unreadNotificationsViewModel::markAllNotificationsAsRead,
        onNavigateBack = onNavigateBack,
        navigateToMediaDetails = navigateToMediaDetails,
        onNavigateToUser = onNavigateToUser,
        onNavigateToActivity = onNavigateToActivity,
        onNavigateToThread = onNavigateToThread,
        onNavigateToThreadComment = onNavigateToThreadComment,
        reloadNotificationCount = unreadNotificationsViewModel::fetchNotificationUnReadCount
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
private fun Notifications(
    notifications: LazyPagingItems<AniNotification>,
    unReadNotificationsCount: Int,
    markAllAsRead: () -> Unit,
    reloadNotificationCount: () -> Unit,
    onNavigateBack: () -> Unit,
    navigateToMediaDetails: (Int) -> Unit,
    onNavigateToUser: (Int) -> Unit,
    onNavigateToActivity: (Int) -> Unit,
    onNavigateToThread: (Int) -> Unit, // todo pass comment id as well
    onNavigateToThreadComment: (Int) -> Unit
) {
    var currentFilter by remember {
        mutableStateOf(NotificationFilterList.ALL)
    }
    val notificationFilterList = NotificationFilterList.values()
    val topAppBarState = rememberTopAppBarState()
    val topBarScroll = TopAppBarDefaults.enterAlwaysScrollBehavior(state = topAppBarState)
    val lazyState = rememberLazyListState()
    val lazyScrollScope = rememberCoroutineScope()
    val fabVisibility by remember {
        derivedStateOf {
            lazyState.firstVisibleItemIndex != 0
        }
    }
    Scaffold(modifier = Modifier.nestedScroll(topBarScroll.nestedScrollConnection), topBar = {
        TopAppBar(
            scrollBehavior = topBarScroll,
            title = { Text("Notifications") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "back",
                    )
                }
            },
            actions = {
                PlainTooltipBox(tooltip = { Text(text = "Reload") }) {
                    IconButton(onClick = {
                        notifications.refresh()
                        reloadNotificationCount()
                    }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reload")
                    }
                }
            })
    }, floatingActionButton = {
        ScrollUpFab(fabVisibility, lazyScrollScope, topAppBarState, lazyState)
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
                    LazyColumn(
                        state = lazyState,
                        modifier = Modifier.padding(top = padding.calculateTopPadding())
                    ) {
                        item {
                            FlowRow(
                                modifier = Modifier
                                    .padding(horizontal = Dimens.PaddingNormal)
                            ) {
                                notificationFilterList.forEachIndexed { _, filter ->
                                    val selected = filter == currentFilter
                                    FilterChip(
                                        selected = selected,
                                        onClick = {
                                            currentFilter = filter
                                        },
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
//                        OutlinedButton(
//                            onClick = { },
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(horizontal = Dimens.PaddingNormal)
//                        ) {
//                            Text(text = "Amount of unread notifications is $unReadNotificationsCount")
//                        }
                            OutlinedButton(
                                onClick = markAllAsRead,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Dimens.PaddingNormal),
                            ) {
                                Text(text = stringResource(R.string.mark_all_as_read))
                            }
                        }
                        items(
                            notifications.itemCount
                        ) { index ->
                            val notification = notifications[index]
                            if (notification != null && notification.type.isInFilter(
                                    currentFilter
                                )
                            ) {
                                val isUnread by remember(key1 = unReadNotificationsCount) {
                                    mutableStateOf(
                                        index < unReadNotificationsCount
                                    )
                                }
                                when (notification.type) {

                                    AniNotificationType.AiringNotification -> {
                                        NotificationComponent(
                                            { navigateToMediaDetails(notification.animeId) },
                                            notification,
                                            context = "Episode ${notification.airedEpisode} of ${notification.mediaTitle} aired.",
                                            isUnRead = isUnread
                                        )
                                    }

                                    AniNotificationType.FollowingNotification -> {
                                        NotificationComponent(
                                            onClick = { onNavigateToActivity(notification.activityId) },
                                            notification = notification,
                                            context = "${notification.userName}${notification.context}",
                                            isUnRead = isUnread
                                        )
                                    }

                                    AniNotificationType.ActivityMessageNotification -> {
                                        NotificationComponent(
                                            onClick = { onNavigateToActivity(notification.activityId) },
                                            notification = notification,
                                            context = "${notification.userName}${notification.context}",
                                            isUnRead = isUnread
                                        )
                                    }

                                    AniNotificationType.ActivityMentionNotification -> {
                                        NotificationComponent(
                                            onClick = { onNavigateToActivity(notification.activityId) },
                                            notification = notification,
                                            context = "${notification.userName}${notification.context}",
                                            isUnRead = isUnread
                                        )
                                    }

                                    AniNotificationType.ActivityReplyNotification -> {
                                        NotificationComponent(
                                            onClick = { onNavigateToActivity(notification.activityId) },
                                            notification = notification,
                                            context = "${notification.userName}${notification.context}",
                                            isUnRead = isUnread
                                        )
                                    }

                                    AniNotificationType.ActivityReplySubscribedNotification -> {
                                        NotificationComponent(
                                            onClick = { onNavigateToActivity(notification.activityId) },
                                            notification = notification,
                                            context = "${notification.userName}${notification.context}",
                                            isUnRead = isUnread
                                        )
                                    }

                                    AniNotificationType.ActivityLikeNotification -> {
                                        NotificationComponent(
                                            onClick = { onNavigateToActivity(notification.activityId) },
                                            notification = notification,
                                            context = "${notification.userName}${notification.context}",
                                            isUnRead = isUnread
                                        )
                                    }

                                    AniNotificationType.ActivityReplyLikeNotification -> {
                                        NotificationComponent(
                                            onClick = { onNavigateToActivity(notification.activityId) },
                                            notification = notification,
                                            context = "${notification.userName}${notification.context}",
                                            isUnRead = isUnread
                                        )
                                    }

                                    AniNotificationType.ThreadCommentMentionNotification -> {
                                        NotificationComponent(
                                            onClick = { onNavigateToThreadComment(notification.threadCommentId) },
                                            notification = notification,
                                            context = "${notification.userName}${notification.context}",
                                            isUnRead = isUnread
                                        )
                                    }

                                    AniNotificationType.ThreadCommentReplyNotification -> {
                                        NotificationComponent(
                                            onClick = { onNavigateToThreadComment(notification.threadCommentId) },
                                            notification = notification,
                                            context = "${notification.userName}${notification.context}",
                                            isUnRead = isUnread
                                        )
                                    }

                                    AniNotificationType.ThreadCommentSubscribedNotification -> {
                                        NotificationComponent(
                                            onClick = { onNavigateToThreadComment(notification.threadCommentId) },
                                            notification = notification,
                                            context = "${notification.userName}${notification.context}",
                                            isUnRead = isUnread
                                        )
                                    }

                                    AniNotificationType.ThreadCommentLikeNotification -> {
                                        NotificationComponent(
                                            onClick = { onNavigateToThreadComment(notification.threadCommentId) },
                                            notification = notification,
                                            context = "${notification.userName}${notification.context}",
                                            isUnRead = isUnread
                                        )
                                    }

                                    AniNotificationType.ThreadLikeNotification -> { //todo what is being liked?
                                        NotificationComponent(
                                            onClick = { onNavigateToThread(notification.threadId) },
                                            notification = notification,
                                            context = "${notification.userName}${notification.context}",
                                            isUnRead = isUnread
                                            // todo is it always your thread?
                                        )
                                    }

                                    AniNotificationType.RelatedMediaAdditionNotification -> {
                                        NotificationComponent(
                                            { navigateToMediaDetails(notification.mediaId) },
                                            notification,
                                            context = "${notification.mediaTitle}${notification.context}",
                                            isUnRead = isUnread
                                        )
                                    }

                                    AniNotificationType.MediaDataChangeNotification -> {
                                        NotificationComponent(
                                            { navigateToMediaDetails(notification.mediaId) },
                                            notification,
                                            context = "${notification.mediaTitle}${notification.context}. ${notification.mediaChangeReason}",
                                            isUnRead = isUnread
                                        )
                                    }

                                    //todo what does context say
                                    AniNotificationType.MediaMergeNotification -> {
                                        NotificationComponent(
                                            onClick = { navigateToMediaDetails(notification.mediaId) },
                                            notification = notification,
                                            context = "One of the media on your list: ${
                                                buildString {
                                                    notification.deletedMediaTitles.forEachIndexed { index, title ->
                                                        if (index == notification.deletedMediaTitles.lastIndex) {
                                                            append(
                                                                title
                                                            )
                                                        } else {
                                                            append("$title, ")
                                                        }
                                                    }
                                                }
                                            } was merged into ${notification.mediaTitle}${notification.mediaChangeReason.ifBlank { "" }}",
                                            isUnRead = isUnread
                                        )
                                    }

                                    AniNotificationType.MediaDeletionNotification -> {
                                        NotificationComponent(
                                            { /*cannot navigate to delete title*/ },
                                            notification,
                                            context = "${notification.deletedMediaTitle}${notification.context}${notification.mediaChangeReason}",
                                            isUnRead = isUnread
                                        )
                                    }

                                    AniNotificationType.UNKNOWN -> {}
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScrollUpFab(
    fabVisibility: Boolean,
    lazyScrollScope: CoroutineScope,
    topAppBarState: TopAppBarState,
    lazyState: LazyListState
) {
    AnimatedVisibility(
        visible = fabVisibility,
        enter = scaleIn() + fadeIn(tween(100)),
        exit = scaleOut() + fadeOut(tween(200))
    ) {
        FloatingActionButton(onClick = {
            lazyScrollScope.launch {
                topAppBarState.heightOffset = 0f
                lazyState.animateScrollToItem(
                    0
                )
            }
        }) {
            Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Scroll up")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun ScrollUpFabPreview() {
    val topAppBarState = rememberTopAppBarState()
    ScrollUpFab(
        fabVisibility = true,
        lazyScrollScope = rememberCoroutineScope(),
        topAppBarState = topAppBarState,
        lazyState = rememberLazyListState()
    )
}

@Composable
private fun NotificationComponent(
    onClick: () -> Unit,
    notification: AniNotification,
    context: String,
    isUnRead: Boolean
) {
    if (isUnRead) Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh) else Modifier
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (isUnRead) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.PaddingSmall),
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier
//                .then(isUnReadModifier)
//                .fillMaxWidth()
//                .padding(vertical = Dimens.PaddingSmall)
//                .clickable { onClick() }

        ) {
            if (!LocalInspectionMode.current) {
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
                        .height(80.dp)
                        .width(90.dp)
                        .padding(
//                        start = Dimens.PaddingNormal,
                            end = Dimens.PaddingNormal,
//                    bottom = Dimens.PaddingSmall,
//                    top = Dimens.PaddingNormal
                        )
                        .clip(MaterialTheme.shapes.medium),
                )
            }
            Column {
                Text(
                    text = Utils.getRelativeTimeFromNow(notification.createdAt.toLong()),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = context,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
//                if (isUnRead) {
//                    Text(
//                        text = "UNREAD",
//                        style = MaterialTheme.typography.bodyLarge,
//                        color = MaterialTheme.colorScheme.onSurface
//                    )
//                }
//            Text("Type is: ${notification.type}")
//            Text("Context given is ${notification.context}")
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun NotificationComponentPreview() {
    NotificationComponent(
        onClick = { },
        notification = AniNotification(),
        context = "Context",
        isUnRead = true
    )
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
