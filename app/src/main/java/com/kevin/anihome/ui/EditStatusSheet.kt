package com.kevin.anihome.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.kevin.anihome.R
import com.kevin.anihome.data.models.AniMediaListEntry
import com.kevin.anihome.data.models.AniPersonalMediaStatus
import com.kevin.anihome.data.models.FuzzyDate
import com.kevin.anihome.data.models.Media
import com.kevin.anihome.data.models.StatusUpdate
import com.kevin.anihome.utils.Utils
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import timber.log.Timber
import java.util.Locale
import kotlin.math.roundToInt

const val TAG = "EditStatusSheet"

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun EditStatusModalSheet(
    editSheetState: SheetState,
    media: Media,
    unChangeListEntry: AniMediaListEntry,
    isAnime: Boolean,
    hideEditSheet: () -> Unit,
    saveStatus: (StatusUpdate, Boolean) -> Unit,
    deleteListEntry: (id: Int) -> Unit,
) {
    var currentListEntry by remember { mutableStateOf(unChangeListEntry) }
    var showCloseConfirmation by remember {
        mutableStateOf(false)
    }

    val datePickerStateCompletedAt =
        rememberDatePickerState(
            initialSelectedDateMillis =
                if (currentListEntry.completedAt == null) {
                    null
                } else if (currentListEntry.completedAt?.day == null || currentListEntry.completedAt?.month == null || currentListEntry.completedAt?.year == null) {
                    null
                } else {
                    Timber.d("completedAt: ${currentListEntry.completedAt}")
                    LocalDateTime(
                        currentListEntry.completedAt!!.year,
                        currentListEntry.completedAt!!.month,
                        currentListEntry.completedAt!!.day,
                        0,
                        0
                    ).toInstant(TimeZone.UTC).toEpochMilliseconds()
                },
        )
    Timber.d("datePickerStateCompletedAt: ${datePickerStateCompletedAt.selectedDateMillis}")

    val datePickerStateStartedAt =
        rememberDatePickerState(
            initialSelectedDateMillis =
                if (currentListEntry.startedAt == null) {
                    null
                } else if (currentListEntry.startedAt?.day == null || currentListEntry.startedAt?.month == null || currentListEntry.startedAt?.year == null) {
                    null
                }
                else {
                    LocalDateTime(
                        currentListEntry.startedAt!!.year,
                        currentListEntry.startedAt!!.month,
                        currentListEntry.startedAt!!.day,
                        0,
                        0
                    ).toInstant(TimeZone.UTC).toEpochMilliseconds()
                },
        )

    ModalBottomSheet(
        sheetState = editSheetState,
        onDismissRequest = { hideEditSheet() },
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            // do we need this? fixme
//            var selectedOptionText by remember { mutableStateOf(unChangeListEntry.status) }
            AnimatedVisibility(showCloseConfirmation) {
                AlertDialog(
                    title = { Text(text = "Discard pending changes?") },
                    dismissButton = {
                        TextButton(onClick = {
                            showCloseConfirmation = false
                            hideEditSheet()
                        }) {
                            Text(text = stringResource(R.string.discard))
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showCloseConfirmation = false
                        }) {
                            Text(stringResource(R.string.keep_editing))
                        }
                    },
                    onDismissRequest = {
                        showCloseConfirmation = false
                        hideEditSheet()
                    },
                )
            }

            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = media.title,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (unChangeListEntry == currentListEntry) {
                                Timber.d("Unchanged media: $unChangeListEntry\ncurrentMedia: $currentListEntry")
                                hideEditSheet()
                            } else {
                                Timber.d("Unchanged media: $unChangeListEntry\ncurrentMedia: $currentListEntry")
                                showCloseConfirmation = true
                            }
                        },
                        modifier = Modifier.padding(Dimens.PaddingNormal),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "close",
                        )
                    }
                },
                actions = {
                    TextButton(onClick = {
                        saveStatus(
                            StatusUpdate(
                                entryListId = currentListEntry.listEntryId,
                                status = currentListEntry.status,
                                scoreRaw = currentListEntry.score.toInt(),
                                progress = currentListEntry.progress,
                                progressVolumes = if (!isAnime) currentListEntry.progressVolumes else null,
                                repeat = currentListEntry.repeat,
                                priority = null,
                                privateToUser = currentListEntry.private,
                                notes = currentListEntry.notes,
                                hiddenFromStatusList = null,
                                customLists = null,
                                advancedScores = null,
                                startedAt = currentListEntry.startedAt,
                                completedAt = currentListEntry.completedAt,
                                mediaId = media.id,
                            ),
                            false,
                        )
                        hideEditSheet()
                    }) {
                        Text("Save")
                    }
                },
            )

            DropDownMenuStatus(
                currentListEntry.status,
                isAnime = isAnime,
            ) { currentListEntry = currentListEntry.copy(status = it) }

            NumberTextField(
                suffix = if (isAnime) media.episodeAmount.toString() else media.chapters.toString(),
                label = if (isAnime) "Episodes" else "Chapters",
                initialValue = currentListEntry.progress,
                setValue = {
                    currentListEntry = currentListEntry.copy(progress = it)
                    if ((if (isAnime) media.episodeAmount else media.chapters)
                        == currentListEntry.progress
                    ) {
                        currentListEntry =
                            currentListEntry.copy(
                                status = AniPersonalMediaStatus.COMPLETED,
                                completedAt = Utils.getCurrentDay(),
                            )
                        datePickerStateCompletedAt.selectedDateMillis = Utils.getCurrentDayMillisEpoch()
                    }
                },
                maxCount =
                    if (isAnime) {
                        if (media.episodeAmount != -1) {
                            media.episodeAmount
                        } else {
                            Int.MAX_VALUE
                        }
                    } else {
                        if (media.chapters != -1) {
                            media.chapters
                        } else {
                            Int.MAX_VALUE
                        }
                    },
            )
            if (!isAnime) {
                NumberTextField(
                    suffix = media.volumes.toString(),
                    label = "Volumes",
                    initialValue = currentListEntry.progressVolumes,
                    setValue = {
                        currentListEntry = currentListEntry.copy(progress = it)
                    },
                    maxCount = media.volumes,
                )
            }
            NumberTextField(
                suffix = "",
                label = if (isAnime) "Total rewatches" else "Total rereads",
                initialValue = currentListEntry.repeat,
                setValue = { currentListEntry = currentListEntry.copy(repeat = it) },
                maxCount = Int.MAX_VALUE,
            )
            SliderTextField(
                currentListEntry.score,
                setRawScore = { currentListEntry = currentListEntry.copy(score = it) },
            )

            DatePickerDialogue(
                "Start date",
                datePickerState = datePickerStateStartedAt,
                setDateListEntry = { currentListEntry = currentListEntry.copy(startedAt = it) },
            )
            DatePickerDialogue(
                "Finish date",
                datePickerState = datePickerStateCompletedAt,
                setDateListEntry = { currentListEntry = currentListEntry.copy(completedAt = it) },
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.PaddingSmall)
                        .clickable {
                            currentListEntry =
                                currentListEntry.copy(private = !currentListEntry.private)
                        },
            ) {
                Checkbox(checked = currentListEntry.private, onCheckedChange = {
                    currentListEntry = currentListEntry.copy(private = it)
                })
                Text(
                    "Private",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            OutlinedTextField(
                value = currentListEntry.notes,
                label = { Text("Notes") },
                onValueChange = {
                    currentListEntry = currentListEntry.copy(notes = it)
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = Dimens.PaddingNormal,
                            vertical = Dimens.PaddingSmall,
                        ),
                minLines = 5,
                trailingIcon = {
                    if (currentListEntry.notes.isNotBlank()) {
                        IconButton(onClick = {
                            currentListEntry = currentListEntry.copy(notes = "")
                        }) {
                            Box {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "clear",
                                    modifier = Modifier.align(Alignment.TopEnd),
                                )
                            }
                        }
                    }
                },
            )

            var showDeleteConfirmation by remember { mutableStateOf(false) }
            AnimatedVisibility(visible = showDeleteConfirmation) {
                AlertDialog(
                    title = { Text(text = "Delete entry \"${media.title}\"?") },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmation = false }) {
                            Text(text = stringResource(R.string.cancel))
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            deleteListEntry(currentListEntry.listEntryId)
                            showDeleteConfirmation = false
                            hideEditSheet()
                        }) {
                            Text(stringResource(R.string.delete))
                        }
                    },
                    onDismissRequest = { showDeleteConfirmation = false },
                )
            }

            Button(
                onClick = {
                    showDeleteConfirmation = true
                },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            start = Dimens.PaddingNormal,
                            end = Dimens.PaddingNormal,
                            bottom = Dimens.PaddingNormal,
                        ),
            ) {
                Text(
                    text = "Delete entry",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
fun SliderTextField(
    rawScore: Double,
    setRawScore: (Double) -> Unit,
) {
    var sliderPosition by remember {
        mutableFloatStateOf(
            rawScore.roundToInt().toFloat(),
        )
    }
    val valueRange = 0f..100f
    var text by remember {
        mutableStateOf("${sliderPosition.toInt()}")
    }
    OutlinedTextField(
        value = text,
        onValueChange = { newInput ->
            text =
                try {
                    if (newInput.toFloat() in valueRange) {
                        setRawScore(newInput.toDouble())
                        newInput
                    } else {
                        ""
                    }
                } catch (e: NumberFormatException) {
                    ""
                }
            sliderPosition =
                try {
                    if (newInput.toFloat() in valueRange) {
                        newInput.toFloat()
                    } else {
                        0f
                    }
                } catch (e: NumberFormatException) {
                    0f
                }
        },
        label = { Text(stringResource(R.string.score)) },
        suffix = { Text(text = "/100") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier =
            Modifier
                .padding(horizontal = Dimens.PaddingNormal, vertical = Dimens.PaddingSmall),
    )
    Slider(
        modifier =
            Modifier
                .semantics {
                    contentDescription = "Localized Description"
                }
                .padding(horizontal = Dimens.PaddingLarge, vertical = Dimens.PaddingNormal),
        valueRange = valueRange,
        value = sliderPosition,
        onValueChange = {
            sliderPosition = it
            setRawScore(it.roundToInt().toDouble())
            text = it.roundToInt().toString()
        },
        steps = 100,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DropDownMenuStatus(
    selectedOptionText: AniPersonalMediaStatus,
    isAnime: Boolean,
    setSelectedOptionText: (AniPersonalMediaStatus) -> Unit,
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
            modifier =
                Modifier
                    // TODO deprecated
                    .menuAnchor()
                    .fillMaxWidth(),
            readOnly = true,
            value =
                selectedOptionText.toString(
                    isAnime = isAnime,
                    context = LocalContext.current,
                    unknownString =
                        stringResource(
                            R.string.none,
                        ),
                ),
            onValueChange = {},
            label = { Text("Status") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            AniPersonalMediaStatus.entries.forEach { selectionOption ->
                if (selectionOption != AniPersonalMediaStatus.UNKNOWN) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                selectionOption.toString(
                                    isAnime = isAnime,
                                    context = LocalContext.current,
                                ),
                            )
                        },
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
                            newInput.toInt(),
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
            modifier =
                Modifier
                    .padding(horizontal = Dimens.PaddingNormal, vertical = Dimens.PaddingSmall)
                    .weight(1f),
        )
        TextButton(onClick = {
            text =
                try {
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
@OptIn(ExperimentalMaterial3Api::class)
fun DatePickerDialogue(
    label: String,
    datePickerState: DatePickerState,
    setDateListEntry: (FuzzyDate?) -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    val dateBeforeChange = datePickerState.selectedDateMillis
    Timber.d("dateBeforeChange: $dateBeforeChange")
    val dateBeforeChangeConverted =
        dateBeforeChange?.let {
            Instant.fromEpochMilliseconds(
                it,
            ).toLocalDateTime(TimeZone.currentSystemDefault())
        }
    Timber.d("dateBeforeChangeConverted: $dateBeforeChangeConverted")
    val dateBeforeChangeString =
        if (dateBeforeChangeConverted != null) {
            String.format(Locale.UK,
                "%04d-%02d-%02d",
                dateBeforeChangeConverted.year,
                dateBeforeChangeConverted.monthNumber,
                dateBeforeChangeConverted.dayOfMonth,
            )
        } else {
            ""
        }
    Timber.d("dateBeforeChangeString: $dateBeforeChangeString")
    OutlinedTextField(
        modifier =
            Modifier
//                .menuAnchor()
                .clickable {
                    expanded = !expanded
                }
                .padding(Dimens.PaddingNormal)
                .fillMaxWidth(),
        readOnly = true,
        enabled = false,
        value = dateBeforeChangeString,
        onValueChange = {},
        label = { Text(label) },
        trailingIcon = {
            if (datePickerState.selectedDateMillis != null) {
                IconButton(onClick = {
                    datePickerState.selectedDateMillis = null
                    setDateListEntry(null)
                }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(id = R.string.clear),
                    )
                }
            }
        },
        colors =
            OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
    )
    if (expanded) {
        DatePickerDialog(
            onDismissRequest = {
                expanded = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        expanded = false
                        val newDate = datePickerState.selectedDateMillis?.let {
                            Instant.fromEpochMilliseconds(
                                it,
                            ).toLocalDateTime(TimeZone.currentSystemDefault())
                        }
                        val newDateFuzzy : FuzzyDate? = if (newDate != null) FuzzyDate(
                            year = newDate.year,
                            month = newDate.month.value,
                            day = newDate.dayOfMonth
                        ) else {null}
                        setDateListEntry(newDateFuzzy)
                    },
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        expanded = false
                        datePickerState.selectedDateMillis = dateBeforeChange
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, group = "Date picker")
@Composable
fun DatePickerPreview() {
    DatePickerDialogue("Start date", datePickerState = rememberDatePickerState())
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, group = "Date picker")
@Composable
fun DatePickerNoDatePreview() {
    DatePickerDialogue("Start date", datePickerState = rememberDatePickerState())
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
