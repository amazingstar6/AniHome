package com.example.anilist.ui.details.mediadetails

import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.anilist.MainActivity
import com.example.anilist.R
import com.example.anilist.data.models.FuzzyDate
import com.example.anilist.data.models.Media
import com.example.anilist.data.models.AniMediaType
import com.example.anilist.data.models.AniPersonalMediaStatus
import com.example.anilist.data.models.AniLikeAbleType
import com.example.anilist.ui.EditStatusModalSheet
import com.example.anilist.ui.details.mediadetails.components.Characters
import com.example.anilist.ui.details.mediadetails.components.Overview
import com.example.anilist.ui.details.mediadetails.components.Reviews
import com.example.anilist.ui.details.mediadetails.components.StaffScreen
import com.example.anilist.ui.details.mediadetails.components.Stats
import com.example.anilist.ui.mymedia.components.ErrorScreen
import com.example.anilist.utils.LoadingCircle
import com.example.anilist.utils.quantityStringResource
import kotlinx.coroutines.launch

enum class DetailTabs {
    Overview,
    Characters,
    Staff,
    Reviews,
    Stats,
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MediaDetail(
    mediaId: Int,
    modifier: Modifier = Modifier,
    mediaDetailsViewModel: MediaDetailsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (Int) -> Unit,
    onNavigateToReviewDetails: (Int) -> Unit,
    navigateToStaff: (Int) -> Unit,
    navigateToCharacter: (Int) -> Unit,
    onNavigateToStaff: (Int) -> Unit,
    onNavigateToLargeCover: (String) -> Unit,
    navigateToStudioDetails: (Int) -> Unit
) {
    val media by mediaDetailsViewModel.media.collectAsStateWithLifecycle()

    val isAnime by remember {
        mutableStateOf(
            if (media is MediaDetailUiState.Success) {
                (media as MediaDetailUiState.Success).data.type == AniMediaType.ANIME
            } else true
        )
    }
    var editStatusBottomSheetIsVisible by remember { mutableStateOf(false) }
    val editSheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = false, confirmValueChange = {
            it != SheetValue.Hidden
        })
    val modalSheetScope = rememberCoroutineScope()
    val showEditSheet: () -> Unit = {
        editStatusBottomSheetIsVisible = true
        modalSheetScope.launch { editSheetState.show() }
    }
    val hideEditSheet: () -> Unit = {
        editStatusBottomSheetIsVisible = false
        modalSheetScope.launch { editSheetState.hide() }
    }

    val context = LocalContext.current
    LaunchedEffect(key1 = Unit, block = {
        launch {
            mediaDetailsViewModel.toast.collect {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }
    })

//    fetchMedia()

    //fixme reviews reload (network) on navigating back
    val reviews = mediaDetailsViewModel.reviews.collectAsLazyPagingItems()
    val staff = mediaDetailsViewModel.staffList.collectAsLazyPagingItems()


    Scaffold(modifier = modifier, topBar = {
        TopAppBar(title = {
            Text(
                text = if (media is MediaDetailUiState.Success) (media as MediaDetailUiState.Success).data.title else "",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }, navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(id = R.string.back),
                )
            }
        }, actions = {
            val uriHandler = LocalUriHandler.current
            val uri = "https://anilist.co/${if (isAnime) "anime" else "manga"}/$mediaId"
            PlainTooltipBox(tooltip = {
                Text(
                    text = "Favourite",
                )
            }) {
                IconButton(
                    onClick = {
                        mediaDetailsViewModel.toggleFavourite(
                            if (isAnime) AniLikeAbleType.ANIME else AniLikeAbleType.MANGA,
                            mediaId,
                        )
                    },
                    modifier = Modifier.tooltipTrigger(),
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (if (media is MediaDetailUiState.Success) {
                                    (media as MediaDetailUiState.Success).data.isFavourite
                                } else false
                            ) {
                                R.drawable.baseline_favorite_24
                            } else R.drawable.anime_details_heart,
                        ),
                        contentDescription = "Add to favourites",
                    )
                }
            }
            OpenInBrowserAndShareToolTips(uriHandler, uri, context)
        })
    }, floatingActionButton = {
        if (MainActivity.accessCode != "" && media !is MediaDetailUiState.Error && media !is MediaDetailUiState.Loading) {
            FloatingActionButton(
                onClick = {
                    showEditSheet()
                },
            ) {
                Icon(imageVector = Icons.Outlined.Edit, contentDescription = "edit")
            }
        }
    }) {
        when (media) {
            is MediaDetailUiState.Error -> {
                ErrorScreen(
                    errorMessage = (media as MediaDetailUiState.Error).message,
                    reloadMedia = { mediaDetailsViewModel.fetchMedia(mediaId) },
                    modifier = Modifier.padding(top = it.calculateTopPadding())
                )
            }

            is MediaDetailUiState.Success, MediaDetailUiState.Loading -> {

                val selectedLanguage by mediaDetailsViewModel.selectedCharacterLanguage.collectAsStateWithLifecycle()
                val characters = mediaDetailsViewModel.characterList.collectAsLazyPagingItems()
                val setSelectedLanguage: (Int) -> Unit =
                    { mediaDetailsViewModel.setCharacterLanguage(it) }
                Box {
                    Column {
                        val pagerState =
                            rememberPagerState(
                                initialPage = 0,
                                pageCount = { DetailTabs.values().size })
                        val pagerScope = rememberCoroutineScope()
//                val nestedScrollConnection = PagerDefaults.pageNestedScrollConnection(Orientation.Horizontal)

                        AniDetailTabs(
                            modifier = Modifier.padding(top = it.calculateTopPadding()),
                            titles = DetailTabs.values().map { it.name },
                            tabSelected = pagerState.currentPage,
                            onTabSelected = {
                                pagerScope.launch {
                                    pagerState.animateScrollToPage(it.ordinal)
                                }
//                        index = it
                            }
                        )
                        HorizontalPager(
                            state = pagerState,
//                    flingBehavior = PagerDefaults.flingBehavior(
//                        state = pagerState,
//                    ),
                            flingBehavior = PagerDefaults.flingBehavior(
                                state = pagerState,
                                snapAnimationSpec = spring(stiffness = Spring.StiffnessHigh)
                            )
//                    pageNestedScrollConnection = nestedScrollConnection
                        ) { currentPage ->
                            when (currentPage) {
                                0 -> {
                                    Overview(
                                        (media as? MediaDetailUiState.Success)?.data ?: Media(),
                                        isLoading = media is MediaDetailUiState.Loading,
                                        onNavigateToDetails,
                                        onNavigateToLargeCover,
                                        navigateToStudioDetails
                                    )
                                }

                                1 -> {
                                    Characters(
                                        isAnime = isAnime,
                                        languages = (media as MediaDetailUiState.Success).data.languages,
                                        selectedLanguage = selectedLanguage,
                                        setSelectedLanguage = setSelectedLanguage,
//                                        (media as MediaDetailUiState.Success).data.characterWithVoiceActors,
                                        characterWithVoiceActors = characters,
                                        navigateToCharacter = navigateToCharacter,
                                        navigateToStaff = navigateToStaff,
                                    )
                                }

                                2 -> {
                                    StaffScreen(staff, onNavigateToStaff)
                                }

                                3 -> {
                                    val dataIsLoaded: Boolean =
                                        reviews.loadState.source.refresh is LoadState.NotLoading
                                    if (!dataIsLoaded) {
                                        LoadingCircle()
                                    } else {
                                        Reviews(
                                            reviews,
                                            vote = { rating, reviewId ->
                                                mediaDetailsViewModel.rateReview(reviewId, rating)
                                                reviews.refresh() //fixme don't reload pls
                                            },
                                            onNavigateToReviewDetails
                                        )
                                    }
                                }

                                4 -> {
                                    Stats(
                                        (media as MediaDetailUiState.Success).data.stats
                                    )
                                }
                            }
                        }
                        if (editStatusBottomSheetIsVisible) {
                            EditStatusModalSheet(
                                editSheetState = editSheetState,
                                hideEditSheet = hideEditSheet,
                                unChangeListEntry = (media as MediaDetailUiState.Success).data.mediaListEntry,
                                media = (media as? MediaDetailUiState.Success)?.data ?: Media(),
                                saveStatus = { status, isComplete ->
                                    mediaDetailsViewModel.updateProgress(
                                        if (isComplete) status.copy(status = AniPersonalMediaStatus.COMPLETED) else status
                                    )
                                },
                                isAnime = isAnime,
                                deleteListEntry = { mediaDetailsViewModel.deleteEntry(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun OpenInBrowserAndShareToolTips(
    uriHandler: UriHandler,
    uri: String,
    context: Context
) {
    PlainTooltipBox(tooltip = {
        Text(
            text = "Open in browser",
        )
    }) {
        IconButton(
            onClick = { uriHandler.openUri(uri) },
            modifier = Modifier.tooltipTrigger(),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_open_in_browser_24),
                contentDescription = "open in browser",
            )
        }
    }
    PlainTooltipBox(tooltip = { Text(stringResource(id = R.string.share)) }) {
        IconButton(onClick = {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, uri)
                putExtra(Intent.EXTRA_TITLE, "Share AniList.co URL")
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, "Share AniList.co URL")
            startActivity(context, shareIntent, null)
        }, modifier = Modifier.tooltipTrigger()) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = stringResource(
                    id = R.string.share,
                ),
            )
        }
    }
}

@Composable
private fun AniDetailTabs(
    modifier: Modifier = Modifier,
    titles: List<String>,
    tabSelected: Int,
    onTabSelected: (DetailTabs) -> Unit,
) {
    ScrollableTabRow(selectedTabIndex = tabSelected, modifier = modifier, tabs = {
        titles.forEachIndexed { index, title ->
            val selected = index == tabSelected
            Tab(
                selected = selected,
                onClick = { onTabSelected(DetailTabs.values()[index]) },
                text = { Text(text = title) },
            )
        }
    })
}

@Composable
fun QuickInfo(media: Media, isAnime: Boolean) {
    Column(modifier = Modifier.padding(start = 24.dp)) {
        IconWithText(
            R.drawable.anime_details_movie,
            media.format.toString(context = LocalContext.current),
            textColor = MaterialTheme.colorScheme.onSurface,
        )
        IconWithText(
            R.drawable.anime_details_calendar,
            text = if (isAnime) {
                "${media.season.getString(LocalContext.current)}${if (media.seasonYear != -1) " " + media.seasonYear else ""}"
            } else {
                if (media.startDate != null) {
                    formatFuzzyDateToYearMonthDayString(media.startDate)
                } else {
                    stringResource(id = R.string.question_mark)
                }
            },
            textColor = MaterialTheme.colorScheme.onSurface,
        )
        IconWithText(
            R.drawable.anime_details_timer,
            if (isAnime) {
                if (media.episodeAmount != -1) {
                    quantityStringResource(
                        id = R.plurals.episode,
                        quantity = media.episodeAmount,
                        media.episodeAmount,
                    )
                } else stringResource(id = R.string.question_mark)
            } else {
                if (media.chapters != -1) {
                    quantityStringResource(
                        id = R.plurals.chapter,
                        quantity = media.chapters,
                        media.chapters,
                    )
                } else {
                    "?"
                }
            },
            textColor = MaterialTheme.colorScheme.onSurface,
        )
        IconWithText(
            R.drawable.anime_details_heart,
            text = if (media.averageScore != -1) "${media.averageScore}% Average score" else stringResource(
                id = R.string.question_mark
            ),
            textColor = MaterialTheme.colorScheme.onSurface,
        )
    }
}

fun formatFuzzyDateToYearMonthDayString(startDate: FuzzyDate) = "${
    startDate.day.toString().padStart(2, '0')
}-${
    startDate.month.toString().padStart(2, '0')
}-${startDate.year.toString().padStart(2, '0')}"


@Composable
fun IconWithText(
    icon: Int,
    text: String,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    textColor: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = 4.dp)
            .then(modifier),
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = text,
            tint = iconTint,
        )
        Text(
            text,
            modifier = Modifier.padding(horizontal = 6.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
        )
    }
}

