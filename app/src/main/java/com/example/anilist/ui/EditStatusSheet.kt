package com.example.anilist.ui

import android.util.Log
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.anilist.R
import com.example.anilist.data.models.Media
import com.example.anilist.data.models.StatusUpdate
import com.example.anilist.ui.mymedia.DatePickerDialogue
import com.example.anilist.ui.mymedia.DropDownMenuStatus
import com.example.anilist.ui.mymedia.NumberTextField
import kotlin.math.roundToInt

private const val TAG = "EditStatusSheet"

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun EditStatusModalSheet(
    editSheetState: SheetState,
    currentMedia: Media,
    isAnime: Boolean,
    hideEditSheet: () -> Unit,
    saveStatus: (StatusUpdate) -> Unit,
    deleteListEntry: (id: Int) -> Unit
) {
    var currentMedia1 by remember { mutableStateOf(currentMedia) }
    Log.d(TAG, "Current media remembered is $currentMedia1\ncurrent media passed as parameter is $currentMedia")
    ModalBottomSheet(sheetState = editSheetState,
        onDismissRequest = { hideEditSheet() }) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            var selectedOptionText by remember { mutableStateOf(currentMedia.personalStatus) }
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = currentMedia1.title,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = hideEditSheet,
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
            var sliderPosition by remember {
                mutableFloatStateOf(
                    currentMedia1.rawScore.roundToInt().toFloat(),
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
                    currentMedia1 =
                        currentMedia1.copy(rawScore = it.roundToInt().toDouble())
                    text = it.roundToInt().toString()
                },
                steps = 100,
            )

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