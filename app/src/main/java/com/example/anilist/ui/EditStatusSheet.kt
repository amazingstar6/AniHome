package com.example.anilist.ui

import android.widget.Toast
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
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.tooling.preview.Preview
import com.example.anilist.R
import com.example.anilist.data.models.FuzzyDate
import com.example.anilist.data.models.Media
import com.example.anilist.data.models.PersonalMediaStatus
import com.example.anilist.data.models.StatusUpdate
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import timber.log.Timber
import kotlin.math.roundToInt

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun EditStatusModalSheet(
    editSheetState: SheetState,
    unchangedMedia: Media,
    isAnime: Boolean,
    hideEditSheet: () -> Unit,
    saveStatus: (StatusUpdate) -> Unit,
    deleteListEntry: (id: Int) -> Unit
) {
    var currentMedia1 by remember { mutableStateOf(unchangedMedia) }
//    val unChangedMedia = currentMedia
    var showCloseConfirmation by remember {
        mutableStateOf(false)
    }
    ModalBottomSheet(sheetState = editSheetState,
        onDismissRequest = { hideEditSheet() }) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            /*do we need this? fixme */
            var selectedOptionText by remember { mutableStateOf(unchangedMedia.personalStatus) }
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
                    })
            }

            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = currentMedia1.title,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (unchangedMedia == currentMedia1) {
                                Timber.d("Unchanged media: $unchangedMedia\ncurrentMedia: $currentMedia1")
                                hideEditSheet()
                            } else {
                                Timber.d("Unchanged media: $unchangedMedia\ncurrentMedia: $currentMedia1")
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
                            // todo fill these
                            StatusUpdate(
                                entryListId = currentMedia1.listEntryId,
                                status = selectedOptionText,
                                scoreRaw = currentMedia1.rawScore.toInt(),
                                progress = currentMedia1.personalProgress,
                                progressVolumes = if (!isAnime) currentMedia1.personalVolumeProgress else null,
                                repeat = currentMedia1.rewatches,
                                priority = null,
                                privateToUser = currentMedia1.isPrivate,
                                notes = currentMedia1.note,
                                hiddenFromStatusList = null,
                                customLists = null,
                                advancedScores = null,
                                startedAt = currentMedia1.startedAt,
                                completedAt = currentMedia1.completedAt,
                            ),
                        )
                        hideEditSheet()
//                        reloadMyMedia()
                    }) {
                        Text("Save")
                    }
                },
            )

            DropDownMenuStatus(
                selectedOptionText,
                isAnime = isAnime
            ) { selectedOptionText = it }

            NumberTextField(
                suffix = if (isAnime) currentMedia1.episodeAmount.toString() else currentMedia1.chapters.toString(),
                label = if (isAnime) "Episodes" else "Chapters",
                initialValue = currentMedia1.personalProgress,
                setValue = {
                    currentMedia1 = currentMedia1.copy(personalProgress = it)
                },
                maxCount = if (isAnime) {
                    if (currentMedia1.episodeAmount != -1) {
                        currentMedia1.episodeAmount
                    } else {
                        Int.MAX_VALUE
                    }
                } else {
                    if (currentMedia1.chapters != -1) {
                        currentMedia1.chapters
                    } else {
                        Int.MAX_VALUE
                    }
                },
            )
            if (!isAnime) {
                NumberTextField(
                    suffix = currentMedia1.volumes.toString(),
                    label = "Volumes",
                    initialValue = currentMedia1.personalVolumeProgress,
                    setValue = {
                        currentMedia1 = currentMedia1.copy(personalProgress = it)
                    },
                    maxCount = currentMedia1.volumes,
                )
            }
            NumberTextField(
                suffix = "",
                label = if (isAnime) "Total rewatches" else "Total rereads",
                initialValue = currentMedia1.rewatches,
                setValue = { currentMedia1 = currentMedia1.copy(rewatches = it) },
                maxCount = Int.MAX_VALUE,
            )
            SliderTextField(
                currentMedia1.rawScore,
                setRawScore = { currentMedia1 = currentMedia1.copy(rawScore = it) })

            DatePickerDialogue(
                "Start date",
                initialValue = currentMedia1.startedAt,
                setValue = { currentMedia1 = currentMedia1.copy(startedAt = it) },
            )
            DatePickerDialogue(
                "Finish date",
                initialValue = currentMedia1.completedAt,
                setValue = { currentMedia1 = currentMedia1.copy(completedAt = it) },
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.PaddingNormal)
                    .clickable {
                        currentMedia1 = currentMedia1.copy(isPrivate = !currentMedia1.isPrivate)
                    }
            ) {
                Checkbox(checked = currentMedia1.isPrivate, onCheckedChange = {
                    currentMedia1 = currentMedia1.copy(isPrivate = it)
                })
                Text(
                    "Private",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            OutlinedTextField(
                value = currentMedia1.note,
                label = { Text("Notes") },
                onValueChange = {
                    currentMedia1 = currentMedia1.copy(note = it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = Dimens.PaddingNormal,
                        vertical = Dimens.PaddingLarge
                    ),
                minLines = 5,
                trailingIcon = {
                    if (currentMedia1.note.isNotBlank()) {
                        IconButton(onClick = {
                            currentMedia1 = currentMedia1.copy(note = "")
                        }) {
                            Box {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "clear",
                                    modifier = Modifier.align(Alignment.TopEnd)
                                )
                            }
                        }
                    }
                }
            )

            var showDeleteConfirmation by remember { mutableStateOf(false) }
            AnimatedVisibility(visible = showDeleteConfirmation) {
                AlertDialog(
                    title = { Text(text = "Delete entry?") },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmation = false }) {
                            Text(text = stringResource(R.string.cancel))
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            deleteListEntry(currentMedia1.listEntryId)
                            showDeleteConfirmation = false
                            hideEditSheet()
                        }) {
                            Text(stringResource(R.string.delete))
                        }
                    },
                    onDismissRequest = { showDeleteConfirmation = false })
            }

            Button(
                onClick = {
                    showDeleteConfirmation = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = Dimens.PaddingNormal,
                        end = Dimens.PaddingNormal,
                        bottom = Dimens.PaddingNormal
                    )
            ) {
                Text(
                    text = "Delete entry",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun SliderTextField(rawScore: Double, setRawScore: (Double) -> Unit) {
    var sliderPosition by remember {
        mutableFloatStateOf(
            rawScore.roundToInt().toFloat(),
        )
    }
    val valueRange = 0f..100f
    var text by remember {
        mutableStateOf("${sliderPosition.toInt()}")
    }
    val context = LocalContext.current
    OutlinedTextField(
        value = text,
        onValueChange = { newInput ->
            text = try {
                if (newInput.toFloat() in valueRange) {
                    newInput
                } else {
                    //fixme should we make a toast?
                    Toast.makeText(
                        context,
                        R.string.input_a_valid_value,
                        Toast.LENGTH_SHORT
                    ).show()
                    ""
                }
            } catch (e: NumberFormatException) {
                ""
            }
            sliderPosition = try {
                if (newInput.toFloat() in valueRange) {
                    newInput.toFloat()
                } else {
                    0f
                }
            } catch (e: NumberFormatException) {
                0f
            }
        },
        label = { Text("Score") },
        suffix = { Text(text = "/100") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
            .padding(Dimens.PaddingNormal),
    )
    Slider(
        modifier = Modifier
            .semantics {
                contentDescription = "Localized Description"
            }
            .padding(Dimens.PaddingNormal),
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
                if (selectionOption != PersonalMediaStatus.UNKNOWN) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun EditStatusModalSheetPreview() {
    EditStatusModalSheet(
        editSheetState = rememberModalBottomSheetState(),
        unchangedMedia = Media(),
        isAnime = true,
        hideEditSheet = { },
        saveStatus = { },
        deleteListEntry = { }
    )
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