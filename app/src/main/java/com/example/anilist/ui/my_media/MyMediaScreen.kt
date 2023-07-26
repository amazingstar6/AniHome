package com.example.anilist.ui.my_media

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.anilist.R
import com.example.anilist.data.models.Media
import com.example.anilist.data.models.StatusUpdate
import com.example.anilist.ui.Dimens
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.Clock
import kotlin.math.roundToInt

enum class MediaStatus {
    CURRENT,
    PLANNING,
    COMPLETED,
    DROPPED,
    PAUSED,
    REPEATING,
    UNKNOWN
}

private const val TAG = "MyMediaScreen"

@Composable
fun MyMediaScreen(
    myMediaViewModel: MyMediaViewModel = hiltViewModel(),
    navigateToDetails: (Int) -> Unit,
    isAnime: Boolean
) {
    val myAnime by myMediaViewModel.myAnime.observeAsState()
    val myManga by myMediaViewModel.myManga.observeAsState()
    val currentMap = if (isAnime) myAnime else myManga
    myMediaViewModel.fetchMyMedia(isAnime)
    MyMedia(
        isAnime = isAnime,
        myMedia = currentMap,
        navigateToDetails = navigateToDetails,
        increaseEpisodeProgress = myMediaViewModel::increaseEpisodeProgress,
    ) {
        Log.d(TAG, "Clicked on save button!")
        myMediaViewModel.updateProgress(it)
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MyMedia(
    isAnime: Boolean,
    myMedia: Map<MediaStatus, List<Media>>?,
    navigateToDetails: (Int) -> Unit,
    increaseEpisodeProgress: (mediaId: Int, newProgress: Int) -> Unit,
    saveStatus: (StatusUpdate) -> Unit
) {
    val modalSheetScope = rememberCoroutineScope()
    var currentMedia by remember {
        mutableStateOf(Media())
    }

    val filterSheetState = rememberModalBottomSheetState()
    val editSheetState = rememberModalBottomSheetState()


    val showFilterSheet: () -> Unit = { modalSheetScope.launch { filterSheetState.show() } }
    val hideFilterSheet: () -> Unit = { modalSheetScope.launch { filterSheetState.hide() } }

    val showEditSheet: (Media) -> Unit = {
        currentMedia = it
        modalSheetScope.launch { editSheetState.show() }
    }
    val hideEditSheet: () -> Unit = { modalSheetScope.launch { editSheetState.hide() } }

    var filter by remember { mutableStateOf(MediaStatus.UNKNOWN) }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = if (isAnime) "Watching" else "Reading") })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Filter") },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.my_media_filter),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                },
                onClick = showFilterSheet,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .padding(Dimens.PaddingNormal)
            )
        },
    ) {
        Box(
            Modifier
                .fillMaxSize()
        ) {
            MyMediaLazyList(
                it,
                filter,
                myMedia,
                navigateToDetails,
                increaseEpisodeProgress,
                showEditSheet,
                isAnime
            )
            if (editSheetState.isVisible) {
                ModalBottomSheet(sheetState = editSheetState, onDismissRequest = hideEditSheet) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        val currentStatus =
                            myMedia?.entries?.find { it.value.any { media -> media.id == currentMedia.id } }?.key
                                ?: MediaStatus.UNKNOWN
//                        val currentStatus =
//                            myMedia?.filterValues { entry -> entry.any { media -> media.id == currentMedia.id } }?.keys
                        var selectedOptionText by remember { mutableStateOf(currentStatus) }
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = if (isAnime) "Edit anime status" else "Edit manga status"
                                )
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = hideEditSheet,
                                    modifier = Modifier.padding(Dimens.PaddingNormal)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "close"
                                    )
                                }
                            },
                            actions = {
                                TextButton(onClick = {
                                    saveStatus(
                                        StatusUpdate(
                                            id = currentMedia.listEntryId,
                                            status = selectedOptionText,
                                            scoreRaw = null,
                                            progress = null,
                                            progressVolumes = null,
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
                                }) {
                                    Text("Save")
                                }
                            }
                        )
                        DropDownMenuStatus(selectedOptionText) { selectedOptionText = it }

                        NumberTextField(
                            media = currentMedia,
                            label = "Episodes",
                            initialValue = currentMedia.personalProgress.toString(),
                            hasSuffix = true
                        )
                        NumberTextField(
                            media = currentMedia,
                            label = "Total rewatches",
                            initialValue = currentMedia.rewatches.toString(),
                            hasSuffix = false
                        )
                        var text by remember {
                            mutableStateOf("")
                        }

                        val sliderState = remember {
                            SliderState(
                                valueRange = 0f..100f,
                                onValueChangeFinished = {
                                    // launch some business logic update with the state you hold
                                    // viewModel.updateSelectedSliderValue(sliderPosition)
                                    // todo
                                },
                                steps = 100
                            )
                        }
                        OutlinedTextField(
                            value = sliderState.value.roundToInt().toString(),
                            onValueChange = { newInput ->
                                text = newInput
                            },
                            label = { Text("Score") },
                            suffix = { Text(text = "/100") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            placeholder = { Text(text = "?") },
                            modifier = Modifier
                                .padding(Dimens.PaddingNormal)
                        )
                        val interactionSource = MutableInteractionSource()
                        val colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.secondary,
                            activeTrackColor = MaterialTheme.colorScheme.secondary
                        )
                        Slider(
                            state = sliderState,
                            modifier = Modifier
                                .semantics {
                                    contentDescription = "Localized Description"
                                }
                                .padding(
                                    horizontal = Dimens.PaddingLarge,
                                    vertical = Dimens.PaddingNormal
                                ),
                            interactionSource = interactionSource,
                            thumb = {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = null,
                                    modifier = Modifier.size(ButtonDefaults.IconSize),
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            },
                            track = {
                                SliderDefaults.Track(
                                    colors = colors,
                                    sliderState = sliderState
                                )
                            }
                        )

                        DatePickerDialogue("Start date")
                        DatePickerDialogue("Finish date")

                        var note by remember { mutableStateOf(currentMedia.note) }
                        OutlinedTextField(
                            value = note,
                            label = { Text("Notes") },
                            onValueChange = { note = it },
                            modifier = Modifier
                                .padding(Dimens.PaddingNormal)
                                .weight(1f)
                        )

                        var check by remember {
                            mutableStateOf(currentMedia.isPrivate)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Checkbox(checked = check, onCheckedChange = { check = it })
                            Text(
                                "Private",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            //fixme animation not working
            AnimatedVisibility(
                visible = filterSheetState.isVisible,
                enter = fadeIn(),
                exit = ExitTransition.None
            ) {
                ModalBottomSheet(
                    sheetState = filterSheetState,
                    onDismissRequest = hideFilterSheet
                ) {
                    val filterFunction = { it: MediaStatus -> filter = it }
                    val modifier = Modifier
                        .fillMaxWidth()
                    CenterAlignedTopAppBar(title = {
                        Text(
                            text = "Filter"
                        )
                    }, navigationIcon = {
                        IconButton(
                            onClick = hideFilterSheet,
                            modifier = Modifier.padding(Dimens.PaddingNormal)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "close"
                            )
                        }
                    })
                    TextButton(onClick = {
                        filterFunction(MediaStatus.UNKNOWN)
                        hideFilterSheet()
                    }, modifier = modifier, shape = RectangleShape) {
                        Text("All")
                    }
                    TextButton(onClick = {
                        filterFunction(MediaStatus.CURRENT)
                        hideFilterSheet()
                    }, modifier = modifier, shape = RectangleShape) {
                        Text(if (isAnime) "Watching" else "Reading")
                    }
                    TextButton(onClick = {
                        filterFunction(MediaStatus.REPEATING)
                        hideFilterSheet()
                    }, modifier = modifier, shape = RectangleShape) {
                        Text(if (isAnime) "Rewatching" else "Rereading")
                    }
                    TextButton(onClick = {
                        filterFunction(MediaStatus.DROPPED)
                        hideFilterSheet()
                    }, modifier = modifier, shape = RectangleShape) {
                        Text("Dropped")
                    }
                    TextButton(onClick = {
                        filterFunction(MediaStatus.COMPLETED)
                        hideFilterSheet()
                    }, modifier = modifier, shape = RectangleShape) {
                        Text("Completed")
                    }
                    TextButton(onClick = {
                        filterFunction(MediaStatus.PLANNING)
                        hideFilterSheet()
                    }, modifier = modifier, shape = RectangleShape) {
                        Text("Planning")
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun MyMediaLazyList(
    it: PaddingValues,
    filter: MediaStatus,
    myMedia: Map<MediaStatus, List<Media>>?,
    navigateToDetails: (Int) -> Unit,
    increaseEpisodeProgress: (mediaId: Int, newProgress: Int) -> Unit,
    showEditSheet: (Media) -> Unit,
    isAnime: Boolean
) {
    LazyColumn(modifier = Modifier.padding(top = it.calculateTopPadding())) {
        if (filter == MediaStatus.UNKNOWN || filter == MediaStatus.CURRENT) {
            stickyHeader {
                MyMediaHeadline("Current")
            }
            items(myMedia?.get(MediaStatus.CURRENT).orEmpty()) { media ->
                MediaCard(
                    navigateToDetails,
                    increaseEpisodeProgress,
                    media,
                    { showEditSheet(media) },
                    isAnime = isAnime
                )
            }
        }
        if (filter == MediaStatus.UNKNOWN || filter == MediaStatus.REPEATING) {
            stickyHeader { MyMediaHeadline(text = "Repeating") }
            items(myMedia?.get(MediaStatus.REPEATING).orEmpty()) { media ->
                MediaCard(
                    navigateToDetails,
                    increaseEpisodeProgress,
                    media,
                    { showEditSheet(media) },
                    isAnime = isAnime
                )
            }
        }
        if (filter == MediaStatus.UNKNOWN || filter == MediaStatus.PLANNING) {
            stickyHeader { MyMediaHeadline(text = "Planning") }
            items(myMedia?.get(MediaStatus.PLANNING).orEmpty()) { media ->
                MediaCard(
                    navigateToDetails,
                    increaseEpisodeProgress,
                    media,
                    { showEditSheet(media) },
                    isAnime = isAnime
                )
            }
        }
        if (filter == MediaStatus.UNKNOWN || filter == MediaStatus.COMPLETED) {
            stickyHeader { MyMediaHeadline(text = "Completed") }
            items(myMedia?.get(MediaStatus.COMPLETED).orEmpty()) { media ->
                MediaCard(
                    navigateToDetails,
                    increaseEpisodeProgress,
                    media,
                    { showEditSheet(media) },
                    isAnime = isAnime
                )
            }
        }
        if (filter == MediaStatus.UNKNOWN || filter == MediaStatus.DROPPED) {
            stickyHeader { MyMediaHeadline(text = "Dropped") }
            items(myMedia?.get(MediaStatus.DROPPED).orEmpty()) { media ->
                MediaCard(
                    navigateToDetails,
                    increaseEpisodeProgress,
                    media,
                    { showEditSheet(media) },
                    isAnime = isAnime
                )
            }
        }
        if (filter == MediaStatus.UNKNOWN || filter == MediaStatus.PAUSED) {
            stickyHeader {
                MyMediaHeadline(text = "Paused")
            }
            items(myMedia?.get(MediaStatus.PAUSED).orEmpty()) { media ->
                MediaCard(
                    navigateToDetails,
                    increaseEpisodeProgress,
                    media,
                    { showEditSheet(media) },
                    isAnime = isAnime
                )
            }
        }
    }
}

@Composable
private fun MyMediaHeadline(text: String) {
    Surface(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = Dimens.PaddingNormal)
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DatePickerDialogue(label: String) {
    var expanded by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.padding(Dimens.PaddingNormal)
    ) {
        val time =
            Instant.fromEpochMilliseconds(
                datePickerState.selectedDateMillis
                    ?: Clock.systemDefaultZone()
                        .millis()
            ).toLocalDateTime(TimeZone.UTC)
        val timeString =
            String.format(
                "%04d-%02d-%02d",
                time.year,
                time.monthNumber,
                time.dayOfMonth
            )
        OutlinedTextField(
            // The `menuAnchor` modifier must be passed to the text field for correctness.
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            readOnly = true,
            value = timeString,
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
    }
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
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        expanded = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DropDownMenuStatus(
    selectedOptionText: MediaStatus,
    setSelectedOptionText: (MediaStatus) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    // We want to react on tap/press on TextField to show menu
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.padding(Dimens.PaddingNormal)
    ) {
        OutlinedTextField(
            // The `menuAnchor` modifier must be passed to the text field for correctness.
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            readOnly = true,
            value = selectedOptionText.name,
            onValueChange = {},
            label = { Text("Status") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            MediaStatus.values().forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption.name) },
                    onClick = {
                        setSelectedOptionText(selectionOption)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    colors = MenuDefaults.itemColors()
                )
            }
        }
    }
}

@Composable
private fun NumberTextField(media: Media, label: String, initialValue: String, hasSuffix: Boolean) {
    var text by remember {
        mutableStateOf(initialValue)
    }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { newInput ->
                text = newInput
            },
            label = { Text(label) },
            suffix = { if (hasSuffix) Text(text = "/${media.episodeAmount}") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text(text = "?") },
            modifier = Modifier
                .padding(Dimens.PaddingNormal)
                .weight(1f)
        )
        TextButton(onClick = {
            text = try {
                if (text == "" || text.toInt() < 1) {
                    0.toString()
                } else {
                    text.toInt().dec().toString()
                }
            } catch (e: NumberFormatException) {
                ""
            }
        }) {
            Text(text = stringResource(R.string.minus_one))
        }
        TextButton(onClick = {
            text = try {
                if (text == "") {
                    1.toString()
                } else {
                    text.toInt().inc().toString()
                }
            } catch (e: NumberFormatException) {
                ""
            }
        }) {
            Text(text = stringResource(R.string.plus_one))
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MediaCard(
    navigateToDetails: (Int) -> Unit,
    increaseEpisodeProgress: (mediaId: Int, newProgress: Int) -> Unit,
    media: Media,
    onNavigateToStatusEditor: () -> Unit,
    isAnime: Boolean
) {
    var personalEpisodeProgress by remember { mutableIntStateOf(media.personalProgress) }
    Card(
        onClick = { navigateToDetails(media.id) },
        modifier = Modifier
            .padding(Dimens.PaddingSmall)
            .height(150.dp)
            .fillMaxWidth()
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
                    .clip(MaterialTheme.shapes.medium)
            )
//            Column(
//                modifier = Modifier.padding(start = Dimens.PaddingNormal),
//                verticalArrangement = Arrangement.SpaceBetween
//            ) {
//                Row(
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(top = Dimens.PaddingSmall)
//                ) {
//
//
//                }
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = Dimens.PaddingSmall,
                        bottom = Dimens.PaddingSmall,
                        start = Dimens.PaddingSmall
                    )
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = media.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(175.dp),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2
                    )
                    Text(
                        text = media.format,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Icon(
                            painterResource(id = R.drawable.anime_details_rating_star),
                            contentDescription = "star",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = media.personalRating.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (isAnime) {
                        Text(
                            text = "$personalEpisodeProgress/${media.episodeAmount}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Text(
                            text = "${media.personalProgress}/${media.chapters}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${media.personalVolumeProgress}/${media.volumes}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    IconButton(
                        onClick = onNavigateToStatusEditor
//                        modifier = Modifier.weight(1f, false)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "edit"
                        )
                    }
                    if (isAnime) {
                        if (media.personalProgress != media.episodeAmount) {
                            IncreaseProgress(
                                increaseEpisodeProgress,
                                media,
                                personalEpisodeProgress,
                                stringResource(id = R.string.plus_one)
                            )
                        }
                    } else {
                        if (media.chapters != media.personalProgress) {
                            IncreaseProgress(
                                increaseEpisodeProgress,
                                media,
                                personalEpisodeProgress,
                                stringResource(id = R.string.plus_one_chapter)
                            )
                        }
                        if (media.personalVolumeProgress != media.volumes) {
                            IncreaseProgress(
                                increaseEpisodeProgress,
                                media,
                                personalEpisodeProgress,
                                stringResource(
                                    id = R.string.plus_one_volume
                                )
                            )
                        }
                    }
                }
            }
        }
        LinearProgressIndicator(
            progress = (personalEpisodeProgress / media.episodeAmount.toFloat()) * 100
        )
    }
}

@Composable
private fun IncreaseProgress(
    increaseEpisodeProgress: (mediaId: Int, newProgress: Int) -> Unit,
    media: Media,
    personalEpisodeProgress: Int,
    label: String
) {
    var personalEpisodeProgress1 = personalEpisodeProgress
    OutlinedButton(
        onClick = {
            increaseEpisodeProgress(
                media.id,
                personalEpisodeProgress1 + 1
            )
            personalEpisodeProgress1++
        },
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
//                            modifier = Modifier.padding(bottom = Dimens.PaddingSmall)
    ) {
//                            Row(
//                                verticalAlignment = Alignment.CenterVertically,
//                                horizontalArrangement = Arrangement.SpaceEvenly
//                            ) {
//                                Icon(imageVector = Icons.Default.Add, contentDescription = "add")
        Text(
            text = label,
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

//@Preview(
//    showBackground = true,
//    group = "bottomSheet",
//    device = "spec:width=392.7dp,height=2000dp,dpi=440"
//)
//@Composable
//fun EditStatusScreenPreview() {
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
//}

@Preview(showBackground = true, group = "fullscreen")
@Composable
fun MyAnimePreview() {
    MyMedia(
        isAnime = false,
        myMedia = mapOf(
            MediaStatus.CURRENT to listOf(
                Media(
                    title = "鬼滅の刃",
                    format = "TV",
                    chapters = 80,
                    volumes = 6,
                    personalRating = 6.0,
                    personalProgress = 20,
                    personalVolumeProgress = 3,
                    note = ""
                ),
                Media(
                    title = "NARUTO -ナルト- 紅き四つ葉のクローバーを探せ",
                    format = "TV",
                    chapters = 1012,
                    personalRating = 10.0,
                    personalProgress = 120,
                    note = "",
                    personalVolumeProgress = 2,
                    volumes = 5
                ),
                Media(
                    title = "ONE PIECE",
                    format = "TV",
                    episodeAmount = 101,
                    personalRating = 3.0,
                    personalProgress = 101,
                    note = "",
                    volumes = 3,
                    personalVolumeProgress = 2
                )
            )
        ),
        navigateToDetails = {},
        increaseEpisodeProgress = { _, _ -> },
        saveStatus = { }
    )
}
