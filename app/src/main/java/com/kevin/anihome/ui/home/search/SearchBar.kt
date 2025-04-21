package com.kevin.anihome.ui.home.search

import android.content.Context
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kevin.anihome.R
import com.kevin.anihome.data.models.AniMediaStatus
import com.kevin.anihome.data.models.AniMediaType
import com.kevin.anihome.data.models.AniSeason
import com.kevin.anihome.data.models.AniStudio
import com.kevin.anihome.data.models.AniTag
import com.kevin.anihome.data.models.Media
import com.kevin.anihome.ui.Dimens
import com.kevin.anihome.ui.details.mediadetails.QuickInfo
import com.kevin.anihome.ui.home.FailedToLoadTagsAndGenres
import com.kevin.anihome.ui.home.HomeUiStateData
import com.kevin.anihome.ui.home.MediaSearchState
import com.kevin.anihome.ui.home.NotificationBadge
import com.kevin.anihome.utils.AsyncImageRoundedCorners
import com.kevin.anihome.utils.LoadingCircle
import com.kevin.anihome.utils.MEDIUM_MEDIA_HEIGHT
import com.kevin.anihome.utils.MEDIUM_MEDIA_WIDTH
import com.kevin.anihome.utils.Utils
import kotlinx.coroutines.launch
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import okhttp3.internal.toImmutableList
import timber.log.Timber
import java.util.Locale

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
    // TODO add these as well? https://studio.apollographql.com/sandbox/schema/reference/enums/MediaSort
//    STATUS,
//    TITLE_ENGLISH,
//    TITLE_NATIVE,
//    TITLE_ROMAJI,
//    TYPE,
//    UPDATED_AT,
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
//    RELEVANCE,
//    ROLE,
//    ROLE_DESC,
//    FAVOURITES,
    FAVOURITES_DESC,
    SEARCH_MATCH
    ;

    fun toString(context: Context): String {
        return when (this) {
            FAVOURITES_DESC -> context.getString(R.string.favourites)
            SEARCH_MATCH -> context.getString(
                R.string.search_match
            )
//            FAVOURITES -> context.getString(R.string.favourites)
//            RELEVANCE -> context.getString(R.string.relevance)
//            ROLE -> context.getString(R.string.role)
//            ROLE_DESC -> context.getString(R.string.role_desc)
        }
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

    val inputField =
        @Composable {
            SearchBarDefaults.InputField(
                query = query,
                modifier = Modifier,
                expanded = active,
                onExpandedChange = { setActive(it) },
                onSearch = { keyboardController?.hide(); updateSearchParameterless(it) },
                placeholder = { Text(text = stringResource(R.string.search_for_anime_manga)) },
                onQueryChange = { updateSearchParameterless(it) },
                leadingIcon = {
                    if (active) {
                        IconButton(onClick = { setActive(false) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(id = R.string.back),
                            )
                        }
                    } else {
                        Box {
                            TooltipBox(
                                tooltip = { PlainTooltip { Text(text = stringResource(R.string.settings)) } },
                                modifier = Modifier.Companion.align(Alignment.Companion.BottomCenter),
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
                            modifier = Modifier.Companion.padding(end = 16.dp),
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
            )
        }
    SearchBar(
        inputField = inputField,
        expanded = active,
        onExpandedChange = {},
        modifier =
            Modifier.Companion
                .animateContentSize()
                .fillMaxWidth()
                .padding(start = padding, end = padding, top = padding, bottom = padding)
                .focusRequester(focusRequester),
    ) {
        Box {
            Column {
                val filterList = SearchFilter.entries.toTypedArray()
                LazyRow(modifier = Modifier.Companion.padding(top = Dimens.PaddingSmall)) {
                    itemsIndexed(filterList) { index, filterName ->
                        FilterChip(
                            selected = index == selectedChip.ordinal,
                            onClick = {
                                setSelectedChipValue(filterList[index])
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
                                Modifier.Companion.padding(
                                    start = if (index == 0) {
                                        Dimens.PaddingNormal
                                    } else {
                                        Dimens.PaddingSmall
                                    },
                                    end =
                                        if (index == filterList.lastIndex) {
                                            Dimens.PaddingSmall
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
                var yearChipSelected by remember { mutableStateOf(false) }
                var showYearDialog by remember { mutableStateOf(false) }
                var showSeasonBottomSheet by remember { mutableStateOf(false) }
                var showAiringStatusBottomSheet by remember { mutableStateOf(false) }

                val context = LocalContext.current
                FlowRow(
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.Companion.padding(start = Dimens.PaddingNormal),
                ) {
                    if (selectedChip == SearchFilter.MEDIA || selectedChip == SearchFilter.ANIME || selectedChip == SearchFilter.MANGA) {
                        HorizontalDivider(modifier = Modifier.Companion.padding(vertical = Dimens.PaddingSmall))
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
                            modifier = Modifier.Companion.padding(end = Dimens.PaddingSmall),
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
                            modifier = Modifier.Companion.padding(end = Dimens.PaddingSmall),
                        )

                        FilterChip(
                            onClick = { showYearDialog = true },
                            selected = yearChipSelected,
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.anime_details_calendar),
                                    contentDescription = null,
                                )
                            },
                            label = {
                                Text(
                                    text = if (selectedYear == -1) stringResource(id = R.string.anime_year) else selectedYear.toString(),
                                )
                            },
                            modifier = Modifier.Companion.padding(end = Dimens.PaddingSmall),
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
                                modifier = Modifier.Companion.padding(end = Dimens.PaddingSmall),
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
                                modifier = Modifier.Companion.padding(end = Dimens.PaddingSmall),
                            )
                        }

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
                            modifier = Modifier.Companion.padding(end = Dimens.PaddingSmall),
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
                            AniMediaSort.entries.forEachIndexed { _, mediaSort ->
                                if (mediaSort == AniMediaSort.VOLUMES && selectedChip == SearchFilter.ANIME) {

                                } else {
                                    TextButton(
                                        onClick = {
                                            currentMediaSort = mediaSort
                                            updateSearchParameterless(query)
                                            showSortingBottomSheet = false
                                        },
                                        modifier = Modifier.Companion.fillMaxWidth(),
                                        shape = RectangleShape,
                                    ) {
                                        Text(
                                            mediaSort.toString(context),
                                            color = if (currentMediaSort == mediaSort) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        )
                                    }
                                }
                            }
                        } else if (selectedChip == SearchFilter.CHARACTERS) {
                            AniCharacterSort.entries.forEach { aniCharacterSort ->
                                TextButton(
                                    onClick = {
                                        currentCharacterSort = aniCharacterSort
                                        updateSearchParameterless(query)
                                        showSortingBottomSheet = false
                                    },
                                    modifier = Modifier.Companion.fillMaxWidth(),
                                    shape = RectangleShape,
                                ) {
                                    Text(
                                        aniCharacterSort.toString(context),
                                        color = if (currentCharacterSort == aniCharacterSort) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        } else if (selectedChip == SearchFilter.STAFF) {
                            // TODO issue #49
                        } else if (selectedChip == SearchFilter.STUDIOS) {
                            // TODO issue #50
                        } else if (selectedChip == SearchFilter.THREADS) {
                            // TODO issue #51
                        } else if (selectedChip == SearchFilter.USER) {
                            // TODO issue #52
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
                    var newYear by remember(key1 = showYearDialog) {
                        mutableIntStateOf(selectedYear)
                    }
                    AlertDialog(
                        modifier = Modifier.height(400.dp),
                        onDismissRequest = { showYearDialog = true }, dismissButton = {
                            TextButton(onClick = {
                                showYearDialog = false
                            }) {
                                Text(text = stringResource(R.string.close))
                            }
                        }, confirmButton = {
                            TextButton(onClick = {
                                selectedYear = newYear
                                updateSearchParameterless(query)
                                yearChipSelected = newYear != -1
                                showYearDialog = false
                            }) {
                                Text(text = stringResource(R.string.confirm))
                            }
                        }, title = { Text(text = stringResource(R.string.anime_year)) }, text = {
                            val yearsRange = 1940..Utils.Companion.nextYear()
                            val listState = rememberLazyListState()
                            LazyColumnScrollbar(
                                state = listState,
                                settings = ScrollbarSettings(
                                    alwaysShowScrollbar = true,
                                    thumbUnselectedColor = MaterialTheme.colorScheme.onSurface,
                                    thumbSelectedColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                LazyColumn(state = listState) {
                                    items(yearsRange.toList().sortedDescending()) { year ->
                                        TextButton(
                                            onClick = {
                                                newYear =
                                                    if (newYear == year) {
                                                        -1
                                                    } else {
                                                        year
                                                    }
                                            },
                                            modifier = Modifier.Companion.fillMaxWidth(),
                                            shape = RectangleShape,
                                        ) {
                                            Text(
                                                year.toString(),
                                                color =
                                                    if (newYear == year) {
                                                        MaterialTheme.colorScheme.primary
                                                    } else {
                                                        MaterialTheme.colorScheme.onSurface
                                                    },
                                            )
                                        }
                                    }
                                }
                            }
                        })
                }
                if (showSeasonBottomSheet) {
                    ModalBottomSheet(onDismissRequest = { showSeasonBottomSheet = false }) {
                        AniSeason.entries.forEach { season ->
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
                                        showSeasonBottomSheet = false
                                    },
                                    modifier = Modifier.Companion.fillMaxWidth(),
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
                        AniMediaStatus.entries.forEach { status ->
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
                                        showAiringStatusBottomSheet = false
                                    },
                                    modifier = Modifier.Companion.fillMaxWidth(),
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
    LazyColumn(modifier = Modifier.Companion.fillMaxSize()) {
        when (selectedChip) {
            SearchFilter.MEDIA, SearchFilter.ANIME, SearchFilter.MANGA -> {
                val pager = uiState.searchResultsMedia
                Timber.Forest.d(
                    "Refresh load-state is: " + pager.loadState.refresh.toString() + "\nappend load state is : ${pager.loadState.append}",
                )
                when (pager.loadState.refresh) {
                    is LoadState.Error -> {
                        Timber.Forest.d("Search error: ${(pager.loadState.refresh as LoadState.Error).error.message}")
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
    Box(contentAlignment = Alignment.Companion.TopCenter) {
        Text(
            text = "Network error! Please try searching again.",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier =
                Modifier.Companion
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
    Column(modifier = Modifier.Companion.clickable { navigateToUserDetails(id) }) {
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
    Column(modifier = Modifier.Companion.clickable { navigateToThreadDetails(id) }) {
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
        verticalAlignment = Alignment.Companion.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier =
            Modifier.Companion
                .fillMaxWidth()
                .clickable { navigateToStudioDetails(studio.id) }
                .padding(Dimens.PaddingNormal),
    ) {
        Text(
            text = studio.name,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.Companion.weight(1f),
        )
        Text(
            text = if (studio.favourites == -1) "?" else studio.favourites.toString(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.Companion.padding(horizontal = Dimens.PaddingSmall),
        )
        Icon(
            painter = painterResource(id = if (!studio.isFavourite) R.drawable.anime_details_heart else R.drawable.baseline_favorite_24),
            contentDescription = "Favourites",
            modifier =
                Modifier.Companion.clickable {
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
            Modifier.Companion
                .clickable { onNavigateToDetails(id) },
    ) {
        AsyncImageRoundedCorners(
            coverImage = coverImage,
            padding = Dimens.PaddingSmall,
            width = MEDIUM_MEDIA_WIDTH.dp,
            height = MEDIUM_MEDIA_HEIGHT.dp,
            contentDescription = userPreferredName
        )
        Column(modifier = Modifier.Companion.padding(Dimens.PaddingNormal)) {
            Text(
                text = userPreferredName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Companion.CenterVertically,
                modifier = Modifier.Companion.padding(top = Dimens.PaddingSmall),
            ) {
                Text(
                    text = favourites.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.Companion.padding(end = Dimens.PaddingSmall),
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
            Modifier.Companion
                .fillMaxWidth()
                .clickable { onNavigateToDetails(id) },
        verticalAlignment = Alignment.Companion.CenterVertically,
    ) {
        AsyncImage(
            model =
                ImageRequest.Builder(LocalContext.current).data(coverImage).crossfade(true)
                    .build(),
            contentDescription = "Cover of $title",
            placeholder = painterResource(id = R.drawable.no_image),
            fallback = painterResource(id = R.drawable.no_image),
            contentScale = ContentScale.Companion.Crop,
            modifier =
                Modifier.Companion
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
                    Modifier.Companion.padding(
                        start = Dimens.PaddingNormal,
                        end = Dimens.PaddingNormal,
                        top = Dimens.PaddingNormal,
                    ),
                maxLines = 1,
                overflow = TextOverflow.Companion.Ellipsis,
            )
            QuickInfo(media = media, isAnime = mediaType == AniMediaType.ANIME)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchCardStudioPreview() {
    SearchCardStudio(
        AniStudio(id = 12, name = "Tokyo TV", favourites = 2123),
        navigateToStudioDetails = { },
        toggleFavourite = { })
}

@Preview(showBackground = true)
@Composable
fun SearchCardCharacterPreview() {
    SearchCardCharacter(
        onNavigateToDetails = {},
        id = 12,
        coverImage = "",
        userPreferredName = "Jabami Yumeko",
        favourites = 1223,
        isFavourite = true
    )
}

@Composable
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class,
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

        val sheetState = rememberModalBottomSheetState()
        val scope = rememberCoroutineScope()
        ModalBottomSheet(
            onDismissRequest = {
                setShowTagDialog(false)
                updateSearchParameterless(query)
//                selectedTags.apply {
//                    clear()
//                    addAll(unChangedTags)
//                }
//                selectedGenres.apply {
//                    clear()
//                    addAll(unChangedGenres)
//                }
            },
            shape = RectangleShape,
//            properties =
//                DialogProperties(
//                    usePlatformDefaultWidth = false,
//                    decorFitsSystemWindows = false,
//                ),
//            dismissButton = {
//                TextButton(onClick = {
//                    selectedTags.apply {
//                        clear()
//                        addAll(unChangedTags)
//                    }
//                    selectedGenres.apply {
//                        clear()
//                        addAll(unChangedGenres)
//                    }
//                    setShowTagDialog(false)
//                }) {
//                    Text(text = stringResource(id = R.string.close))
//                }
//            },
//            confirmButton = {
//                TextButton(onClick = {
//                    setShowTagDialog(false)
//                    updateSearchParameterless(query)
//                }) {
//                    Text(text = stringResource(id = R.string.save))
//                }
//            },
//            title = { Text(text = stringResource(id = R.string.genres_tags)) },
        ) {
            if (tags.isNotEmpty() && genres.isNotEmpty()) {
                Column {
                    OutlinedTextField(
                        value = filterTextFieldValue,
                        label = { Text(text = stringResource(id = R.string.filter)) },
                        onValueChange = {
                            filterTextFieldValue = it
                            filterTagsAndGenres(it)
                        },
                        trailingIcon = {
                            if (filterTextFieldValue.isNotEmpty()) {
                                IconButton(onClick = { filterTextFieldValue = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                    )
                                }
                            }
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
                                TagCheckBox(
                                    selectedTags = selectedGenres,
                                    tagName = genre,
                                    tagDescription = "",
                                    addToSelectedTags = selectedGenres::add,
                                    removeFromSelectedTags = selectedGenres::remove,
                                    isGenre = true
                                )
                            }
                        }

                        val alphabeticalListOfTagCategories =
                            filteredTags.map { it.category }.distinct().sorted()
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
                                TagCheckBox(
                                    selectedTags = selectedTags,
                                    tagName = aniTag.name,
                                    tagDescription = aniTag.description,
                                    addToSelectedTags = selectedTags::add,
                                    removeFromSelectedTags = selectedTags::remove
                                )
                            }
                        }
                    }
                }
            } else {
                FailedToLoadTagsAndGenres(genres, reloadGenres, tags, reloadTags)
            }
        }
    }
}

@Preview
@Composable
fun TagDialogPreview() {
    TagDialog(
        showTagDialog = true,
        selectedTags = mutableListOf(),
        selectedGenres = mutableListOf(),
        setShowTagDialog = {},
        updateSearchParameterless = {},
        query = "Demon",
        tags = mutableListOf(
            AniTag(
                id = 12321,
                name = "Boy's love",
                category = "Romance",
                description = "Content involving male gay relationships",
                isAdult = false
            ), AniTag(
                id = 12321,
                name = "Yuri",
                category = "Romance",
                description = "Content involving lesbian relationships",
                isAdult = false
            ),
            AniTag(
                id = 1421,
                name = "Drawing",
                category = "Theme / Arts",
                description = "Centers around the art of drawign",
                isAdult = false
            )
        ),
        genres = mutableListOf("Drama"),
        reloadGenres = { },
        reloadTags = { }) {

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagCheckBox(
    selectedTags: List<String>,
    addToSelectedTags: (String) -> Unit,
    removeFromSelectedTags: (String) -> Unit,
    tagName: String,
    tagDescription: String,
    isGenre: Boolean = false,
) {
    var checked by remember(key1 = selectedTags) {
        mutableStateOf(
            selectedTags.contains(
                tagName,
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
                        removeFromSelectedTags(tagName)
                    } else {
                        checked = true
                        addToSelectedTags(tagName)
                    }
                },
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = {
                if (it) {
                    checked = true
                    addToSelectedTags(tagName)
                } else {
                    checked = false
                    removeFromSelectedTags(tagName)
                }
            },
        )
        Text(
            text = tagName,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (!isGenre) {
            val tooltipState = rememberTooltipState(isPersistent = true)
            val tooltipScope = rememberCoroutineScope()
            TooltipBox(
                positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
                state = tooltipState,
                tooltip = { RichTooltip(title = { Text(text = tagName) }) { Text(text = tagDescription) } },
            ) {
                IconButton(onClick = { tooltipScope.launch { tooltipState.show() } }) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = stringResource(R.string.tag_info),
                    )

                }
            }
        }
    }
}