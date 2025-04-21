package com.kevin.anihome.ui.home

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
// import androidx.compose.material3.PlainTooltipBox
// import androidx.compose.material3.RichTooltipBox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
// import androidx.compose.material3.rememberRichTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.kevin.anihome.R
import com.kevin.anihome.data.models.AniTag
import com.kevin.anihome.data.models.Media
import com.kevin.anihome.data.models.HomeTrendingTypes
import com.kevin.anihome.data.models.AniLikeAbleType
import com.kevin.anihome.ui.Dimens
import com.kevin.anihome.utils.LoadingCircle
import com.kevin.anihome.ui.details.mediadetails.MediaDetailsViewModel
import com.kevin.anihome.ui.home.notifications.UnreadNotificationsViewModel
import com.kevin.anihome.ui.home.search.AniSearchBar
import com.kevin.anihome.utils.AsyncImageRoundedCorners
import com.kevin.anihome.utils.MEDIUM_MEDIA_HEIGHT
import com.kevin.anihome.utils.MEDIUM_MEDIA_WIDTH
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber

private const val CARD_HEIGHT = 240

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    unreadNotificationsViewModel: UnreadNotificationsViewModel,
    mediaDetailsViewModel: MediaDetailsViewModel = hiltViewModel(),
    onNavigateToMediaDetails: (Int) -> Unit,
    onNavigateToNotification: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToCharacterDetails: (Int) -> Unit,
    onNavigateToStaffDetails: (Int) -> Unit,
    navigateToUserDetails: (Int) -> Unit,
    navigateToThreadDetails: (Int) -> Unit,
    navigateToStudioDetails: (Int) -> Unit,
    navigateToOverview: (HomeTrendingTypes) -> Unit,
) {
    val uiState =
        HomeUiStateData(
            pagerTrendingNow = homeViewModel.trendingNowPager.collectAsLazyPagingItems(),
            pagerPopularThisSeason = homeViewModel.popularThisSeasonPager.collectAsLazyPagingItems(),
            pagerUpcomingNextSeason = homeViewModel.upComingNextSeasonPager.collectAsLazyPagingItems(),
            pagerAllTimePopular = homeViewModel.allTimePopularPager.collectAsLazyPagingItems(),
            pagerTop100Anime = homeViewModel.top100AnimePager.collectAsLazyPagingItems(),
            pagerPopularManhwa = homeViewModel.popularManhwaPager.collectAsLazyPagingItems(),
            searchResultsMedia = homeViewModel.searchResultsMedia.collectAsLazyPagingItems(),
            searchResultsCharacter = homeViewModel.searchResultsCharacter.collectAsLazyPagingItems(),
            searchResultsStaff = homeViewModel.searchResultsStaff.collectAsLazyPagingItems(),
            searchResultsStudio = homeViewModel.searchResultsStudio.collectAsLazyPagingItems(),
            searchResultsThread = homeViewModel.searchResultsThread.collectAsLazyPagingItems(),
            searchResultsUser = homeViewModel.searchResultsUser.collectAsLazyPagingItems(),
        )

    val isAnime by homeViewModel.isAnime.collectAsStateWithLifecycle()
    val search by homeViewModel.search.collectAsStateWithLifecycle()
    val searchType by homeViewModel.searchType.collectAsStateWithLifecycle()

    // fixme don't fetch this after every destroyed composition
//    LaunchedEffect(key1 = Unit) { homeViewModel.fetchUnReadNotifications() }
    val unReadNotificationCount by unreadNotificationsViewModel.notificationUnReadCount.collectAsStateWithLifecycle()

    val uiState2 by homeViewModel.uiState.collectAsStateWithLifecycle()
//    val trendingTogethery by homeViewModel.trendingTogetherPager.collectAsLazyPagingItems()

    val tags by homeViewModel.tags.collectAsStateWithLifecycle()
    val genres by homeViewModel.genres.collectAsStateWithLifecycle()

    var active by rememberSaveable {
        mutableStateOf(false)
    }
    val focusRequester by remember { mutableStateOf(FocusRequester()) }
    val columnScrollState = rememberScrollState()
    val columnScrollScope = rememberCoroutineScope()

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        homeViewModel
            .toastMessage
            .collect { message ->
                Toast.makeText(
                    context,
                    message,
                    Toast.LENGTH_SHORT,
                ).show()
            }
    }

    Scaffold(topBar = {
        AniSearchBar(
            uiState = uiState,
            tags = tags,
            genres = genres,
            query = search,
            updateSearch = homeViewModel::setSearch,
            active = active,
            unReadNotificationCount = unReadNotificationCount,
            setActive = { active = it },
            onNavigateToMediaDetails = onNavigateToMediaDetails,
            onNavigateToNotification = onNavigateToNotification,
            onNavigateToSettings = onNavigateToSettings,
            focusRequester = focusRequester,
            selectedChip = searchType,
            setSelectedChipValue = homeViewModel::setMediaSearchType,
            onNavigateToCharacterDetails = onNavigateToCharacterDetails,
            onNavigateToStaffDetails = onNavigateToStaffDetails,
            navigateToUserDetails = navigateToUserDetails,
            navigateToThreadDetails = navigateToThreadDetails,
            navigateToStudioDetails = navigateToStudioDetails,
            toggleFavourite = {
                mediaDetailsViewModel.toggleFavourite(
                    AniLikeAbleType.STUDIO,
                    it,
                )
                uiState.searchResultsStudio.refresh()
            },
            reloadGenres = homeViewModel::fetchGenres,
            reloadTags = homeViewModel::fetchTags,
            filterTagsAndGenres = { text ->
//                tags.filter { tag -> tag.name.startsWith(text, ignoreCase = true) }
//                genres
            },
        )
    }, floatingActionButton = {
        FloatingActionButton(
            onClick = {
                active = true
                focusRequester.requestFocus()
            },
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(id = R.string.search),
            )
        }
    }) {
        // checks if there are any values loaded yet
        // fixme
        if (true/*uiState.pagerTrendingNow.itemCount != 0 || uiState.pagerPopularThisSeason.itemCount != 0 || uiState.pagerUpcomingNextSeason.itemCount != 0 || uiState.pagerAllTimePopular.itemCount != 0 || uiState.pagerTop100Anime.itemCount != 0*/) {
            Column(
                modifier = Modifier.padding(top = it.calculateTopPadding()),
//                    .verticalScroll(rememberScrollState()),
            ) {
                var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
                val options = listOf("Anime", "Manga")
                SingleChoiceSegmentedButtonRow(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                start = Dimens.PaddingNormal,
                                end = Dimens.PaddingNormal,
                                bottom = Dimens.PaddingSmall,
                            ),
                ) {
                    SegmentedButton(
                        shape =
                            SegmentedButtonDefaults.itemShape(
                                index = 0,
                                count = options.size,
                            ),
                        onClick = {
                            columnScrollScope.launch {
                                columnScrollState.animateScrollTo(0)
                            }
                            selectedIndex = 0
                            homeViewModel.setToAnime()
                        },
                        selected = 0 == selectedIndex,
                        icon = { },
                    ) {
                        Text("Anime")
                    }
                    SegmentedButton(
                        shape =
                            SegmentedButtonDefaults.itemShape(
                                index = 1,
                                count = options.size,
                            ),
                        onClick = {
                            columnScrollScope.launch {
                                columnScrollState.animateScrollTo(0)
                            }
                            selectedIndex = 1
                            homeViewModel.setToManga()
                        },
                        selected = 1 == selectedIndex,
                        icon = { },
                    ) {
                        Text("Manga")
                    }
                }

                Column(modifier = Modifier.verticalScroll(columnScrollState)) {
                    HeadlineText(
                        text = stringResource(R.string.trending_now),
                        onNavigateToOverview = { navigateToOverview(HomeTrendingTypes.TRENDING_NOW) },
                    )
                    LazyRowLazyPagingItems(uiState.pagerTrendingNow, onNavigateToMediaDetails)

                    if (isAnime) {
                        HeadlineText(
                            text = stringResource(R.string.popular_this_season),
                            onNavigateToOverview = { navigateToOverview(HomeTrendingTypes.POPULAR_THIS_SEASON) },
                        )
                        LazyRowLazyPagingItems(
                            uiState.pagerPopularThisSeason,
                            onNavigateToMediaDetails,
                        )
                        HeadlineText(
                            text = stringResource(R.string.upcoming_next_season),
                            onNavigateToOverview = { navigateToOverview(HomeTrendingTypes.UPCOMING_NEXT_SEASON) },
                        )
                        Timber.d(uiState.pagerUpcomingNextSeason.itemCount.toString())
                        LazyRowLazyPagingItems(
                            uiState.pagerUpcomingNextSeason,
                            onNavigateToMediaDetails,
                        )
                    }

                    if (!isAnime) {
                        HeadlineText(
                            text = stringResource(id = R.string.popular_manhwa),
                            onNavigateToOverview = { navigateToOverview(HomeTrendingTypes.POPULAR_MANHWA) },
                        )
                        LazyRowLazyPagingItems(
                            pager = uiState.pagerPopularManhwa,
                            onNavigateToMediaDetails = onNavigateToMediaDetails,
                        )
                    }

                    HeadlineText(
                        text = stringResource(R.string.all_time_popular),
                        onNavigateToOverview = { navigateToOverview(HomeTrendingTypes.ALL_TIME_POPULAR) },
                    )
                    LazyRowLazyPagingItems(uiState.pagerAllTimePopular, onNavigateToMediaDetails)

                    HeadlineText(
                        text = stringResource(if (isAnime) R.string.top_100_anime else R.string.top_100_manga),
                        onNavigateToOverview = { navigateToOverview(HomeTrendingTypes.TOP_100_ANIME) },
                    )
                    LazyRowLazyPagingItems(uiState.pagerTop100Anime, onNavigateToMediaDetails)
                }
            }
        } else {
            LoadingCircle()
        }
    }
}

@Composable
fun HeadlineText(
    text: String,
    onNavigateToOverview: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(12.dp),
        )
        IconButton(onClick = onNavigateToOverview) {
            Icon(imageVector = Icons.Outlined.ArrowForward, contentDescription = "anime overview")
        }
    }
}

@Composable
fun LazyRowLazyPagingItems(
    pager: LazyPagingItems<Media>,
    onNavigateToMediaDetails: (Int) -> Unit,
) {
    when (pager.loadState.refresh) {
        is LoadState.Error -> {
            Column(
                modifier =
                    Modifier
                        .height(CARD_HEIGHT.dp)
                        .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Network error, please reload",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = Dimens.PaddingNormal),
                )
                TextButton(
                    onClick = { pager.retry() },
                    modifier = Modifier.padding(horizontal = Dimens.PaddingNormal),
                ) {
                    Text(text = "Reload")
                }
            }
        }

        is LoadState.Loading -> LoadingCircle(modifier = Modifier.height(240.dp))
        is LoadState.NotLoading -> {
            LazyRow {
                items(count = pager.itemCount) { index ->
                    val item = pager[index]
                    if (item != null) {
                        AnimeCard(
                            title = item.title,
                            coverImage = item.coverImage,
                            onNavigateToDetails = { onNavigateToMediaDetails(item.id) },
                            modifier = Modifier.padding(start = Dimens.PaddingNormal)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimeCard(
    title: String,
    coverImage: String,
    onNavigateToDetails: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            Modifier
                .width(MEDIUM_MEDIA_WIDTH.dp)
                .then(modifier)
                .clickable { onNavigateToDetails() }
                .semantics(mergeDescendants = true) {},
    ) {
        AsyncImageRoundedCorners(
            coverImage = coverImage,
            contentDescription = title,
            width = MEDIUM_MEDIA_WIDTH.dp,
            height = MEDIUM_MEDIA_HEIGHT.dp,
            padding = 0.dp,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = Dimens.PaddingSmall),
            overflow = TextOverflow.Ellipsis,
            maxLines = 2
        )
    }
}

@Composable
fun AnimeRow(
    onNavigateToDetails: (Int) -> Unit,
    animeList: List<Media>,
    loadMoreAnime: () -> Unit,
) {
    val state = rememberLazyListState()
    LazyRow(
        state = state,
    ) {
        items(animeList) { anime ->
            AnimeCard(
                title = anime.title,
                coverImage = anime.coverImage,
                onNavigateToDetails = (
                    {
                        onNavigateToDetails(anime.id)
                    }
                ),
            )
        }
    }

    val needNextPage by remember {
        derivedStateOf {
            val layoutInfo = state.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1
            val buffer = 3
            lastItemIndex > (totalItems - buffer)
        }
    }
    LaunchedEffect(needNextPage) {
        snapshotFlow {
            needNextPage
        }.distinctUntilChanged().collect {
            if (needNextPage) loadMoreAnime()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationBadge(
    unReadNotificationCount: Int,
    onNavigateToNotification: () -> Unit,
) {
    TooltipBox(
        tooltip = { PlainTooltip { Text(text = stringResource(id = R.string.notifications)) } },
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        state = rememberTooltipState(),
    ) {
//        IconButton(onClick = { /*TODO*/ }) {
        BadgedBox(
            modifier = Modifier,
//            .padding(Dimens.PaddingNormal)
//            .clip(CircleShape)
//            .clickable { onNavigateToNotification() }
            badge = {
                if (unReadNotificationCount != 0 && unReadNotificationCount != -1) {
                    Badge {
                        Text(
                            unReadNotificationCount.toString(),
                            modifier =
                                Modifier.semantics {
                                    contentDescription =
                                        "$unReadNotificationCount new notifications"
                                },
                        )
                    }
                }
            },
        ) {
//                        IconButton(onClick = onNavigateToNotification, modifier = Modifier.tooltipTrigger()) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "notifications",
                modifier =
                    Modifier
                        .clip(CircleShape)
                        .clickable { onNavigateToNotification() },
            )
//                        }
//            }
        }
    }
}

@Composable
fun FailedToLoadTagsAndGenres(
    genres: List<String>,
    reloadGenres: () -> Unit,
    tags: List<AniTag>,
    reloadTags: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "Failed to load",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = Dimens.PaddingSmall),
        )
        if (genres.isEmpty()) {
            TextButton(onClick = { reloadGenres() }) {
                Text(text = stringResource(R.string.reload_genres))
            }
        }
        if (tags.isEmpty()) {
            TextButton(onClick = { reloadTags() }) {
                Text(text = stringResource(R.string.reload_tags))
            }
        }
    }
}


@Preview(showBackground = true, group = "Notification")
 @Composable
 fun NotificationBadgePreview() {
    NotificationBadge(unReadNotificationCount = 2, onNavigateToNotification = {})
 }

