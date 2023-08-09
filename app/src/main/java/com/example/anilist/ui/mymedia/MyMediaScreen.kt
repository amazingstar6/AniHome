package com.example.anilist.ui.mymedia

import android.util.Log
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
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
import com.example.anilist.data.models.FuzzyDate
import com.example.anilist.data.models.Media
import com.example.anilist.data.models.StatusUpdate
import com.example.anilist.ui.Dimens
import com.example.anilist.ui.EditStatusModalSheet
import com.example.anilist.ui.SliderTextField
import com.example.anilist.ui.mediadetails.LoadingCircle
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import timber.log.Timber

enum class PersonalMediaStatus {
    CURRENT,
    PLANNING,
    COMPLETED,
    REPEATING,
    PAUSED,
    DROPPED,
    UNKNOWN, ;

    fun getString(isAnime: Boolean): String {
        return when (this) {
            CURRENT -> if (isAnime) "Watching" else "Reading"
            PLANNING -> if (isAnime) "Plan to watch" else "Plan to read"
            COMPLETED -> "Completed"
            DROPPED -> "Dropped"
            PAUSED -> "Paused"
            REPEATING -> if (isAnime) "Rewatching" else "Rereading"
            UNKNOWN -> "None"
        }
    }
}

private const val TAG = "MyMediaScreen"

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
private fun ErrorScreen(errorMessage: String, reloadMedia: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.PaddingNormal),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = errorMessage)
        Text(text = "Check your network connections and reload your media list, " +
                "if the issue still persists, try logging out through the settings menu on the home page")
        Button(onClick = {
            reloadMedia()
        }) {
            Text(text = "Reload")
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MyMedia(
    isAnime: Boolean,
    myMedia: Map<PersonalMediaStatus, List<Media>>?,
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

    val filterSheetState = rememberModalBottomSheetState()
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
                                contentDescription = "Refresh"
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
                text = { Text("Filter") },
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
            if (showRatingDialog) {
                AlertDialog(onDismissRequest = { showRatingDialog = false }, dismissButton = {
                    TextButton(
                        onClick = {
                            showRatingDialog =
                                false
                        }) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                }, confirmButton = {
                    TextButton(
                        onClick = {
                            saveStatus(
                                StatusUpdate(
                                    entryListId = currentMedia.listEntryId,
                                    progressVolumes = null,
                                    status = null,
                                    scoreRaw = currentMedia.rawScore.toInt(),
                                    progress = null,
                                    repeat = null,
                                    priority = null,
                                    privateToUser = null,
                                    notes = null,
                                    hiddenFromStatusList = null,
                                    customLists = null,
                                    advancedScores = null,
                                    startedAt = null,
                                    completedAt = null
                                )
                            )
                            showRatingDialog = false
                        }) {
                        Text(text = "Save")
                    }
                }, text = {
                    Column {
                        SliderTextField(
                            rawScore = currentMedia.rawScore,
                            setRawScore = { currentMedia = currentMedia.copy(rawScore = it) })
                    }
                })
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
                FilterModalSheet(
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
@OptIn(ExperimentalMaterial3Api::class)
private fun FilterModalSheet(
    filterSheetState: SheetState,
    hideFilterSheet: () -> Unit,
    filter: PersonalMediaStatus,
    setFilter: (PersonalMediaStatus) -> Unit,
    isAnime: Boolean
) {
    ModalBottomSheet(
        sheetState = filterSheetState,
        onDismissRequest = hideFilterSheet,
    ) {
        CenterAlignedTopAppBar(title = {
            Text(
                text = stringResource(R.string.filter),
            )
        }, navigationIcon = {
            IconButton(
                onClick = hideFilterSheet,
                modifier = Modifier.padding(Dimens.PaddingNormal),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.close),
                )
            }
        })
        ModalSheetTextButton(
            filterFunction = setFilter,
            hideFilterSheet = hideFilterSheet,
            filter = filter,
            thisFilter = PersonalMediaStatus.UNKNOWN,
            name = stringResource(R.string.all),
            icon = R.drawable.baseline_done_all_24
        )
        ModalSheetTextButton(
            filterFunction = setFilter,
            hideFilterSheet = hideFilterSheet,
            filter = filter,
            thisFilter = PersonalMediaStatus.CURRENT,
            name = if (isAnime) stringResource(R.string.watching) else stringResource(R.string.reading),
            icon = R.drawable.outline_play_circle_24
        )
        ModalSheetTextButton(
            filterFunction = setFilter,
            hideFilterSheet = hideFilterSheet,
            filter = filter,
            thisFilter = PersonalMediaStatus.REPEATING,
            name = if (isAnime) stringResource(R.string.rewatching) else stringResource(R.string.rereading),
            icon = R.drawable.outline_replay_24
        )
        ModalSheetTextButton(
            filterFunction = setFilter,
            hideFilterSheet = hideFilterSheet,
            filter = filter,
            thisFilter = PersonalMediaStatus.COMPLETED,
            name = "Completed",
//            icon = R.drawable.outline_done_24
            icon = R.drawable.outline_check_circle_24
        )
        ModalSheetTextButton(
            filterFunction = setFilter,
            hideFilterSheet = hideFilterSheet,
            filter = filter,
            thisFilter = PersonalMediaStatus.PAUSED,
            name = "Paused",
            icon = R.drawable.outline_pause_circle_24
        )
        ModalSheetTextButton(
            filterFunction = setFilter,
            hideFilterSheet = hideFilterSheet,
            filter = filter,
            thisFilter = PersonalMediaStatus.DROPPED,
            name = "Dropped",
//            icon = R.drawable.outline_arrow_drop_down_circle_24
            icon = R.drawable.outline_cancel_24
        )
        ModalSheetTextButton(
            filterFunction = setFilter,
            hideFilterSheet = hideFilterSheet,
            filter = filter,
            thisFilter = PersonalMediaStatus.PLANNING,
            name = "Planning",
            icon = R.drawable.outline_task_alt_24
        )
//        TextButton(
//            onClick = {
//                setFilter(PersonalMediaStatus.CURRENT)
//                hideFilterSheet()
//            }, modifier = Modifier
//                .fillMaxWidth(), shape = RectangleShape
//        ) {
//            Text(
//                if (isAnime) stringResource(R.string.watching) else stringResource(R.string.reading),
//                color = if (filter == PersonalMediaStatus.CURRENT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
//            )
//        }
//        TextButton(
//            onClick = {
//                setFilter(PersonalMediaStatus.REPEATING)
//                hideFilterSheet()
//            }, modifier = Modifier
//                .fillMaxWidth(), shape = RectangleShape
//        ) {
//            Text(
//                if (isAnime) stringResource(R.string.rewatching) else stringResource(R.string.rereading),
//                color = if (filter == PersonalMediaStatus.REPEATING) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
//            )
//        }
//        TextButton(
//            onClick = {
//                setFilter(PersonalMediaStatus.DROPPED)
//                hideFilterSheet()
//            }, modifier = Modifier
//                .fillMaxWidth(), shape = RectangleShape
//        ) {
//            Text(
//                "Dropped",
//                color = if (filter == PersonalMediaStatus.DROPPED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
//            )
//        }
//        TextButton(
//            onClick = {
//                setFilter(PersonalMediaStatus.COMPLETED)
//                hideFilterSheet()
//            }, modifier = Modifier
//                .fillMaxWidth(), shape = RectangleShape
//        ) {
//            Text(
//                "Completed",
//                color = if (filter == PersonalMediaStatus.COMPLETED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
//            )
//        }
//        TextButton(
//            onClick = {
//                setFilter(PersonalMediaStatus.PLANNING)
//                hideFilterSheet()
//            }, modifier = Modifier
//                .fillMaxWidth(), shape = RectangleShape
//        ) {
//            Text(
//                "Planning",
//                color = if (filter == PersonalMediaStatus.PLANNING) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
//            )
//        }
    }
}

@Composable
private fun ModalSheetTextButton(
    filterFunction: (PersonalMediaStatus) -> Unit,
    hideFilterSheet: () -> Unit,
    filter: PersonalMediaStatus,
    thisFilter: PersonalMediaStatus,
    icon: Int,
    name: String
) {
    Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.fillMaxWidth()) {
        TextButton(
            onClick = {
                filterFunction(thisFilter)
                hideFilterSheet()
            }, shape = RectangleShape
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = "icon",
                modifier = Modifier.padding(horizontal = Dimens.PaddingNormal)
            )
            Text(
                name,
                color = if (filter == thisFilter) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

data class MyMediaTypes(
    val filter: PersonalMediaStatus,
    val headLineId: Int
)

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
//        val listOfHeaders: List<MyMediaTypes> = listOf(
//            MyMediaTypes(
//                filter = PersonalMediaStatus.CURRENT,
//                headLineId = if (isAnime) R.string.watching else
//                    R.string.reading
//            )
//        )
//        items(listOfHeaders) {
//            stickyHeader {
//                MyMediaHeadline(
//                    if (isAnime) stringResource(id = R.string.watching) else stringResource(
//                        id = R.string.reading
//                    )
//                )
//            }
//            items(myMedia?.get(PersonalMediaStatus.CURRENT).orEmpty()) { media ->
//                MediaCard(
//                    navigateToDetails,
//                    increaseEpisodeProgress,
//                    increaseVolumeProgress,
//                    media,
//                    { showEditSheet(media) },
//                    isAnime = isAnime,
//                )
//            }
//        }

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
                    MyMediaHeadline(text = "Paused")
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
//            .padding(bottom = Dimens.PaddingSmall)
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
@OptIn(ExperimentalMaterial3Api::class)
fun DatePickerDialogue(
    label: String,
    initialValue: FuzzyDate?,
    setValue: (FuzzyDate?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = if (initialValue == null) {
            null
        } else {
            LocalDate(
                initialValue.year,
                initialValue.month,
                initialValue.day,
            ).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        },
    )
//    ExposedDropdownMenuBox(
//        expanded = expanded,
//        onExpandedChange = { expanded = !expanded },
//        modifier = Modifier.padding(Dimens.PaddingNormal),
//    ) {
    val time =
        datePickerState.selectedDateMillis?.let {
            Instant.fromEpochMilliseconds(
                it,
            ).toLocalDateTime(TimeZone.UTC)
        }
//    setValue(
//        if (time != null) {
//            FuzzyDate(
//                time.year,
//                time.monthNumber,
//                time.dayOfMonth,
//            )
//        } else {
//            null
//        },
//    )
    val timeString =
        if (time != null) {
            String.format(
                "%04d-%02d-%02d",
                time.year,
                time.monthNumber,
                time.dayOfMonth,
            )
        } else {
            ""
        }
    OutlinedTextField(
        // The `menuAnchor` modifier must be passed to the text field for correctness.
        modifier = Modifier
//                .menuAnchor()
            .clickable {
                expanded = !expanded
            }
            .padding(Dimens.PaddingNormal)
            .fillMaxWidth(),
        readOnly = true,
        enabled = false,
        value = timeString,
        onValueChange = { },
        label = { Text(label) },
        trailingIcon = {
            if (datePickerState.selectedDateMillis != null) IconButton(onClick = {
                datePickerState.selectedDateMillis = null
            }) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = stringResource(id = R.string.clear)
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
//            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
    )
//    }
    if (expanded) {
        DatePickerDialog(
            onDismissRequest = {
                // Dismiss the dialog when the user clicks outside the dialog or on the back
                // button. If you want to disable that functionality, simply use an empty
                // onDismissRequest.
                expanded = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        expanded = false
                    },
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        expanded = false
                    },
                ) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
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
//    val personalEpisodeProgress by remember { mutableIntStateOf(media.personalProgress) }
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
                                contentDescription = "star",
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
                                    contentDescription = "edit",
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
//                            Row(
//                                verticalAlignment = Alignment.CenterVertically,
//                                horizontalArrangement = Arrangement.SpaceEvenly
//                            ) {
//                                Icon(imageVector = Icons.Default.Add, contentDescription = "add")
        Text(
            text = label,
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

// @Preview(
//    showBackground = true,
//    group = "bottomSheet",
//    device = "spec:width=392.7dp,height=2000dp,dpi=440"
// )
// @Composable
// fun EditStatusScreenPreview() {
//    EditStatusScreen(
//        isAnime = false,
//        closeSheet = { },
//        Media(
//            title = "鬼滅の刃",
//            format = "TV",
//            episodeAmount = 11,
//            personalRating = 6.0,
//            personalProgress = 1,
//            note = ""
//        ),
//        saveStatus = {}
//    )
// }

@Preview(showBackground = true, group = "fullscreen")
@Composable
fun MyAnimePreview() {
    MyMedia(
        isAnime = true,
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

@Preview(showBackground = true, group = "Date picker")
@Composable
fun DatePickerPreview() {
    DatePickerDialogue("Start date", initialValue = FuzzyDate(2022, 7, 22), setValue = {})
}

@Preview(showBackground = true, group = "Date picker")
@Composable
fun DatePickerNoDatePreview() {
    DatePickerDialogue("Start date", initialValue = null, setValue = {})
}

@Preview(showBackground = true, group = "Dialog")
@Composable
fun RangeSliderTextPreview() {
    var rawScore by remember {
        mutableDoubleStateOf(23.0)
    }
    Column {
        SliderTextField(rawScore = rawScore, setRawScore = { rawScore = it })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, group = "Filter")
@Composable
fun FilterModalSheetPreview() {
    val state = rememberModalBottomSheetState()
    rememberCoroutineScope()
    LaunchedEffect(key1 = state.currentValue, block = { state.show() })
    FilterModalSheet(
        filterSheetState = state,
        hideFilterSheet = { },
        filter = PersonalMediaStatus.CURRENT,
        setFilter = {},
        isAnime = true
    )
}

@Preview(showBackground = true, group = "Filter")
@Composable
fun FilterComponentPreview() {
    ModalSheetTextButton(
        filterFunction = {},
        hideFilterSheet = { },
        filter = PersonalMediaStatus.PLANNING,
        thisFilter = PersonalMediaStatus.REPEATING,
        name = "Repeating",
        icon = R.drawable.outline_replay_24
    )
}