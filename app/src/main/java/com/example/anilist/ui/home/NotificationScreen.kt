package com.example.anilist.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.livedata.observeAsState
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.anilist.R
import com.example.anilist.utils.Utils
import com.example.anilist.data.models.Notification
import com.example.anilist.ui.Dimens

@Composable
fun NotificationScreen(homeViewModel: HomeViewModel = hiltViewModel(), onNavigateBack: () -> Unit) {
    val notifications = homeViewModel.notifications.observeAsState()
    Notifications(
        notifications.value?.data ?: emptyList(),
        homeViewModel::markAllNotificationsAsRead,
        onNavigateBack = onNavigateBack,
    )
}

enum class FilterList {
    ALL,
    AIRING,
    ACTIVITY,
    FORUM,
    FOLLOWS,
    MEDIA,
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
private fun Notifications(
    notifications: List<Notification>,
    markAllAsRead: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    var currentIndex by remember {
        mutableStateOf(FilterList.ALL)
    }
    val filterList = FilterList.values()
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
        Column(modifier = Modifier.padding(top = padding.calculateTopPadding())) {
            FlowRow(modifier = Modifier.padding(Dimens.PaddingNormal)) {
                filterList.forEachIndexed { _, filter ->
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
                        label = { Text(text = filter.name) },
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
                    notifications.filter {
                        when (currentIndex) {
                            FilterList.ALL -> {
                                true
                            }

                            FilterList.AIRING -> {
                                it.type == "AiringNotification"
                            }

                            FilterList.ACTIVITY -> {
                                it.type == "Activity"
                            }

                            FilterList.FORUM -> {
                                it.type == "ThreadCommentSubscribedNotification"
                            }

                            FilterList.FOLLOWS -> {
                                it.type == "Follows"
                            }

                            FilterList.MEDIA -> {
                                it.type == "Media"
                            }
                        }
                    },
                ) {
                    Column {
                        Row {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(it.image)
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
                                    text = Utils.getRelativeTime(it.createdAt.toLong()),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = "Episode ${it.airedEpisode} of ${it.title} aired",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text("Type is: ${it.type}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(locale = "nl")
@Composable
fun NotificationScreenPreview() {
    Notifications(
        notifications = listOf(
            Notification(
                type = "Airing",
                airedEpisode = 2,
                title = "时光代理人 第二季",
                createdAt = 1689303604,
                image = "",
            ),
        ),
        markAllAsRead = { },
        onNavigateBack = { },
    )
}
