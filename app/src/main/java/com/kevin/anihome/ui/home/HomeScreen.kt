package com.kevin.anihome.ui.home

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.RichTooltip
// import androidx.compose.material3.PlainTooltipBox
// import androidx.compose.material3.RichTooltipBox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kevin.anihome.R
import com.kevin.anihome.data.models.AniMediaStatus
import com.kevin.anihome.data.models.AniStudio
import com.kevin.anihome.data.models.AniTag
import com.kevin.anihome.data.models.Media
import com.kevin.anihome.data.models.AniMediaType
import com.kevin.anihome.data.models.AniSeason
import com.kevin.anihome.data.models.HomeTrendingTypes
import com.kevin.anihome.data.models.AniLikeAbleType
import com.kevin.anihome.ui.Dimens
import com.kevin.anihome.utils.LoadingCircle
import com.kevin.anihome.ui.details.mediadetails.MediaDetailsViewModel
import com.kevin.anihome.ui.details.mediadetails.QuickInfo
import com.kevin.anihome.ui.home.notifications.UnreadNotificationsViewModel
import com.kevin.anihome.utils.AsyncImageRoundedCorners
import com.kevin.anihome.utils.Utils
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import okhttp3.internal.toImmutableList
import timber.log.Timber
import java.util.Locale

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
                    // TODO does this work well?
//                    SegmentedButton(shape = SegmentedButtonDefaults.shape(
//                        position = 1, count = options.size
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
                .padding(start = 12.dp)
                .width(120.dp)
                .height(240.dp)
                .then(modifier)
                .clickable { onNavigateToDetails() }
                .semantics(mergeDescendants = true) {},
    ) {
        AsyncImage(
            model =
                ImageRequest.Builder(LocalContext.current).data(coverImage).crossfade(true)
                    .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp)),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(10.dp),
            overflow = TextOverflow.Ellipsis,
        )
    }
}

enum class SearchFilter {
    MEDIA,
    ANIME,
    MANGA,
    CHARACTERS,
    STAFF,
    STUDIOS,
    THREADS,
    USER,
    ;

    override fun toString(): String {
        return this.name.lowercase()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    fun getIconResource(): Int {
        return when (this) {
            MEDIA -> R.drawable.outline_description_24
            ANIME -> R.drawable.navigation_anime_outlined
            MANGA -> R.drawable.navigation_manga_outlined
            CHARACTERS -> R.drawable.baseline_face_24
            STAFF -> R.drawable.outline_group_24
            STUDIOS -> R.drawable.baseline_apartment_24
            THREADS -> R.drawable.navigation_forum_outlined
            USER -> R.drawable.outline_person_24
        }
    }
}

enum class AniMediaSort {
    SEARCH_MATCH,
    START_DATE,
    END_DATE,
    SCORE,
    POPULARITY,
    TRENDING,
    EPISODES,
    DURATION,
    CHAPTERS,
    VOLUMES,
    FAVOURITES,
    ;

    fun toString(context: Context): String {
        return when (this) {
            SEARCH_MATCH -> context.getString(R.string.search_match)
            START_DATE -> context.getString(R.string.start_date)
            END_DATE -> context.getString(R.string.end_date)
            SCORE -> context.getString(R.string.score)
            POPULARITY -> context.getString(R.string.popularity)
            TRENDING -> context.getString(R.string.trending)
            EPISODES -> context.getString(R.string.episode_amount) // fixme also is chapter amount?
            DURATION -> context.getString(R.string.duration)
            CHAPTERS -> context.getString(R.string.chapter_amount)
            VOLUMES -> context.getString(R.string.volume_amount)
            FAVOURITES -> context.getString(R.string.favourites)
        }
    }
}

enum class AniCharacterSort {
    DEFAULT,

    //    RELEVANCE,
//    ROLE,
//    ROLE_DESC,
    FAVOURITES,
    FAVOURITES_DESC,
    ;

    fun toString(context: Context): String {
        return when (this) {
            DEFAULT -> context.getString(R.string.default_sort)
            FAVOURITES -> context.getString(R.string.favourites)
            FAVOURITES_DESC -> context.getString(R.string.favourites_DESC)
        }
    }
}

@Composable
fun GenreCheckBox(
    selectedGenres: MutableList<String>,
    genre: String,
) {
    var checked by remember {
        mutableStateOf(selectedGenres.contains(genre))
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {
                    if (checked) {
                        checked = false
                        selectedGenres.remove(genre)
                    } else {
                        checked = true
                        selectedGenres.add(genre)
                    }
                },
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = {
                if (it) {
                    checked = true
                    selectedGenres.add(genre)
                } else {
                    checked = false
                    selectedGenres.remove(genre)
                }
                Timber.d(
                    "Checkbox change in tag: parameter is $it\n list of tags is $selectedGenres\n check status is $checked",
                )
            },
        )
        Text(
            text = genre,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class,
    ExperimentalFoundationApi::class,
)
fun AniSearchBar(
//    characterSort: AniCharacterSort,
//    setCharacterSort: (AniCharacterSort) -> Unit,
    uiState: HomeUiStateData,
    query: String,
    updateSearch: (searchState: MediaSearchState) -> Unit,
    active: Boolean,
    unReadNotificationCount: Int,
    setActive: (Boolean) -> Unit,
    focusRequester: FocusRequester,
    selectedChip: SearchFilter,
    setSelectedChipValue: (SearchFilter) -> Unit,
    toggleFavourite: (Int) -> Unit,
    reloadTags: () -> Unit,
    reloadGenres: () -> Unit,
    onNavigateToMediaDetails: (Int) -> Unit,
    onNavigateToNotification: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToCharacterDetails: (Int) -> Unit,
    navigateToUserDetails: (Int) -> Unit,
    onNavigateToStaffDetails: (Int) -> Unit,
    navigateToThreadDetails: (Int) -> Unit,
    navigateToStudioDetails: (Int) -> Unit,
    tags: List<AniTag>,
    genres: List<String>,
    filterTagsAndGenres: (String) -> Unit,
) {
    val padding by animateDpAsState(
        targetValue = if (!active) Dimens.PaddingNormal else 0.dp,
        label = "increase padding" + "",
    )
    val keyboardController = LocalSoftwareKeyboardController.current

    var currentMediaSort by rememberSaveable {
        mutableStateOf(AniMediaSort.POPULARITY)
    }

    var currentCharacterSort by rememberSaveable {
        mutableStateOf(AniCharacterSort.FAVOURITES_DESC)
    }

    var selectedSeason by rememberSaveable { mutableStateOf(AniSeason.UNKNOWN) }
    var selectedYear by rememberSaveable { mutableIntStateOf(-1) }
    var selectedStatus by rememberSaveable { mutableStateOf(AniMediaStatus.UNKNOWN) }
    val selectedGenres: MutableList<String> by rememberSaveable { mutableStateOf(mutableListOf()) } // fixme warning?
    val selectedTags: MutableList<String> by rememberSaveable {
        mutableStateOf(mutableListOf()) // fixme warning?
    }
    var showOnlyOnMyList by rememberSaveable {
        mutableStateOf(false)
    }

    val updateSearchParameterless: (String) -> Unit = {
        updateSearch(
            MediaSearchState(
                query = it,
                searchType = selectedChip,
                mediaSort = currentMediaSort,
                characterSort = currentCharacterSort,
                currentSeason = selectedSeason,
                status = selectedStatus,
                year = selectedYear,
                genres = selectedGenres.toImmutableList(),
                tags = selectedTags.toImmutableList(),
                onlyOnMyList = showOnlyOnMyList,
            ),
        )
    }

    SearchBar(
        query = query,
        onQueryChange = { updateSearchParameterless(it) },
        onSearch = {
            updateSearchParameterless(it)
            keyboardController?.hide()
        },
        active = active,
        onActiveChange = {
            setActive(it)
        },
        placeholder = {
            Text(text = "Search for Anime, Manga...")
        },
        leadingIcon = {
            if (active) {
                IconButton(onClick = { setActive(false) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(id = R.string.back),
                    )
                }
            } else {
                Box {
                    TooltipBox(
                        tooltip = { PlainTooltip { Text(text = stringResource(R.string.settings)) } },
                        modifier = Modifier.align(Alignment.BottomCenter),
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                        state = rememberTooltipState(),
                    ) {
                        IconButton(
                            onClick = onNavigateToSettings,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = stringResource(R.string.settings),
                            )
                        }
                    }
                }
            }
        },
        trailingIcon = {
            if (!active) {
                NotificationBadge(unReadNotificationCount, onNavigateToNotification)
            } else if (query == "") {
                Icon(
                    painterResource(id = R.drawable.baseline_search_24),
                    "Search",
                    modifier = Modifier.padding(end = 16.dp),
                )
            } else {
                TooltipBox(
                    tooltip = { PlainTooltip { Text(text = "Clear text") } },
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    state = rememberTooltipState(),
                ) {
                    IconButton(
                        onClick = { updateSearchParameterless("") },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(R.string.clear),
                        )
                    }
                }
            }
        },
        modifier =
            Modifier
                .animateContentSize()
                .fillMaxWidth()
                .padding(start = padding, end = padding, top = padding, bottom = padding)
                .focusRequester(focusRequester),
    ) {
        Box {
            Column {
                val filterList = SearchFilter.values()
                LazyRow(modifier = Modifier.padding(top = Dimens.PaddingSmall)) {
                    itemsIndexed(filterList) { index, filterName ->
                        FilterChip(
                            selected = index == selectedChip.ordinal,
                            onClick = {
                                setSelectedChipValue(filterList[index]) // fixme
//                            updateSearchParameterless(query)
                            },
                            label = { Text(text = filterName.toString()) },
                            leadingIcon = {
                                if (index == selectedChip.ordinal) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(id = filterName.getIconResource()),
                                        contentDescription = null,
                                    )
                                }
                            },
                            modifier =
                                Modifier.padding(
                                    start = Dimens.PaddingNormal,
                                    end =
                                        if (index == filterList.lastIndex) {
                                            Dimens.PaddingNormal
                                        } else {
                                            0.dp
                                        },
                                ),
                        )
                    }
                }
                var showSortingBottomSheet by remember { mutableStateOf(false) }
                var showGenreDialog by remember { mutableStateOf(false) }
                var showTagDialog by remember { mutableStateOf(false) }
                var showYearDialog by remember { mutableStateOf(false) }
                var showSeasonBottomSheet by remember { mutableStateOf(false) }
                var showAiringStatusBottomSheet by remember { mutableStateOf(false) }

                val context = LocalContext.current
                FlowRow(
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.padding(start = Dimens.PaddingNormal),
                ) {
                    if (selectedChip == SearchFilter.MEDIA || selectedChip == SearchFilter.ANIME || selectedChip == SearchFilter.MANGA) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = Dimens.PaddingSmall))
                        AssistChip(
                            onClick = { showSortingBottomSheet = true },
                            label = { Text(text = currentMediaSort.toString(context)) },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.sort),
                                    contentDescription =
                                        stringResource(
                                            id = R.string.sort,
                                        ),
                                )
                            },
                            modifier = Modifier.padding(end = Dimens.PaddingNormal),
                        )

//                        AssistChip(
//                            onClick = { showGenreDialog = true },
//                            leadingIcon = {
//                                Icon(
//                                    painter = painterResource(id = R.drawable.outline_theater_comedy_24),
//                                    contentDescription = null
//                                )
//                            },
//                            label = { Text(text = "Genre") },
//                            modifier = Modifier.padding(end = Dimens.PaddingNormal)
//                        )

                        AssistChip(
                            onClick = { showTagDialog = true },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.outline_theater_comedy_24),
                                    contentDescription = null,
                                )
                            },
                            label = { Text(text = stringResource(R.string.genres_tags)) },
                            modifier = Modifier.padding(end = Dimens.PaddingNormal),
                        )

                        AssistChip(
                            onClick = { showYearDialog = true },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.anime_details_calendar),
                                    contentDescription = null,
                                )
                            },
                            label = {
                                Text(
                                    text = if (selectedYear == -1) stringResource(id = R.string.year) else selectedYear.toString(),
                                )
                            },
                            modifier = Modifier.padding(end = Dimens.PaddingNormal),
                        )

                        if (selectedChip == SearchFilter.MEDIA || selectedChip == SearchFilter.ANIME) {
                            AssistChip(
                                onClick = { showSeasonBottomSheet = true },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_local_florist_24),
                                        contentDescription = null,
                                    )
                                },
                                label = {
                                    Text(
                                        text =
                                            if (selectedSeason == AniSeason.UNKNOWN) {
                                                stringResource(id = R.string.season)
                                            } else {
                                                selectedSeason.getString(
                                                    context,
                                                )
                                            },
                                    )
                                },
                                modifier = Modifier.padding(end = Dimens.PaddingNormal),
                            )
                        }

                        if (selectedChip == SearchFilter.MEDIA || selectedChip == SearchFilter.ANIME) {
                            AssistChip(
                                onClick = { showAiringStatusBottomSheet = true },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_cast_24),
                                        contentDescription = null,
                                    )
                                },
                                label = {
                                    Text(
                                        text =
                                            if (selectedStatus == AniMediaStatus.UNKNOWN) {
                                                stringResource(R.string.airing_status)
                                            } else {
                                                selectedStatus.toString(context)
                                            },
                                    )
                                },
                                modifier = Modifier.padding(end = Dimens.PaddingNormal),
                            )
                        }

                        // todo change to assist chip
                        FilterChip(
                            onClick = {
                                showOnlyOnMyList = !showOnlyOnMyList
                                updateSearchParameterless(query)
                            },
                            leadingIcon = {
                                if (showOnlyOnMyList) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Check",
                                    )
                                }
                            },
                            selected = showOnlyOnMyList,
//                            {
//                                Icon(
//                                    painter = painterResource(id = R.drawable.anime_details_calendar),
//                                    contentDescription = null
//                                )
//                            },
                            label = { Text("Show only my list") },
                            modifier = Modifier.padding(end = Dimens.PaddingNormal),
                        )
                    } else if (selectedChip == SearchFilter.CHARACTERS) {
                        HorizontalDivider()
                        AssistChip(
                            onClick = { showSortingBottomSheet = true },
                            label = { Text(text = currentCharacterSort.toString(context)) },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.sort),
                                    contentDescription =
                                        stringResource(
                                            id = R.string.sort,
                                        ),
                                )
                            },
                        )
                    }
                }
                if (showSortingBottomSheet) {
                    ModalBottomSheet(onDismissRequest = { showSortingBottomSheet = false }) {
                        if (selectedChip == SearchFilter.MEDIA || selectedChip == SearchFilter.ANIME || selectedChip == SearchFilter.MANGA) {
                            AniMediaSort.values().forEachIndexed { _, mediaSort ->
                                TextButton(
                                    onClick = {
                                        currentMediaSort = mediaSort
                                        updateSearchParameterless(query)
                                        showSortingBottomSheet = false
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RectangleShape,
                                ) {
                                    Text(
                                        mediaSort.toString(context),
                                        color = if (currentMediaSort == mediaSort) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        } else if (selectedChip == SearchFilter.CHARACTERS) {
                            AniCharacterSort.values().forEach { aniCharacterSort ->
                                TextButton(
                                    onClick = {
                                        currentCharacterSort = aniCharacterSort
                                        showSortingBottomSheet = false
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RectangleShape,
                                ) {
                                    Text(
                                        aniCharacterSort.toString(context),
                                        color = if (currentCharacterSort == aniCharacterSort) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                    }
                }
//                if (showGenreDialog) {
//                    AlertDialog(onDismissRequest = { showGenreDialog = false },
//                        dismissButton = {
//                            TextButton(onClick = { showGenreDialog = false }) {
//                                Text(text = "Dismiss")
//                            }
//                        },
//                        confirmButton = {
//                            TextButton(onClick = { showGenreDialog = false }) {
//                                Text(text = "Confirm")
//                            }
//                        },
//                        title = { Text(text = "Genre") },
//                        text = {
//                            LazyColumn {
//                                items(genres) { genre ->
//                                    Text(text = genre)
//                                }
//                            }
//                        })
//                }
                val setShowTagDialog: (Boolean) -> Unit = { showTagDialog = it }
                TagDialog(
                    showTagDialog,
                    selectedTags,
                    selectedGenres,
                    setShowTagDialog,
                    updateSearchParameterless,
                    query,
                    tags,
                    genres,
                    reloadGenres,
                    reloadTags,
                    filterTagsAndGenres,
                )
                if (showYearDialog) {
                    val unchangedYear by remember(key1 = showYearDialog) {
                        mutableIntStateOf(selectedYear)
                    }
                    AlertDialog(onDismissRequest = { showYearDialog = true }, dismissButton = {
                        TextButton(onClick = {
                            selectedYear = unchangedYear
                            showYearDialog = false
                        }) {
                            Text(text = "Close")
                        }
                    }, confirmButton = {
                        TextButton(onClick = {
                            updateSearchParameterless(query)
                            showYearDialog = false
                        }) {
                            Text(text = "Confirm")
                        }
                    }, title = { Text(text = stringResource(id = R.string.year)) }, text = {
                        val yearsRange = 1940..Utils.nextYear()
                        LazyColumn {
                            items(yearsRange.toList().sortedDescending()) { year ->
                                TextButton(
                                    onClick = {
                                        selectedYear =
                                            if (selectedYear == year) {
                                                -1
                                            } else {
                                                year
                                            }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RectangleShape,
                                ) {
                                    Text(
                                        year.toString(),
                                        color =
                                            if (selectedYear == year) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            },
                                    )
                                }
                            }
                        }
                    })
                }
                if (showSeasonBottomSheet) {
                    ModalBottomSheet(onDismissRequest = { showSeasonBottomSheet = false }) {
                        AniSeason.values().forEach { season ->
                            if (season != AniSeason.UNKNOWN) {
                                TextButton(
                                    onClick = {
                                        selectedSeason =
                                            if (selectedSeason == season) {
                                                AniSeason.UNKNOWN
                                            } else {
                                                season
                                            }
                                        updateSearchParameterless(query)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(
                                        text = season.getString(context),
                                        color = if (season == selectedSeason) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                    }
                }
                if (showAiringStatusBottomSheet) {
                    ModalBottomSheet(onDismissRequest = { showAiringStatusBottomSheet = false }) {
                        AniMediaStatus.values().forEach { status ->
                            if (status != AniMediaStatus.UNKNOWN) {
                                TextButton(
                                    onClick = {
                                        selectedStatus =
                                            if (selectedStatus == status) {
                                                AniMediaStatus.UNKNOWN
                                            } else {
                                                status
                                            }
                                        updateSearchParameterless(query)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(
                                        text = status.toString(context),
                                        color =
                                            if (status == selectedStatus) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            },
                                    )
                                }
                            }
                        }
                    }
                }

                SearchResults(
                    uiState = uiState,
                    selectedChip = selectedChip,
                    onNavigateToMediaDetails = onNavigateToMediaDetails,
                    onNavigateToCharacterDetails = onNavigateToCharacterDetails,
                    onNavigateToStaffDetails = onNavigateToStaffDetails,
                    navigateToStudioDetails = navigateToStudioDetails,
                    navigateToThreadDetails = navigateToThreadDetails,
                    navigateToUserDetails = navigateToUserDetails,
                    toggleFavourite = toggleFavourite,
                )
            }
        }
    }
}

@Composable
fun SearchResults(
    uiState: HomeUiStateData,
    selectedChip: SearchFilter,
    onNavigateToMediaDetails: (Int) -> Unit,
    onNavigateToCharacterDetails: (Int) -> Unit,
    onNavigateToStaffDetails: (Int) -> Unit,
    navigateToStudioDetails: (Int) -> Unit,
    navigateToThreadDetails: (Int) -> Unit,
    navigateToUserDetails: (Int) -> Unit,
    toggleFavourite: (Int) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        when (selectedChip) {
            SearchFilter.MEDIA, SearchFilter.ANIME, SearchFilter.MANGA -> {
                val pager = uiState.searchResultsMedia
                Timber.d(
                    "Refresh load-state is: " + pager.loadState.refresh.toString() + "\nappend load state is : ${pager.loadState.append}",
                )
                when (pager.loadState.refresh) {
                    is LoadState.Error -> {
                        Timber.d("Search error: ${(pager.loadState.refresh as LoadState.Error).error.message}")
                        item {
                            SearchResultNetworkError()
                        }
                    }

                    is LoadState.Loading -> {
                        item {
                            LoadingCircle()
                        }
                    }

                    is LoadState.NotLoading -> {
                        if (pager.itemCount != 0) {
                            items(pager.itemCount) { index ->
                                val media = uiState.searchResultsMedia[index]
                                if (media != null) {
                                    SearchCardMedia(
                                        media,
                                        onNavigateToMediaDetails,
                                        media.id,
                                        media.coverImage,
                                        media.title,
                                        media.type,
                                    )
                                }
                            }
                        } else {
                            item {
//                                Text(text = "no results") todo? check for other search results as well
                            }
                        }
                    }
                }
            }

            SearchFilter.CHARACTERS -> {
                val pager = uiState.searchResultsCharacter
                when (pager.loadState.refresh) {
                    is LoadState.Error -> {
                        item {
                            SearchResultNetworkError()
                        }
                    }

                    LoadState.Loading -> {
                        item {
                            LoadingCircle()
                        }
                    }

                    is LoadState.NotLoading -> {
                        items(pager.itemCount) { index ->
                            val character = uiState.searchResultsCharacter[index]
                            if (character != null) {
                                SearchCardCharacter(
                                    onNavigateToCharacterDetails,
                                    character.id,
                                    character.coverImage,
                                    character.userPreferredName,
                                    character.favorites,
                                    character.isFavourite,
                                )
                            }
                        }
                    }
                }
            }

            SearchFilter.STAFF -> {
                val pager = uiState.searchResultsStaff
                when (pager.loadState.refresh) {
                    is LoadState.Error -> {
                        item {
                            SearchResultNetworkError()
                        }
                    }

                    LoadState.Loading -> {
                        item {
                            LoadingCircle()
                        }
                    }

                    is LoadState.NotLoading -> {
                        items(pager.itemCount) { index ->
                            val staff = uiState.searchResultsStaff[index]
                            if (staff != null) {
                                SearchCardCharacter(
                                    onNavigateToStaffDetails,
                                    staff.id,
                                    staff.coverImage,
                                    staff.userPreferredName,
                                    staff.favourites,
                                    staff.isFavourite,
                                )
                            }
                        }
                    }
                }
            }

            SearchFilter.STUDIOS -> {
                val pager = uiState.searchResultsStudio
                when (pager.loadState.refresh) {
                    is LoadState.Error -> {
                        item {
                            SearchResultNetworkError()
                        }
                    }

                    LoadState.Loading -> {
                        item {
                            LoadingCircle()
                        }
                    }

                    is LoadState.NotLoading -> {
                        items(uiState.searchResultsStudio.itemCount) { index ->
                            val studio = uiState.searchResultsStudio[index]
                            if (studio != null) {
                                SearchCardStudio(
                                    studio,
                                    navigateToStudioDetails,
                                    toggleFavourite,
                                )
                            }
                        }
                    }
                }
            }

            SearchFilter.THREADS -> {
                val pager = uiState.searchResultsThread
                when (pager.loadState.refresh) {
                    is LoadState.Error -> {
                        item {
                            SearchResultNetworkError()
                        }
                    }

                    LoadState.Loading -> {
                        item {
                            LoadingCircle()
                        }
                    }

                    is LoadState.NotLoading -> {
                        items(uiState.searchResultsThread.itemCount) { index ->
                            val thread = uiState.searchResultsThread[index]
                            if (thread != null) {
                                SearchCardForum(
                                    thread.id,
                                    thread.title,
                                    navigateToThreadDetails,
                                )
                            }
                        }
                    }
                }
            }

            SearchFilter.USER -> {
                val pager = uiState.searchResultsUser
                when (pager.loadState.refresh) {
                    is LoadState.Error -> {
                        item {
                            SearchResultNetworkError()
                        }
                    }

                    LoadState.Loading -> {
                        item {
                            LoadingCircle()
                        }
                    }

                    is LoadState.NotLoading -> {
                        items(uiState.searchResultsUser.itemCount) { index ->
                            val user = uiState.searchResultsUser[index]
                            if (user != null) {
                                SearchCardUser(
                                    user.id,
                                    user.name,
                                    navigateToUserDetails,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultNetworkError() {
    Box(contentAlignment = Alignment.TopCenter) {
        Text(
            text = "Network error! Please try searching again.",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(Dimens.PaddingNormal),
        )
    }
}

@Composable
fun SearchCardUser(
    id: Int,
    name: String,
    navigateToUserDetails: (Int) -> Unit,
) {
    Column(modifier = Modifier.clickable { navigateToUserDetails(id) }) {
        Text(text = id.toString())
        Text(text = name)
    }
}

@Composable
fun SearchCardForum(
    id: Int,
    title: String,
    navigateToThreadDetails: (Int) -> Unit,
) {
    Column(modifier = Modifier.clickable { navigateToThreadDetails(id) }) {
        Text(text = id.toString())
        Text(text = title)
    }
}

@Composable
fun SearchCardStudio(
    studio: AniStudio,
    navigateToStudioDetails: (Int) -> Unit,
    toggleFavourite: (Int) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { navigateToStudioDetails(studio.id) }
                .padding(Dimens.PaddingNormal),
    ) {
        Text(
            text = studio.name,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = if (studio.favourites == -1) "?" else studio.favourites.toString(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = Dimens.PaddingSmall),
        )
        Icon(
            painter = painterResource(id = if (!studio.isFavourite) R.drawable.anime_details_heart else R.drawable.baseline_favorite_24),
            contentDescription = "Favourites",
            modifier =
                Modifier.clickable {
                    toggleFavourite(studio.id)
                },
        )
    }
}

@Composable
fun SearchCardCharacter(
    onNavigateToDetails: (Int) -> Unit,
    id: Int,
    coverImage: String,
    userPreferredName: String,
    favourites: Int,
    isFavourite: Boolean,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onNavigateToDetails(id) },
    ) {
        AsyncImageRoundedCorners(coverImage = coverImage, contentDescription = userPreferredName)
        Column(modifier = Modifier.padding(Dimens.PaddingNormal)) {
            Text(
                text = userPreferredName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = Dimens.PaddingSmall),
            ) {
                Text(
                    text = favourites.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(end = Dimens.PaddingSmall),
                )
                Icon(
                    painter = painterResource(id = if (!isFavourite) R.drawable.anime_details_heart else R.drawable.baseline_favorite_24),
                    contentDescription = "Favourite",
                )
            }
        }
    }
}

@Composable
fun SearchCardMedia(
    media: Media,
    onNavigateToDetails: (Int) -> Unit,
    id: Int,
    coverImage: String,
    title: String,
    mediaType: AniMediaType,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onNavigateToDetails(id) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model =
                ImageRequest.Builder(LocalContext.current).data(coverImage).crossfade(true)
                    .build(),
            contentDescription = "Cover of $title",
            placeholder = painterResource(id = R.drawable.no_image),
            fallback = painterResource(id = R.drawable.no_image),
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .height(175.dp)
                    .width(125.dp)
                    .padding(
                        start = Dimens.PaddingNormal,
                        top = Dimens.PaddingSmall,
                        bottom = Dimens.PaddingSmall,
                    )
                    .clip(RoundedCornerShape(12.dp)),
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier =
                    Modifier.padding(
                        start = Dimens.PaddingNormal,
                        end = Dimens.PaddingNormal,
                        top = Dimens.PaddingNormal,
                    ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            QuickInfo(media = media, isAnime = mediaType == AniMediaType.ANIME)
        }
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
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class,
)
fun TagDialog(
    showTagDialog: Boolean,
    selectedTags: MutableList<String>,
    selectedGenres: MutableList<String>,
    setShowTagDialog: (Boolean) -> Unit,
    updateSearchParameterless: (String) -> Unit,
    query: String,
    tags: List<AniTag>,
    genres: List<String>,
    reloadGenres: () -> Unit,
    reloadTags: () -> Unit,
    filterTagsAndGenres: (String) -> Unit,
) {
    if (showTagDialog) {
        var filterTextFieldValue by remember { mutableStateOf("") }
        val unChangedTags by remember(key1 = showTagDialog) {
            mutableStateOf(selectedTags.toImmutableList())
        }
        val unChangedGenres by remember(key1 = showTagDialog) {
            mutableStateOf(selectedGenres.toImmutableList())
        }
        val filteredTags by remember(key1 = filterTextFieldValue) {
            mutableStateOf(
                tags.filter { tag ->
                    tag.name.startsWith(
                        filterTextFieldValue,
                        ignoreCase = true,
                    )
                },
            )
        }
        val filteredGenres by remember(key1 = filterTextFieldValue) {
            mutableStateOf(
                genres.filter { name ->
                    name.startsWith(
                        filterTextFieldValue,
                        ignoreCase = true,
                    )
                },
            )
        }

        AlertDialog(
            onDismissRequest = { setShowTagDialog(false) },
            shape = RectangleShape,
            properties =
                DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false,
                ),
            dismissButton = {
                TextButton(onClick = {
                    selectedTags.apply {
                        clear()
                        addAll(unChangedTags)
                    }
                    selectedGenres.apply {
                        clear()
                        addAll(unChangedGenres)
                    }
                    setShowTagDialog(false)
                }) {
                    Text(text = stringResource(id = R.string.close))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    setShowTagDialog(false)
                    updateSearchParameterless(query)
                }) {
                    Text(text = stringResource(id = R.string.save))
                }
            },
            title = { Text(text = stringResource(id = R.string.genres_tags)) },
            text = {
                if (tags.isNotEmpty() && genres.isNotEmpty()) {
                    Column {
                        OutlinedTextField(
                            value = filterTextFieldValue,
                            onValueChange = {
                                filterTextFieldValue = it
                                filterTagsAndGenres(it)
                            },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(Dimens.PaddingNormal),
                        )
                        FlowRow {
//                            val chosenTags by remember(key1 = selectedTags, key2 = selectedGenres) {
//                                mutableStateOf(selectedTags.apply { addAll(selectedGenres) }.toImmutableList())
//                            }
                            selectedTags.forEach {
                                InputChip(
                                    selected = false,
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear",
                                        )
                                    },
                                    onClick = {
                                        selectedTags.remove(it); // selectedGenres.remove(it)
                                    },
                                    label = { Text(text = it) },
                                )
                            }
                        }
                        TextButton(onClick = {
                            selectedTags.clear()
                            selectedGenres.clear()
                        }) {
                            Text(text = "Clear filters")
                        }
                        LazyColumn {
                            val alphabeticalListOfTagCategories =
                                filteredTags.map { it.category }.distinct().sorted()
                            Timber.d(alphabeticalListOfTagCategories.toString())

                            if (filteredGenres.isNotEmpty()) {
                                stickyHeader {
                                    Surface(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = stringResource(id = R.string.genres),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(bottom = Dimens.PaddingSmall),
                                        )
                                    }
                                }
                                items(filteredGenres) { genre ->
                                    GenreCheckBox(
                                        selectedGenres = selectedGenres,
                                        genre = genre,
                                    )
                                }
                            }

                            alphabeticalListOfTagCategories.forEach { category ->
                                stickyHeader {
                                    Surface(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = category,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(bottom = Dimens.PaddingSmall),
                                        )
                                    }
                                }
                                items(filteredTags.filter { tag -> tag.category == category }) { aniTag ->
                                    TagCheckBox(selectedTags = selectedTags, tag = aniTag)
                                }
                            }
                        }
                    }
                } else {
                    FailedToLoadTagsAndGenres(genres, reloadGenres, tags, reloadTags)
                }
            },
        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagCheckBox(
    selectedTags: MutableList<String>,
    tag: AniTag,
) {
    var checked by remember(key1 = selectedTags) {
        mutableStateOf(
            selectedTags.contains(
                tag.name,
            ),
        )
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {
                    if (checked) {
                        checked = false
                        selectedTags.remove(tag.name)
                    } else {
                        checked = true
                        selectedTags.add(tag.name)
                    }
                },
    ) {
//        var showTagDescription by remember {
//            mutableStateOf(
//                false
//            )
//        }
        Checkbox(
            checked = checked,
            onCheckedChange = {
                if (it) {
                    checked = true
                    selectedTags.add(tag.name)
                } else {
                    checked = false
                    selectedTags.remove(tag.name)
                }
                Timber.d(
                    "Checkbox change in tag: parameter is $it\n list of tags is $selectedTags\n check status is $checked",
                )
            },
        )
        Text(
            text = tag.name,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
//        val tooltipState = rememberTooltipState(isPersistent = true)
        val scope = rememberCoroutineScope()
        TooltipBox(
            positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
            state = rememberTooltipState(isPersistent = true),
            tooltip = { RichTooltip(title = { Text(text = tag.name) }) { Text(text = tag.description) } },
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = stringResource(R.string.tag_info),
            )
        }
    }
}

// @Preview(showBackground = true)
// @Composable
// fun SearchCardStudioPreview() {
//    SearchCardStudio(AniStudio(id = 12, name = "Tokyo TV", favourites = 2123),
//        navigateToStudioDetails = { },
//        toggleFavourite = { })
// }
//
// @Preview(showBackground = true)
// @Composable
// fun SearchCardCharacterPreview() {
//    SearchCardCharacter(
//        onNavigateToDetails = {},
//        id = 12,
//        coverImage = "",
//        userPreferredName = "Jabami Yumeko",
//        favourites = 1223,
//        isFavourite = true
//    )
// }
//
// @Preview(showBackground = true, group = "Notification")
// @Composable
// fun NotificationBadgePreview() {
//    NotificationBadge(unReadNotificationCount = 2, onNavigateToNotification = {})
// }

// @Preview
// @Composable
// fun TagDialogPreview() {
//    TagDialog(
//        showTagDialog = true,
//        selectedTags = mutableListOf(),
//        selectedGenres = mutableListOf(),
//        setShowTagDialog = {},
//        updateSearchParameterless = {},
//        query = "Demon",
//        tags = mutableListOf(
//            AniTag(
//                id = 12321,
//                name = "Boy's love",
//                category = "Romance",
//                description = "Content involving male gay relationships",
//                isAdult = false
//            ), AniTag(
//                id = 12321,
//                name = "Yuri",
//                category = "Romance",
//                description = "Content involving lesbian relationships",
//                isAdult = false
//            ),
//            AniTag(
//                id = 1421,
//                name = "Drawing",
//                category = "Theme / Arts",
//                description = "Centers around the art of drawign",
//                isAdult = false
//            )
//        ),
//        genres = mutableListOf("Drama"),
//        reloadGenres = { },
//        reloadTags = { }) {
//
//    }
// }
