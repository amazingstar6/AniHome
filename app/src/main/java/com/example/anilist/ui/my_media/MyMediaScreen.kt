package com.example.anilist.ui.my_media

import android.os.Build
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberDatePickerState
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
import com.example.anilist.ui.Dimens
import java.time.Clock
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun MyMediaScreen(
    myMediaViewModel: MyMediaViewModel = hiltViewModel(),
    navigateToDetails: (Int) -> Unit,
    isAnime: Boolean,
    accessCode: String,
    onNavigateToStatusEditor: () -> Unit
) {
    val myAnime = myMediaViewModel.myAnime.observeAsState()
    val myManga = myMediaViewModel.myManga.observeAsState()
    MyMedia(
        isAnime = isAnime,
        myMedia = if (isAnime) myAnime.value.orEmpty() else myManga.value.orEmpty(),
        navigateToDetails = navigateToDetails,
        increaseEpisodeProgress = myMediaViewModel::increaseEpisodeProgress,
//        onNavigateToStatusEditor = onNavigateToStatusEditor,
        refreshList = {
            if (isAnime) myMediaViewModel.refreshAnime() else myMediaViewModel.refreshManga()
        },
        filter = {
        }
    )
}

enum class MediaStatus {
    ALL,
    WATCHING,
    REWATCHING,
    DROPPED,
    COMPLETED,
    PLANNING
}
sealed class BottomSheetScreen {
    object FilterScreen : BottomSheetScreen()
    class EditStatusScreen(val media: Media) : BottomSheetScreen()
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
private fun MyMedia(
    isAnime: Boolean,
    myMedia: List<Media>,
    navigateToDetails: (Int) -> Unit,
    refreshList: () -> Unit,
    increaseEpisodeProgress: (mediaId: Int, newProgress: Int) -> Unit,
//    onNavigateToStatusEditor: () -> Unit,
    filter: (MediaStatus) -> Unit
) {
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState(
        SheetState(initialValue = SheetValue.Hidden, skipPartiallyExpanded = true)
    )
    var currentBottomSheet: BottomSheetScreen by remember {
        mutableStateOf(BottomSheetScreen.FilterScreen)
    }
    val closeSheet: () -> Unit = {
        scope.launch {
            scaffoldState.bottomSheetState.hide()
        }
    }
    val openSheet: (BottomSheetScreen) -> Unit = {
        scope.launch {
            currentBottomSheet = it
            scaffoldState.bottomSheetState.expand()
        }
    }
    BottomSheetScaffold(
//        modifier = Modifier.pointerInput(Unit) {
//            detectTapGestures {
//                scope.launch {
//                    if (scaffoldState.bottomSheetState.isVisible) {
//                        scaffoldState.bottomSheetState.hide()
//                    }
//                }
//            }
//        },
        scaffoldState = scaffoldState,
        sheetContent = {
            currentBottomSheet.let { currentSheet ->
                when (currentSheet) {
                    BottomSheetScreen.FilterScreen -> {
                        FilterScreen(filter, scope, scaffoldState, isAnime, closeSheet)
                    }
                    is BottomSheetScreen.EditStatusScreen -> {
                        EditStatusScreen(isAnime, closeSheet, scope, currentSheet.media)
                    }
                }
            }
        },
        topBar = {
            TopAppBar(title = { Text(text = if (isAnime) "Watching" else "Reading") })
        }
//        floatingActionButton = {

//    }
    ) {
        val refreshScope = rememberCoroutineScope()
        var refreshing by remember { mutableStateOf(false) }

        fun refresh() = refreshScope.launch {
            refreshing = true
            refreshList()
            refreshing = false
        }

        val state = rememberPullRefreshState(refreshing, ::refresh)

        Box(Modifier.pullRefresh(state)) {
            LazyColumn(modifier = Modifier.padding(top = it.calculateTopPadding())) {
                if (!refreshing) {
                    items(myMedia) { media ->
                        MediaCard(
                            navigateToDetails,
                            increaseEpisodeProgress,
                            media
                        ) { openSheet(BottomSheetScreen.EditStatusScreen(media)) }
                    }
                }
            }
            PullRefreshIndicator(
                refreshing = refreshing,
                state = state,
                modifier = Modifier.align(Alignment.TopCenter)
            )
            ExtendedFloatingActionButton(
                text = { Text("Filter") },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.my_media_filter),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                },
                onClick = {
                    scope.launch {
//                        if (!scaffoldState.bottomSheetState.isVisible) {
//                            scaffoldState.bottomSheetState.expand()
//                        } else {
//                            scaffoldState.bottomSheetState.hide()
//                        }
                        openSheet(BottomSheetScreen.FilterScreen)
                    }
                },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(Dimens.PaddingNormal)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditStatusScreen(
    isAnime: Boolean,
    closeSheet: () -> Unit,
    scope: CoroutineScope,
    media: Media
) {
//    val options = listOf(
//        "Watching",
//        "Plan to watch",
//        "Completed",
//        "Rewatching",
//        "Paused",
//        "Dropped"
//    )
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = if (isAnime) "Edit anime status" else "Edit manga status"
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        scope.launch {
                            closeSheet()
                        }
                    },
                    modifier = Modifier.padding(Dimens.PaddingNormal)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "close"
                    )
                }
            },
            actions = {
                TextButton(onClick = { /*TODO*/ }) {
                    Text("Save")
                }
            }
        )
        DropDownMenuStatus()

        NumberTextField(
            media,
            "Episodes",
            initialValue = media.personalEpisodeProgress.toString(),
            hasSuffix = true
        )
        NumberTextField(
            media,
            "Total rewatches",
            initialValue = media.rewatches.toString(),
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
            onValueChange = {
                    newInput ->
                text = newInput
            },
            label = { Text("Score") },
            suffix = { Text(text = "/100") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text(text = "?") },
            modifier = Modifier
                .padding(Dimens.PaddingNormal)
//                .weight(1f)
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
                .padding(horizontal = Dimens.PaddingLarge, vertical = Dimens.PaddingNormal),
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

//        DatePicker(state = datePickerState, modifier = Modifier.padding(16.dp))
        // Decoupled snackbar host state from scaffold state for demo purposes.

        DatePickerDialogue("Start date")
        DatePickerDialogue("Finish date")

        var note by remember { mutableStateOf(media.note) }
        OutlinedTextField(
            value = note,
            label = { Text("Notes") },
            onValueChange = { note = it },
            modifier = Modifier
                .padding(Dimens.PaddingNormal)
                .weight(1f)
        )

        var check by remember {
            mutableStateOf(media.isPrivate)
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
                    ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Clock.systemDefaultZone()
                            .millis()
                    } else {
                        System.currentTimeMillis()
                    }
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
fun DropDownMenuStatus() {
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf(MediaStatus.values()[0]) }
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
                        selectedOptionText = selectionOption
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
            onValueChange = {
                    newInput ->
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterScreen(
    filter: (MediaStatus) -> Unit,
    scope: CoroutineScope,
    scaffoldState: BottomSheetScaffoldState,
    isAnime: Boolean,
    closeSheet: () -> Unit
) {
    val modifier = Modifier
        .fillMaxWidth()
    CenterAlignedTopAppBar(title = {
        Text(
            text = "Filter"
        )
    }, navigationIcon = {
        IconButton(
            onClick = {
                scope.launch {
                    closeSheet()
                }
            },
            modifier = Modifier.padding(Dimens.PaddingNormal)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "close"
            )
        }
    })
    TextButton(onClick = {
        filter(MediaStatus.ALL)
        scope.launch {
            scaffoldState.bottomSheetState.hide()
        }
    }, modifier = modifier, shape = RectangleShape) {
        Text("All")
    }
    TextButton(onClick = {
        filter(MediaStatus.WATCHING)
        scope.launch {
            scaffoldState.bottomSheetState.hide()
        }
    }, modifier = modifier, shape = RectangleShape) {
        Text(if (isAnime) "Watching" else "Reading")
    }
    TextButton(onClick = {
        filter(MediaStatus.REWATCHING)
        scope.launch {
            scaffoldState.bottomSheetState.hide()
        }
    }, modifier = modifier, shape = RectangleShape) {
        Text(if (isAnime) "Rewatching" else "Rereading")
    }
    TextButton(onClick = {
        filter(MediaStatus.DROPPED)
        scope.launch {
            scaffoldState.bottomSheetState.hide()
        }
    }, modifier = modifier, shape = RectangleShape) {
        Text("Dropped")
    }
    TextButton(onClick = {
        filter(MediaStatus.COMPLETED)
        scope.launch {
            scaffoldState.bottomSheetState.hide()
        }
    }, modifier = modifier, shape = RectangleShape) {
        Text("Completed")
    }
    TextButton(onClick = {
        filter(MediaStatus.PLANNING)
        scope.launch {
            scaffoldState.bottomSheetState.hide()
        }
    }, modifier = modifier, shape = RectangleShape) {
        Text("Planning")
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MediaCard(
    navigateToDetails: (Int) -> Unit,
    increaseEpisodeProgress: (mediaId: Int, newProgress: Int) -> Unit,
    media: Media,
    onNavigateToStatusEditor: () -> Unit
) {
    var personalEpisodeProgress by remember { mutableIntStateOf(media.personalEpisodeProgress) }
    Card(
        onClick = { navigateToDetails(media.id) },
        modifier = Modifier
            .padding(Dimens.PaddingSmall)
            .height(150.dp)
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
            Column(
                modifier = Modifier.padding(start = Dimens.PaddingNormal),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimens.PaddingSmall)
                ) {
                    Text(
                        text = media.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(200.dp),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 3
                    )
                    IconButton(
                        onClick = onNavigateToStatusEditor
//                        modifier = Modifier.weight(1f, false)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "edit"
                        )
                    }
                }
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Dimens.PaddingSmall)
                ) {
                    Text(
                        text = "$personalEpisodeProgress/${media.episodeAmount}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (media.episodeAmount != media.personalEpisodeProgress) {
                        OutlinedButton(
                            onClick = {
                                increaseEpisodeProgress(
                                    media.id,
                                    personalEpisodeProgress + 1
                                )
                                personalEpisodeProgress++
                            }
//                            modifier = Modifier.padding(bottom = Dimens.PaddingSmall)
                        ) {
//                            Row(
//                                verticalAlignment = Alignment.CenterVertically,
//                                horizontalArrangement = Arrangement.SpaceEvenly
//                            ) {
//                                Icon(imageVector = Icons.Default.Add, contentDescription = "add")
                            Text(
                                text = stringResource(id = R.string.plus_one),
                                color = MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.titleLarge
                            )
//                            }
                        }
                    }
                }
            }
            LinearProgressIndicator(
                progress = (personalEpisodeProgress / media.episodeAmount.toFloat()) * 100
            )
        }
    }
}

@Preview(
    showBackground = true,
    group = "bottomSheet",
    device = "spec:width=392.7dp,height=2000dp,dpi=440"
)
@Composable
fun EditStatusScreenPreview() {
    EditStatusScreen(
        isAnime = true,
        closeSheet = { },
        scope = rememberCoroutineScope(),
        Media(
            title = "鬼滅の刃",
            format = "TV",
            episodeAmount = 11,
            personalRating = 6.0,
            personalEpisodeProgress = 1,
            note = ""
        )
    )
}

@Preview(showBackground = true)
@Composable
fun MyAnimePreview() {
    MyMedia(
        isAnime = true,
        myMedia = listOf(
            Media(
                title = "鬼滅の刃",
                format = "TV",
                episodeAmount = 11,
                personalRating = 6.0,
                personalEpisodeProgress = 1,
                note = ""
            ),
            Media(
                title = "NARUTO -ナルト- 紅き四つ葉のクローバーを探せ",
                format = "TV",
                episodeAmount = 1012,
                personalRating = 10.0,
                personalEpisodeProgress = 120,
                note = ""
            ),
            Media(
                title = "ONE PIECE",
                format = "TV",
                episodeAmount = 101,
                personalRating = 3.0,
                personalEpisodeProgress = 101,
                note = ""
            )
        ),
        navigateToDetails = {},
        increaseEpisodeProgress = { _, _ -> },
//        onNavigateToStatusEditor = {},
        refreshList = {},
        filter = { }
    )
}
