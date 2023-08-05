package com.example.anilist.ui.mymedia

import android.util.Log
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.anilist.R
import com.example.anilist.data.models.FuzzyDate
import com.example.anilist.data.models.Media
import com.example.anilist.data.models.StatusUpdate
import com.example.anilist.ui.Dimens
import com.example.anilist.ui.EditStatusModalSheet
import com.example.anilist.ui.mediadetails.LoadingCircle
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

enum class PersonalMediaStatus {
    CURRENT,
    PLANNING,
    COMPLETED,
    DROPPED,
    PAUSED,
    REPEATING,
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

@Composable
fun MyMediaScreen(
    myMediaViewModel: MyMediaViewModel = hiltViewModel(),
    navigateToDetails: (Int) -> Unit,
    isAnime: Boolean,
) {
    val myAnime by myMediaViewModel.myAnime.observeAsState()
    val myManga by myMediaViewModel.myManga.observeAsState()
    myMediaViewModel.fetchMyMedia(isAnime)
    if ((if (isAnime) myAnime else myManga) != null) {
        MyMedia(
            isAnime = isAnime,
            myMedia = if (isAnime) myAnime else myManga,
            navigateToDetails = navigateToDetails,
            saveStatus = {
                Log.d(TAG, "Clicked on save button!")
                myMediaViewModel.updateProgress(isAnime = isAnime, statusUpdate = it)
            },
            reloadMyMedia = {
                myMediaViewModel.fetchMyMedia(isAnime)
            }
        ) { myMediaViewModel.deleteEntry(it, isAnime) }
    } else {
        LoadingCircle()
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

    var filter by rememberSaveable { mutableStateOf(PersonalMediaStatus.UNKNOWN) }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = if (isAnime) "Watching" else "Reading") }, actions = {
                IconButton(
                    onClick = reloadMyMedia
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
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
                showEditSheet,
                isAnime,
            )
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
                ModalBottomSheet(
                    sheetState = filterSheetState,
                    onDismissRequest = hideFilterSheet,
                ) {
                    val filterFunction = { it: PersonalMediaStatus -> filter = it }
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
                        filterFunction, hideFilterSheet, filter
                    )
                    TextButton(
                        onClick = {
                            filterFunction(PersonalMediaStatus.CURRENT)
                            hideFilterSheet()
                        }, modifier = Modifier
                            .fillMaxWidth(), shape = RectangleShape
                    ) {
                        Text(
                            if (isAnime) stringResource(R.string.watching) else stringResource(R.string.reading),
                            color = if (filter == PersonalMediaStatus.CURRENT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    TextButton(
                        onClick = {
                            filterFunction(PersonalMediaStatus.REPEATING)
                            hideFilterSheet()
                        }, modifier = Modifier
                            .fillMaxWidth(), shape = RectangleShape
                    ) {
                        Text(
                            if (isAnime) stringResource(R.string.rewatching) else stringResource(R.string.rereading),
                            color = if (filter == PersonalMediaStatus.REPEATING) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    TextButton(
                        onClick = {
                            filterFunction(PersonalMediaStatus.DROPPED)
                            hideFilterSheet()
                        }, modifier = Modifier
                            .fillMaxWidth(), shape = RectangleShape
                    ) {
                        Text(
                            "Dropped",
                            color = if (filter == PersonalMediaStatus.DROPPED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    TextButton(
                        onClick = {
                            filterFunction(PersonalMediaStatus.COMPLETED)
                            hideFilterSheet()
                        }, modifier = Modifier
                            .fillMaxWidth(), shape = RectangleShape
                    ) {
                        Text(
                            "Completed",
                            color = if (filter == PersonalMediaStatus.COMPLETED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    TextButton(
                        onClick = {
                            filterFunction(PersonalMediaStatus.PLANNING)
                            hideFilterSheet()
                        }, modifier = Modifier
                            .fillMaxWidth(), shape = RectangleShape
                    ) {
                        Text(
                            "Planning",
                            color = if (filter == PersonalMediaStatus.PLANNING) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModalSheetTextButton(
    filterFunction: (PersonalMediaStatus) -> Unit,
    hideFilterSheet: () -> Unit,
    filter: PersonalMediaStatus
) {
    TextButton(
        onClick = {
            filterFunction(PersonalMediaStatus.UNKNOWN)
            hideFilterSheet()
        }, modifier = Modifier
            .fillMaxWidth(), shape = RectangleShape
    ) {
        Text(
            stringResource(R.string.all),
            color = if (filter == PersonalMediaStatus.UNKNOWN) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
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
    isAnime: Boolean,
) {
    LazyColumn(modifier = Modifier.padding(top = it.calculateTopPadding())) {
        if (filter == PersonalMediaStatus.UNKNOWN || filter == PersonalMediaStatus.CURRENT) {
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
                )
            }
        }
        if (filter == PersonalMediaStatus.UNKNOWN || filter == PersonalMediaStatus.REPEATING) {
            stickyHeader {
                MyMediaHeadline(
                    text = if (isAnime) stringResource(id = R.string.rewatching) else stringResource(
                        id = R.string.rereading
                    )
                )
            }
            items(myMedia?.get(PersonalMediaStatus.REPEATING).orEmpty()) { media ->
                MediaCard(
                    navigateToDetails,
                    increaseEpisodeProgress,
                    increaseVolumeProgress,
                    media,
                    { showEditSheet(media) },
                    isAnime = isAnime,
                )
            }
        }
        if (filter == PersonalMediaStatus.UNKNOWN || filter == PersonalMediaStatus.PLANNING) {
            stickyHeader {
                MyMediaHeadline(
                    text = if (isAnime) stringResource(R.string.plan_to_watch) else stringResource(
                        R.string.plan_to_read
                    )
                )
            }
            items(myMedia?.get(PersonalMediaStatus.PLANNING).orEmpty()) { media ->
                MediaCard(
                    navigateToDetails,
                    increaseEpisodeProgress,
                    increaseVolumeProgress,
                    media,
                    { showEditSheet(media) },
                    isAnime = isAnime,
                )
            }
        }
        if (filter == PersonalMediaStatus.UNKNOWN || filter == PersonalMediaStatus.COMPLETED) {
            stickyHeader { MyMediaHeadline(text = stringResource(R.string.completed)) }
            items(myMedia?.get(PersonalMediaStatus.COMPLETED).orEmpty()) { media ->
                MediaCard(
                    navigateToDetails,
                    increaseEpisodeProgress,
                    increaseVolumeProgress,
                    media,
                    { showEditSheet(media) },
                    isAnime = isAnime,
                )
            }
        }
        if (filter == PersonalMediaStatus.UNKNOWN || filter == PersonalMediaStatus.DROPPED) {
            stickyHeader { MyMediaHeadline(text = stringResource(R.string.dropped)) }
            items(myMedia?.get(PersonalMediaStatus.DROPPED).orEmpty()) { media ->
                MediaCard(
                    navigateToDetails,
                    increaseEpisodeProgress,
                    increaseVolumeProgress,
                    media,
                    { showEditSheet(media) },
                    isAnime = isAnime,
                )
            }
        }
        if (filter == PersonalMediaStatus.UNKNOWN || filter == PersonalMediaStatus.PAUSED) {
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
                )
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
@OptIn(ExperimentalMaterial3Api::class)
fun DropDownMenuStatus(
    selectedOptionText: PersonalMediaStatus,
    isAnime: Boolean,
    setSelectedOptionText: (PersonalMediaStatus) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    // We want to react on tap/press on TextField to show menu
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.padding(Dimens.PaddingNormal),
    ) {
        OutlinedTextField(
            // The `menuAnchor` modifier must be passed to the text field for correctness.
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            readOnly = true,
            value = selectedOptionText.getString(isAnime),
            onValueChange = {},
            label = { Text("Status") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            PersonalMediaStatus.values().forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption.getString(isAnime)) },
                    onClick = {
                        setSelectedOptionText(selectionOption)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    colors = MenuDefaults.itemColors(),
                )
            }
        }
    }
}

@Composable
fun NumberTextField(
    suffix: String,
    label: String,
    initialValue: Int,
    setValue: (Int) -> Unit,
    maxCount: Int,
) {
    var text by remember {
        mutableStateOf(initialValue.toString())
    }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { newInput ->
                try {
                    if (newInput.toInt() in 0..maxCount) {
                        text = newInput
                        setValue(
                            newInput.toInt()
                        )
                    } else {
                        text = ""
                        setValue(0)
                    }
                } catch (e: NumberFormatException) {
                    text = ""
                    setValue(0)
                }
            },
            singleLine = true,
            label = { Text(label) },
            suffix = { if (suffix.isNotEmpty()) Text(text = "/$suffix") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text(text = (0).toString()) },
            modifier = Modifier
                .padding(Dimens.PaddingNormal)
                .weight(1f),
        )
        TextButton(onClick = {
            text = try {
                if (text.toInt() < 1) {
                    setValue(0)
                    (0).toString()
                } else {
                    setValue(text.toInt().dec())
                    text.toInt().dec().toString()
                }
            } catch (e: NumberFormatException) {
                setValue(0)
                (0).toString()
            }
        }) {
            Text(text = stringResource(R.string.minus_one))
        }
        TextButton(onClick = {
            try {
                if (text.toInt() < maxCount) {
                    setValue(text.toInt().inc())
                    text = text.toInt().inc().toString()
                }
            } catch (e: NumberFormatException) {
                setValue(0)
                text = (0).toString()
            }
        }) {
            Text(text = stringResource(R.string.plus_one))
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun MediaCard(
    navigateToDetails: (Int) -> Unit,
    increaseEpisodeProgress: (mediaId: Int, newProgress: Int) -> Unit,
    increaseVolumeProgress: (mediaId: Int, newProgress: Int) -> Unit,
    media: Media,
    openEditStatusSheet: () -> Unit,
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
                            bottom = Dimens.PaddingSmall,
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
                        ) {
                            Icon(
                                painterResource(id = R.drawable.anime_details_rating_star),
                                contentDescription = "star",
                                tint = MaterialTheme.colorScheme.secondary,
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
                    Column(horizontalAlignment = Alignment.End) {
                        IconButton(
                            onClick = openEditStatusSheet,
                            //                        modifier = Modifier.weight(1f, false)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "edit",
                            )
                        }
                        if (isAnime) {
                            if (media.personalProgress != media.episodeAmount) {
                                IncreaseProgress(
                                    increaseEpisodeProgress,
                                    media.listEntryId,
                                    media.personalProgress,
                                    stringResource(id = R.string.plus_one),
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
