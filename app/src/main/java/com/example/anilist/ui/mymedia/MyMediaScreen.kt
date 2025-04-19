package com.example.anilist.ui.mymedia

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
// todo deprecated
//import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.anilist.R
import com.example.anilist.data.models.AniMediaFormat
import com.example.anilist.data.models.AniMediaListSort
import com.example.anilist.data.models.FuzzyDate
import com.example.anilist.data.models.Media
import com.example.anilist.data.models.AniPersonalMediaStatus
import com.example.anilist.data.models.StatusUpdate
import com.example.anilist.ui.Dimens
import com.example.anilist.ui.EditStatusModalSheet
import com.example.anilist.utils.LoadingCircle
import com.example.anilist.ui.mymedia.components.ErrorScreen
import com.example.anilist.ui.mymedia.components.FilterSheet
import com.example.anilist.ui.mymedia.components.NoMediaScreen
import com.example.anilist.ui.mymedia.components.RatingDialog
import com.example.anilist.ui.mymedia.components.SortingBottomSheet
import com.example.anilist.utils.isScrollingUp
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * The hilt view model is given here and not shared between my anime and my manga,
 * so that the my anime screen and my manga screen don't use the same ui state
 */
@Composable
fun MyMediaScreen(
    myMediaViewModel: MyMediaViewModel = hiltViewModel(),
    navigateToDetails: (Int) -> Unit,
    isAnime: Boolean,
) {
    val uiState by myMediaViewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val sort by myMediaViewModel.sort.collectAsStateWithLifecycle()
    var isDescending by remember { mutableStateOf(sort.isDescending()) }
    val setSort: (AniMediaListSort, Boolean) -> Unit = { newSort, desc ->
        myMediaViewModel.setSort(if (desc) AniMediaListSort.valueOf(newSort.name + "_DESC") else newSort)
    }
    val setIsDescending: (Boolean) -> Unit = { isDescending = it }


    LaunchedEffect(Unit) {
        myMediaViewModel
            .toastMessage
            .collect { message ->
                Toast.makeText(
                    context,
                    message,
                    Toast.LENGTH_SHORT,
                ).show()
            }
    }

    if (uiState !is MyMediaUiState.Success) {
        LaunchedEffect(key1 = Unit) {
            Timber.d("Media got fetched, isAnime is $isAnime")
            myMediaViewModel.fetchMyMedia(isAnime, true)
        }
    }

    when (uiState) {
        is MyMediaUiState.Success -> {
            MyMedia(
                isAnime = isAnime,
                myMedia = (uiState as MyMediaUiState.Success).myMedia,
                navigateToDetails = navigateToDetails,
                saveStatus = { status, isComplete ->
                    myMediaViewModel.updateProgress(statusUpdate = status, isComplete = isComplete)
                },
                reloadMyMedia = {
                    myMediaViewModel.fetchMyMedia(isAnime, true)
                },
                deleteListEntry = {
                    myMediaViewModel.deleteEntry(it)
                },
                sort = sort,
                setSort = setSort,
                isDescending = isDescending,
                setIsDescending = setIsDescending
            )
        }

        is MyMediaUiState.Loading -> {
            LoadingCircle()
        }

        is MyMediaUiState.Error -> {
            val errorMessage = (uiState as MyMediaUiState.Error).message
            val reloadMedia = { myMediaViewModel.fetchMyMedia(isAnime, true) }
            ErrorScreen(errorMessage, reloadMedia)
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MyMedia(
    isAnime: Boolean,
    myMedia: Map<AniPersonalMediaStatus, List<Media>>,
    sort: AniMediaListSort,
    setSort: (AniMediaListSort, Boolean) -> Unit,
    isDescending: Boolean,
    setIsDescending: (Boolean) -> Unit,
    navigateToDetails: (Int) -> Unit,
    saveStatus: (StatusUpdate, Boolean) -> Unit,
    reloadMyMedia: () -> Unit,
    deleteListEntry: (id: Int) -> Unit,
) {
    val modalSheetScope = rememberCoroutineScope()
    var currentMedia by remember {
        mutableStateOf(Media())
    }

    var filterSheetIsVisible by remember {
        mutableStateOf(false)
    }
    var editSheetIsVisible by remember {
        mutableStateOf(false)
    }

    val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val editSheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = false, confirmValueChange = {
            it != SheetValue.Hidden
        })

    val showFilterSheet: () -> Unit = {
        filterSheetIsVisible = true
        modalSheetScope.launch { filterSheetState.show() }
    }
    val hideFilterSheet: () -> Unit = {
        filterSheetIsVisible = false
        modalSheetScope.launch { filterSheetState.hide() }
    }

    val showEditSheet: (Media) -> Unit = {
        editSheetIsVisible = true
        currentMedia = it
        modalSheetScope.launch { editSheetState.show() }
    }
    val hideEditSheet: () -> Unit = {
        editSheetIsVisible = false
        modalSheetScope.launch { editSheetState.hide() }
    }

    var showRatingDialog by remember { mutableStateOf(false) }
    val openRatingDialog: (Media) -> Unit = {
        showRatingDialog = true
        currentMedia = it
    }
    var showSortingSheet by remember { mutableStateOf(false) }

    var filter by rememberSaveable { mutableStateOf(AniPersonalMediaStatus.UNKNOWN) }
    val setFilter: (AniPersonalMediaStatus) -> Unit = { filter = it }

    val topAppBarScrollBehaviour = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val lazyListState = rememberLazyListState()
    val isFabVisible = lazyListState.isScrollingUp()
    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehaviour.nestedScrollConnection),
        topBar = {
            TopAppBar(
                scrollBehavior = topAppBarScrollBehaviour,
                title = {
                    Text(
                        text = if (isAnime) stringResource(R.string.my_anime_list) else stringResource(
                            R.string.my_manga_list
                        )
                    )
                },
                actions = {
                    // todo deprecated
//                    PlainTooltipBox(tooltip = { Text(text = stringResource(id = R.string.refresh)) }) {
//                        IconButton(
//                            onClick = reloadMyMedia,
//                            modifier = Modifier.tooltipTrigger()
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Refresh,
//                                contentDescription = stringResource(id = R.string.refresh)
//                            )
//                        }
//                    }
//                    PlainTooltipBox(tooltip = { Text(text = stringResource(id = R.string.sort)) }) {
//                        IconButton(
//                            onClick = { showSortingSheet = true },
//                            modifier = Modifier.tooltipTrigger()
//                        ) {
//                            Icon(
//                                painter = painterResource(id = R.drawable.sort),
//                                contentDescription = stringResource(
//                                    id = R.string.sort
//                                )
//                            )
//                        }
//                    }
                })
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = isFabVisible,
                enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(
                    tween(100)
                ),
                exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(tween(300))
            ) {
                ExtendedFloatingActionButton(
                    text = {
                        Text(
                            if (filter == AniPersonalMediaStatus.UNKNOWN) stringResource(id = R.string.filter) else filter.toString(
                                isAnime = isAnime,
                                context = LocalContext.current
                            )
                        )
                    },
                    icon = {
                        Icon(
                            painter = painterResource(
                                id = filter.getIconResource()
                            ),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    },
                    onClick = {
                        showFilterSheet()
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .padding(Dimens.PaddingNormal),
                )
            }
        },
    ) {
        Box(
            Modifier
                .fillMaxSize(),
        ) {
            if (myMedia.isNotEmpty()) {
                MyMediaLazyList(
                    it,
                    filter,
                    sort = sort,
                    myMedia,
                    navigateToDetails,
                    increaseEpisodeProgress = { entryId, newProgress ->
                        Timber.d("New progress is $newProgress, current media episode amount is ${currentMedia.episodeAmount}")
                        saveStatus(
                            StatusUpdate(
                                entryListId = entryId,
                                progressVolumes = null,
                                status = null,
                                scoreRaw = null,
                                progress = newProgress,
                                repeat = null,
                                priority = null,
                                privateToUser = null,
                                notes = null,
                                hiddenFromStatusList = null,
                                customLists = null,
                                advancedScores = null,
                                startedAt = null,
                                completedAt = null,
                                mediaId = currentMedia.id
                            ),
                            (if (isAnime) currentMedia.episodeAmount else currentMedia.chapters)
                                    == newProgress
                        )
                    },
                    increaseVolumeProgress = { entryId, newProgress ->
                        saveStatus(
                            StatusUpdate(
                                entryListId = entryId,
                                progressVolumes = newProgress,
                                status = currentMedia.mediaListEntry.status,
                                scoreRaw = null,
                                progress = null,
                                repeat = null,
                                priority = null,
                                privateToUser = null,
                                notes = null,
                                hiddenFromStatusList = null,
                                customLists = null,
                                advancedScores = null,
                                startedAt = null,
                                completedAt = null,
                                mediaId = currentMedia.id
                            ),
                            currentMedia.volumes == currentMedia.mediaListEntry.progress
                        )
                    },
                    showEditSheet = showEditSheet,
                    isAnime = isAnime,
                    openRatingDialog = openRatingDialog,
                    lazyListState = lazyListState
                )
            } else {
                NoMediaScreen(isAnime = isAnime)
            }
            if (showRatingDialog) {
                RatingDialog(
                    { showRatingDialog = it },
                    { saveStatus(it, false) },
                    currentMedia,
                    { currentMedia = it })
            }
            if (editSheetIsVisible) {
                EditStatusModalSheet(
                    editSheetState = editSheetState,
                    hideEditSheet = hideEditSheet,
                    unChangeListEntry = currentMedia.mediaListEntry,
                    media = currentMedia,
                    saveStatus = saveStatus,
                    isAnime = isAnime,
                    deleteListEntry = deleteListEntry
                )
            }
            if (filterSheetIsVisible) {
                FilterSheet(
                    filterSheetState,
                    hideFilterSheet,
                    filter,
                    setFilter = setFilter,
                    isAnime
                )
            }
            if (showSortingSheet) {
                SortingBottomSheet(
                    setShowSortingSheet = { showSortingSheet = it },
                    isDescending = isDescending,
                    setIsDescending = setIsDescending,
                    setSort = setSort,
                    sort = sort
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun MyMediaLazyList(
    it: PaddingValues,
    filter: AniPersonalMediaStatus,
    sort: AniMediaListSort,
    myMedia: Map<AniPersonalMediaStatus, List<Media>>?,
    navigateToDetails: (Int) -> Unit,
    increaseEpisodeProgress: (entryId: Int, newProgress: Int) -> Unit,
    increaseVolumeProgress: (entryId: Int, newProgress: Int) -> Unit,
    showEditSheet: (Media) -> Unit,
    openRatingDialog: (Media) -> Unit,
    isAnime: Boolean,
    lazyListState: LazyListState
) {
    val sortedMediaList: Map<AniPersonalMediaStatus, List<Media>>? by remember(
        key1 = sort,
        key2 = myMedia
    ) {
        mutableStateOf(myMedia?.mapValues { (_, mediaList) ->
            when (sort) {
                AniMediaListSort.MEDIA_ID -> mediaList.sortedBy { media -> media.id }
                AniMediaListSort.MEDIA_ID_DESC -> mediaList.sortedByDescending { media -> media.id }
                AniMediaListSort.SCORE -> mediaList.sortedBy { media -> media.mediaListEntry.score }
                AniMediaListSort.SCORE_DESC -> mediaList.sortedByDescending { media -> media.mediaListEntry.score }
                AniMediaListSort.UPDATED_TIME -> mediaList.sortedBy { media -> sortDate(media.mediaListEntry.updatedAt) }
                AniMediaListSort.UPDATED_TIME_DESC -> mediaList.sortedByDescending { media ->
                    sortDate(
                        media.mediaListEntry.updatedAt
                    )
                }

                AniMediaListSort.PROGRESS -> mediaList.sortedBy { media -> media.mediaListEntry.progress }
                AniMediaListSort.PROGRESS_DESC -> mediaList.sortedByDescending { media -> media.mediaListEntry.progress }
                AniMediaListSort.PROGRESS_VOLUMES -> mediaList.sortedBy { media -> media.mediaListEntry.progressVolumes }
                AniMediaListSort.PROGRESS_VOLUMES_DESC -> mediaList.sortedByDescending { media -> media.mediaListEntry.progressVolumes }
                AniMediaListSort.REPEAT -> mediaList.sortedBy { media -> media.mediaListEntry.repeat }
                AniMediaListSort.REPEAT_DESC -> mediaList.sortedByDescending { media -> media.mediaListEntry.repeat }
                AniMediaListSort.PRIORITY -> mediaList.sortedBy { media -> media.priority }
                AniMediaListSort.PRIORITY_DESC -> mediaList.sortedByDescending { media -> media.priority }
                AniMediaListSort.STARTED_ON -> mediaList.sortedBy { media ->
                    sortDate(media.mediaListEntry.startedAt)
                }

                AniMediaListSort.STARTED_ON_DESC -> mediaList.sortedByDescending { media ->
                    sortDate(media.mediaListEntry.startedAt)
                }

                AniMediaListSort.FINISHED_ON -> mediaList.sortedBy { media ->
                    sortDate(media.mediaListEntry.completedAt)
                }

                AniMediaListSort.FINISHED_ON_DESC -> mediaList.sortedByDescending { media ->
                    sortDate(media.mediaListEntry.completedAt)
                }

                AniMediaListSort.ADDED_TIME -> mediaList.sortedBy { media ->
                    sortDate(media.mediaListEntry.createdAt)
                }

                AniMediaListSort.ADDED_TIME_DESC -> mediaList.sortedByDescending { media ->
                    sortDate(media.mediaListEntry.createdAt)
                }

                AniMediaListSort.MEDIA_TITLE -> mediaList.sortedBy { media -> media.title }
                AniMediaListSort.MEDIA_TITLE_DESC -> mediaList.sortedByDescending { media -> media.title }
            }
        }
        )
    }
    LazyColumn(state = lazyListState, modifier = Modifier.padding(top = it.calculateTopPadding())) {
        AniPersonalMediaStatus.values().forEach { status ->
            if (filter == AniPersonalMediaStatus.UNKNOWN || filter == status) {
                val mediaList = sortedMediaList?.get(status).orEmpty()
                if (mediaList.isNotEmpty()) {
                    stickyHeader {
                        MyMediaHeadline(
                            status.toString(LocalContext.current, isAnime)
                        )
                    }
                    items(
                        mediaList,
                        key = { it.id }
                    ) { media ->
                        MediaCard(
                            navigateToDetails,
                            increaseEpisodeProgress,
                            increaseVolumeProgress,
                            media,
                            { showEditSheet(media) },
                            isAnime = isAnime,
                            openRatingDialog = { openRatingDialog(media) }
                        )
                    }
                }
            }
        }
//        if (filter == PersonalMediaStatus.UNKNOWN || filter == PersonalMediaStatus.CURRENT) {
//            val mediaList = sortedMediaList?.get(PersonalMediaStatus.CURRENT).orEmpty()
//            if (mediaList.isNotEmpty()) {
//                stickyHeader {
//                    MyMediaHeadline(
//                        if (isAnime) stringResource(id = R.string.watching) else stringResource(
//                            id = R.string.reading
//                        )
//                    )
//                }
//                items(
//                    sortedMediaList?.get(PersonalMediaStatus.CURRENT).orEmpty(),
//                    key = { it.id }
//                ) { media ->
//                    MediaCard(
//                        navigateToDetails,
//                        increaseEpisodeProgress,
//                        increaseVolumeProgress,
//                        media,
//                        { showEditSheet(media) },
//                        isAnime = isAnime,
//                        openRatingDialog = { openRatingDialog(media) }
//                    )
//                }
//            }
//        }
//        if (filter == PersonalMediaStatus.UNKNOWN || filter == PersonalMediaStatus.REPEATING) {
//            val mediaList = sortedMediaList?.get(PersonalMediaStatus.REPEATING).orEmpty()
//            if (mediaList.isNotEmpty()) {
//                stickyHeader {
//                    MyMediaHeadline(
//                        text = if (isAnime) stringResource(id = R.string.rewatching) else stringResource(
//                            id = R.string.rereading
//                        )
//                    )
//                }
//                items(mediaList, key = { it.id }) { media ->
//                    MediaCard(
//                        navigateToDetails,
//                        increaseEpisodeProgress,
//                        increaseVolumeProgress,
//                        media,
//                        { showEditSheet(media) },
//                        isAnime = isAnime,
//                        openRatingDialog = { openRatingDialog(media) }
//                    )
//                }
//            }
//        }
//        if (filter == PersonalMediaStatus.UNKNOWN || filter == PersonalMediaStatus.PLANNING) {
//            val mediaList = sortedMediaList?.get(PersonalMediaStatus.PLANNING).orEmpty()
//            if (mediaList.isNotEmpty()) {
//                stickyHeader {
//                    MyMediaHeadline(
//                        text = if (isAnime) stringResource(R.string.plan_to_watch) else stringResource(
//                            R.string.plan_to_read
//                        )
//                    )
//                }
//                items(mediaList, key = { it.id }) { media ->
//                    MediaCard(
//                        navigateToDetails,
//                        increaseEpisodeProgress,
//                        increaseVolumeProgress,
//                        media,
//                        { showEditSheet(media) },
//                        isAnime = isAnime,
//                        openRatingDialog = { openRatingDialog(media) }
//                    )
//                }
//            }
//        }
//        if (filter == PersonalMediaStatus.UNKNOWN || filter == PersonalMediaStatus.COMPLETED) {
//            val mediaList = sortedMediaList?.get(PersonalMediaStatus.COMPLETED).orEmpty()
//            if (mediaList.isNotEmpty()) {
//                stickyHeader { MyMediaHeadline(text = stringResource(R.string.completed)) }
//                items(mediaList, key = { it.id }) { media ->
//                    MediaCard(
//                        navigateToDetails,
//                        increaseEpisodeProgress,
//                        increaseVolumeProgress,
//                        media,
//                        { showEditSheet(media) },
//                        isAnime = isAnime,
//                        openRatingDialog = { openRatingDialog(media) }
//                    )
//                }
//            }
//        }
//        if (filter == PersonalMediaStatus.UNKNOWN || filter == PersonalMediaStatus.DROPPED) {
//            val mediaList = sortedMediaList?.get(PersonalMediaStatus.COMPLETED).orEmpty()
//            if (mediaList.isNotEmpty()) {
//                stickyHeader { MyMediaHeadline(text = stringResource(R.string.dropped)) }
//                items(
//                    sortedMediaList?.get(PersonalMediaStatus.DROPPED).orEmpty(),
//                    key = { it.id }
//                ) { media ->
//                    MediaCard(
//                        navigateToDetails,
//                        increaseEpisodeProgress,
//                        increaseVolumeProgress,
//                        media,
//                        { showEditSheet(media) },
//                        isAnime = isAnime,
//                        openRatingDialog = { openRatingDialog(media) }
//                    )
//                }
//            }
//        }
//        if (filter == PersonalMediaStatus.UNKNOWN || filter == PersonalMediaStatus.PAUSED) {
//            val mediaList = sortedMediaList?.get(PersonalMediaStatus.PAUSED).orEmpty()
//            if (mediaList.isNotEmpty()) {
//                stickyHeader {
//                    MyMediaHeadline(text = stringResource(R.string.paused))
//                }
//                items(
//                    sortedMediaList?.get(PersonalMediaStatus.PAUSED).orEmpty(),
//                    key = { it.id }
//                ) { media ->
//                    MediaCard(
//                        navigateToDetails,
//                        increaseEpisodeProgress,
//                        increaseVolumeProgress,
//                        media,
//                        { showEditSheet(media) },
//                        isAnime = isAnime,
//                        openRatingDialog = { openRatingDialog(media) }
//                    )
//                }
//            }
//        }
    }
}

fun sortDate(date: FuzzyDate?) =
    (date?.year?.times(10000) ?: 0) + (date?.month?.times(100) ?: 0) + (date?.day ?: 0)

@Composable
private fun MyMediaHeadline(text: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(
                start = Dimens.PaddingNormal,
                end = Dimens.PaddingNormal,
                bottom = Dimens.PaddingSmall
            ),
        )
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
private fun MediaCard(
    navigateToDetails: (Int) -> Unit,
    increaseEpisodeProgress: (mediaId: Int, newProgress: Int) -> Unit,
    increaseVolumeProgress: (mediaId: Int, newProgress: Int) -> Unit,
    media: Media,
    openEditStatusSheet: () -> Unit,
    openRatingDialog: () -> Unit,
    isAnime: Boolean,
) {
    val haptic = LocalHapticFeedback.current

//    val context = LocalContext.current
//    val request = ImageRequest.Builder(context)
//        .data(media.coverImage)
//        // Optional, but setting a ViewSizeResolver will conserve memory by limiting the size the image should be preloaded into memory at.
//        .build()
//    context.imageLoader.enqueue(request)

    Box(modifier = Modifier.padding(vertical = Dimens.PaddingSmall)) {
        ElevatedCard(
            shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
            modifier = Modifier
                .padding(horizontal = Dimens.PaddingSmall)
                .height(150.dp)
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { navigateToDetails(media.id) },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        openEditStatusSheet()
                    }
                ),
        ) {
            Row {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(media.coverImage)
                        .crossfade(true).build(),
                    contentDescription = "",
                    placeholder = painterResource(id = R.drawable.no_image),
                    fallback = painterResource(id = R.drawable.no_image),
                    contentScale = ContentScale.FillHeight,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(100.dp)
                        .clip(RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp)),
                )
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = Dimens.PaddingSmall,
                            bottom = Dimens.PaddingNormal,
                            start = Dimens.PaddingSmall,
                        ),
                ) {
                    Column(
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = media.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.width(175.dp),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 2,
                        )
                        Text(
                            text = media.format.toString(context = LocalContext.current),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier.clickable {
                                openRatingDialog()
                            }
                        ) {
                            Icon(
                                painterResource(id = R.drawable.anime_details_rating_star),
                                contentDescription = stringResource(id = R.string.rating),
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(end = 2.dp)
                            )
                            Text(
                                text = media.mediaListEntry.score.div(10).toString(),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        if (isAnime) {
                            Text(
                                text = "${media.mediaListEntry.progress}/${
                                    if (media.episodeAmount != -1) media.episodeAmount else stringResource(
                                        id = R.string.question_mark
                                    )
                                }",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        } else {
                            Text(
                                text = "${media.mediaListEntry.progress}/${
                                    if (media.chapters != -1) media.chapters else stringResource(
                                        id = R.string.question_mark
                                    )
                                }",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "${media.mediaListEntry.progressVolumes}/${
                                    if (media.volumes != -1) media.volumes else stringResource(
                                        id = R.string.question_mark
                                    )
                                }",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        // TODO deprecated
//                        PlainTooltipBox(
//                            tooltip = { Text(text = stringResource(R.string.edit)) },
////                            modifier = Modifier.align(Alignment.BottomCenter),
//                        ) {
//                            IconButton(
//                                onClick = openEditStatusSheet,
//                                modifier = Modifier.tooltipTrigger()
//                                //                        modifier = Modifier.weight(1f, false)
//                            ) {
//                                Icon(
//                                    imageVector = Icons.Default.Edit,
//                                    contentDescription = stringResource(id = R.string.edit),
//                                )
//                            }
//                        }
                        if (isAnime) {
                            if (media.mediaListEntry.progress != media.episodeAmount) {
                                IncreaseProgress(
                                    increaseEpisodeProgress,
                                    media.mediaListEntry.listEntryId,
                                    media.mediaListEntry.progress,
                                    stringResource(id = R.string.plus_one_episode),
                                )
                            }
                        } else {
                            if (media.chapters != media.mediaListEntry.progress) {
                                IncreaseProgress(
                                    increaseEpisodeProgress,
                                    media.mediaListEntry.listEntryId,
                                    media.mediaListEntry.progress,
                                    stringResource(id = R.string.plus_one_chapter),
                                )
                            }
                            if (media.mediaListEntry.progressVolumes != media.volumes) {
                                IncreaseProgress(
                                    increaseVolumeProgress,
                                    media.mediaListEntry.listEntryId,
                                    media.mediaListEntry.progressVolumes,
                                    stringResource(
                                        id = R.string.plus_one_volume,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
        val progress =
            (media.mediaListEntry.progress / (if (isAnime) media.episodeAmount.toFloat() else media.chapters.toFloat()))
        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
            label = "Media progress bar"
        )
        LinearProgressIndicator(
            progress = animatedProgress,
            strokeCap = StrokeCap.Square,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.PaddingSmall)
                .align(
                    Alignment.BottomCenter
                )
                .animateContentSize()
        )
    }
}

@Composable
private fun IncreaseProgress(
    increaseEpisodeProgress: (mediaId: Int, newProgress: Int) -> Unit,
    listEntryId: Int,
    personalEpisodeProgress: Int,
    label: String,
) {
    var personalEpisodeProgress1 = personalEpisodeProgress
    OutlinedButton(
        onClick = {
            increaseEpisodeProgress(
                listEntryId,
                personalEpisodeProgress1 + 1,
            )
            personalEpisodeProgress1++
        },
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier.padding(end = Dimens.PaddingSmall)
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

//@Preview(showBackground = true, group = "fullscreen")
//@Composable
//fun MyAnimePreview() {
//    MyMedia(
//        isAnime = false,
//        myMedia = mapOf(
//            PersonalMediaStatus.CURRENT to listOf(
//                Media(
//                    title = "鬼滅の刃",
//                    format = AniMediaFormat.TV,
//                    personalRating = 6.0,
//                    personalProgress = 20,
//                    note = "",
//                    volumes = 6,
//                    personalVolumeProgress = 3,
//                    chapters = 80,
//                ),
//                Media(
//                    title = "NARUTO -ナルト- 紅き四つ葉のクローバーを探せ",
//                    format = AniMediaFormat.TV,
//                    personalRating = 10.0,
//                    personalProgress = 120,
//                    note = "",
//                    volumes = 5,
//                    personalVolumeProgress = 2,
//                    chapters = 1012,
//                ),
//                Media(
//                    title = "ONE PIECE",
//                    format = AniMediaFormat.TV,
//                    episodeAmount = 101,
//                    personalRating = 3.0,
//                    personalProgress = 101,
//                    note = "",
//                    volumes = 3,
//                    personalVolumeProgress = 2,
//                )
//            ),
//        ),
//        navigateToDetails = {},
//        saveStatus = { },
//        reloadMyMedia = { },
//        deleteListEntry = {},
//        sort = AniMediaListSort.UPDATED_TIME_DESC,
//        setSort = { _, _ -> },
//        isDescending = true,
//        setIsDescending = {}
//    )
//}

@Preview(showBackground = true)
@Composable
fun MediaCardPreview() {
    MediaCard(
        navigateToDetails = {},
        increaseEpisodeProgress = { _, _ -> },
        increaseVolumeProgress = { _, _ -> },
        media = Media(
            title = "NARUTO -ナルト- 紅き四つ葉のクローバーを探せ",
            format = AniMediaFormat.TV,
            episodeAmount = 204,
//            personalRating = 10.0,
//            personalProgress = 120,
//            note = "",
        ),
        openEditStatusSheet = { },
        openRatingDialog = { },
        isAnime = true
    )
}

@Preview(showBackground = true)
@Composable
fun IncreaseProgressPreview() {
    IncreaseProgress(
        increaseEpisodeProgress = { _, _ -> },
        listEntryId = 1023918,
        personalEpisodeProgress = 12,
        label = stringResource(id = R.string.plus_one_episode)
    )
}


