package com.example.anilist.ui.mymedia

import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.example.anilist.data.models.Media
import com.example.anilist.data.models.PersonalMediaStatus
import com.example.anilist.data.models.StatusUpdate
import com.example.anilist.ui.Dimens
import com.example.anilist.ui.EditStatusModalSheet
import com.example.anilist.ui.mediadetails.LoadingCircle
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
                saveStatus = {
                    myMediaViewModel.updateProgress(statusUpdate = it)
                },
                reloadMyMedia = {
                    myMediaViewModel.fetchMyMedia(isAnime, true)
                },
                deleteListEntry = {
                    myMediaViewModel.deleteEntry(it)
                }
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
    myMedia: Map<PersonalMediaStatus, List<Media>>,
    navigateToDetails: (Int) -> Unit,
    saveStatus: (StatusUpdate) -> Unit,
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

    var filter by rememberSaveable { mutableStateOf(PersonalMediaStatus.UNKNOWN) }
    val setFilter: (PersonalMediaStatus) -> Unit = { filter = it }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isAnime) stringResource(R.string.my_anime_list) else stringResource(
                            R.string.my_manga_list
                        )
                    )
                },
                actions = {
                    PlainTooltipBox(tooltip = { Text(text = stringResource(id = R.string.refresh)) }) {
                        IconButton(
                            onClick = reloadMyMedia,
                            modifier = Modifier.tooltipTrigger()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(id = R.string.refresh)
                            )
                        }
                    }
                    PlainTooltipBox(tooltip = { Text(text = stringResource(id = R.string.sort)) }) {
                        IconButton(onClick = { /*TODO*/ }, modifier = Modifier.tooltipTrigger()) {
                            Icon(
                                painter = painterResource(id = R.drawable.sort),
                                contentDescription = stringResource(
                                    id = R.string.sort
                                )
                            )
                        }
                    }
                })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(stringResource(id = R.string.filter)) },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.my_media_filter),
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
                    myMedia,
                    navigateToDetails,
                    increaseEpisodeProgress = { entryId, newProgress ->
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
                            )
                        )
                    },
                    increaseVolumeProgress = { entryId, newProgress ->
                        saveStatus(
                            StatusUpdate(
                                entryListId = entryId,
                                progressVolumes = newProgress,
                                status = null,
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
                            )
                        )
                    },
                    showEditSheet = showEditSheet,
                    isAnime = isAnime,
                    openRatingDialog = openRatingDialog
                )
            } else {
                NoMediaScreen(isAnime = isAnime)
            }
            if (showRatingDialog) {
                RatingDialog(
                    { showRatingDialog = it },
                    saveStatus,
                    currentMedia,
                    { currentMedia = it })
            }
            if (editSheetIsVisible) {
                EditStatusModalSheet(
                    editSheetState = editSheetState,
                    hideEditSheet = hideEditSheet,
                    unchangedMedia = currentMedia,
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
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun MyMediaLazyList(
    it: PaddingValues,
    filter: PersonalMediaStatus,
    myMedia: Map<PersonalMediaStatus, List<Media>>?,
    navigateToDetails: (Int) -> Unit,
    increaseEpisodeProgress: (entryId: Int, newProgress: Int) -> Unit,
    increaseVolumeProgress: (entryId: Int, newProgress: Int) -> Unit,
    showEditSheet: (Media) -> Unit,
    openRatingDialog: (Media) -> Unit,
    isAnime: Boolean,
) {
    LazyColumn(modifier = Modifier.padding(top = it.calculateTopPadding())) {
        if (filter == PersonalMediaStatus.UNKNOWN || filter == PersonalMediaStatus.CURRENT) {
            val mediaList = myMedia?.get(PersonalMediaStatus.CURRENT).orEmpty()
            if (mediaList.isNotEmpty()) {
                stickyHeader {
                    MyMediaHeadline(
                        if (isAnime) stringResource(id = R.string.watching) else stringResource(
                            id = R.string.reading
                        )
                    )
                }
                items(myMedia?.get(PersonalMediaStatus.CURRENT).orEmpty()) { media ->
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
        if (filter == PersonalMediaStatus.UNKNOWN || filter == PersonalMediaStatus.REPEATING) {
            val mediaList = myMedia?.get(PersonalMediaStatus.REPEATING).orEmpty()
            if (mediaList.isNotEmpty()) {
                stickyHeader {
                    MyMediaHeadline(
                        text = if (isAnime) stringResource(id = R.string.rewatching) else stringResource(
                            id = R.string.rereading
                        )
                    )
                }
                items(mediaList) { media ->
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
        if (filter == PersonalMediaStatus.UNKNOWN || filter == PersonalMediaStatus.PLANNING) {
            val mediaList = myMedia?.get(PersonalMediaStatus.PLANNING).orEmpty()
            if (mediaList.isNotEmpty()) {
                stickyHeader {
                    MyMediaHeadline(
                        text = if (isAnime) stringResource(R.string.plan_to_watch) else stringResource(
                            R.string.plan_to_read
                        )
                    )
                }
                items(mediaList) { media ->
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
        if (filter == PersonalMediaStatus.UNKNOWN || filter == PersonalMediaStatus.COMPLETED) {
            val mediaList = myMedia?.get(PersonalMediaStatus.COMPLETED).orEmpty()
            if (mediaList.isNotEmpty()) {
                stickyHeader { MyMediaHeadline(text = stringResource(R.string.completed)) }
                items(mediaList) { media ->
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
        if (filter == PersonalMediaStatus.UNKNOWN || filter == PersonalMediaStatus.DROPPED) {
            val mediaList = myMedia?.get(PersonalMediaStatus.COMPLETED).orEmpty()
            if (mediaList.isNotEmpty()) {
                stickyHeader { MyMediaHeadline(text = stringResource(R.string.dropped)) }
                items(myMedia?.get(PersonalMediaStatus.DROPPED).orEmpty()) { media ->
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
        if (filter == PersonalMediaStatus.UNKNOWN || filter == PersonalMediaStatus.PAUSED) {
            val mediaList = myMedia?.get(PersonalMediaStatus.PAUSED).orEmpty()
            if (mediaList.isNotEmpty()) {
                stickyHeader {
                    MyMediaHeadline(text = stringResource(R.string.paused))
                }
                items(myMedia?.get(PersonalMediaStatus.PAUSED).orEmpty()) { media ->
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
}

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
                    model = ImageRequest.Builder(LocalContext.current).data(media.coverImage)
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
                            text = media.format,
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
                                text = media.personalRating.div(10).toString(),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        if (isAnime) {
                            Text(
                                text = "${media.personalProgress}/${
                                    if (media.episodeAmount != -1) media.episodeAmount else stringResource(
                                        id = R.string.question_mark
                                    )
                                }",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        } else {
                            Text(
                                text = "${media.personalProgress}/${
                                    if (media.chapters != -1) media.chapters else stringResource(
                                        id = R.string.question_mark
                                    )
                                }",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "${media.personalVolumeProgress}/${
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
                        PlainTooltipBox(
                            tooltip = { Text(text = stringResource(R.string.edit)) },
//                            modifier = Modifier.align(Alignment.BottomCenter),
                        ) {
                            IconButton(
                                onClick = openEditStatusSheet,
                                modifier = Modifier.tooltipTrigger()
                                //                        modifier = Modifier.weight(1f, false)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = stringResource(id = R.string.edit),
                                )
                            }
                        }
                        if (isAnime) {
                            if (media.personalProgress != media.episodeAmount) {
                                IncreaseProgress(
                                    increaseEpisodeProgress,
                                    media.listEntryId,
                                    media.personalProgress,
                                    stringResource(id = R.string.plus_one_episode),
                                )
                            }
                        } else {
                            if (media.chapters != media.personalProgress) {
                                IncreaseProgress(
                                    increaseEpisodeProgress,
                                    media.listEntryId,
                                    media.personalProgress,
                                    stringResource(id = R.string.plus_one_chapter),
                                )
                            }
                            if (media.personalVolumeProgress != media.volumes) {
                                IncreaseProgress(
                                    increaseVolumeProgress,
                                    media.listEntryId,
                                    media.personalVolumeProgress,
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
            (media.personalProgress / (if (isAnime) media.episodeAmount.toFloat() else media.chapters.toFloat()))
        LinearProgressIndicator(
            progress = progress,
            strokeCap = StrokeCap.Square,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.PaddingSmall)
                .align(
                    Alignment.BottomCenter
                )
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

@Preview(showBackground = true, group = "fullscreen")
@Composable
fun MyAnimePreview() {
    MyMedia(
        isAnime = false,
        myMedia = mapOf(
            PersonalMediaStatus.CURRENT to listOf(
                Media(
                    title = "鬼滅の刃",
                    format = "TV",
                    chapters = 80,
                    volumes = 6,
                    personalRating = 6.0,
                    personalProgress = 20,
                    personalVolumeProgress = 3,
                    note = "",
                ),
                Media(
                    title = "NARUTO -ナルト- 紅き四つ葉のクローバーを探せ",
                    format = "TV",
                    chapters = 1012,
                    personalRating = 10.0,
                    personalProgress = 120,
                    note = "",
                    personalVolumeProgress = 2,
                    volumes = 5,
                ),
                Media(
                    title = "ONE PIECE",
                    format = "TV",
                    episodeAmount = 101,
                    personalRating = 3.0,
                    personalProgress = 101,
                    note = "",
                    volumes = 3,
                    personalVolumeProgress = 2,
                ),
            ),
        ),
        navigateToDetails = {},
        saveStatus = { },
        reloadMyMedia = { }
    ) {}
}

@Preview(showBackground = true)
@Composable
fun MediaCardPreview() {
    MediaCard(
        navigateToDetails = {},
        increaseEpisodeProgress = { _, _ -> },
        increaseVolumeProgress = { _, _ -> },
        media =  Media(
            title = "NARUTO -ナルト- 紅き四つ葉のクローバーを探せ",
            format = "TV",
            episodeAmount = 204,
            personalRating = 10.0,
            personalProgress = 120,
            note = "",
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