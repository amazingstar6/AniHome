package com.example.anilist.ui.mediadetails

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import kotlinx.coroutines.Job

@Composable
fun rememberMediaDetailState(
    mediaId: Int,
    viewModel: MediaDetailsViewModel,
//    onDeleteTask: () -> Unit,
//    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
//    context: Context = LocalContext.current,
//    coroutineScope: CoroutineScope = rememberCoroutineScope()
): MediaDetailState {
//    val onDeleteTaskUpdateState by rememberUpdatedState(onDeleteTask)
    // If any of the parameters passed to `remember` change, a new instance of MediaDetailState
    // will be created, and the old one will be destroyed.
    return remember(mediaId, viewModel) {
        MediaDetailState(
            viewModel, mediaId
        )
    }
}


/**
 * Responsible for holding state and containing UI-related logic related to [MediaDetail].
 */
@Stable
class MediaDetailState(
    private val viewModel: MediaDetailsViewModel,
    mediaId: Int,
//    private val coroutineScope: CoroutineScope,
//    lifecycleOwner: LifecycleOwner,
//    context: Context
) {
    private var currentSnackbarJob: Job? = null

    init {
        // Start loading data
        viewModel.fetchMedia(mediaId)
    }
}