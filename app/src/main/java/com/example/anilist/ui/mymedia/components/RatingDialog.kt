package com.example.anilist.ui.mymedia.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.anilist.R
import com.example.anilist.data.models.AniMediaListEntry
import com.example.anilist.data.models.Media
import com.example.anilist.data.models.StatusUpdate
import com.example.anilist.ui.SliderTextField

@Composable
fun RatingDialog(
    setShowRatingDialog: (Boolean) -> Unit,
    saveStatus: (StatusUpdate) -> Unit,
    currentMedia: Media,
    setCurrentMedia: (Media) -> Unit,
) {
    AlertDialog(onDismissRequest = { setShowRatingDialog(false) }, dismissButton = {
        TextButton(
            onClick = {
                setShowRatingDialog(false)
            },
        ) {
            Text(text = stringResource(id = R.string.cancel))
        }
    }, confirmButton = {
        TextButton(
            onClick = {
                saveStatus(
                    StatusUpdate(
                        entryListId = currentMedia.mediaListEntry.listEntryId,
                        progressVolumes = null,
                        status = null,
                        scoreRaw = currentMedia.mediaListEntry.score.toInt(),
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
                        mediaId = currentMedia.id,
                    ),
                )
                setShowRatingDialog(false)
            },
        ) {
            Text(text = stringResource(id = R.string.save))
        }
    }, text = {
        Column {
            SliderTextField(
                rawScore = currentMedia.mediaListEntry.score,
                setRawScore = {
                    setCurrentMedia(
                        currentMedia.copy(
                            mediaListEntry =
                                currentMedia.mediaListEntry.copy(
                                    score = it,
                                ),
                        ),
                    )
                },
            )
        }
    })
}

@Preview(showBackground = true)
@Composable
fun RatingDialogPreview() {
    RatingDialog(
        setShowRatingDialog = {},
        saveStatus = {},
        currentMedia = Media(mediaListEntry = AniMediaListEntry(score = 25.0)),
        setCurrentMedia = {},
    )
}
