package com.example.anilist.ui.mymedia.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.anilist.R
import com.example.anilist.data.models.AniPersonalMediaStatus
import com.example.anilist.ui.Dimens

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun FilterSheet(
    filterSheetState: SheetState,
    hideFilterSheet: () -> Unit,
    filter: AniPersonalMediaStatus,
    setFilter: (AniPersonalMediaStatus) -> Unit,
    isAnime: Boolean
) {
    ModalBottomSheet(
        sheetState = filterSheetState,
        onDismissRequest = hideFilterSheet,
    ) {
//        CenterAlignedTopAppBar(title = {
//            Text(
//                text = stringResource(R.string.filter),
//            )
//        }, navigationIcon = {
//            IconButton(
//                onClick = hideFilterSheet,
//                modifier = Modifier.padding(Dimens.PaddingNormal),
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Close,
//                    contentDescription = stringResource(R.string.close),
//                )
//            }
//        })
        AniPersonalMediaStatus.values().forEach { status ->
            ModalSheetTextButton(
                filterFunction = setFilter,
                hideFilterSheet = hideFilterSheet,
                filter = filter,
                thisFilter = status,
                name = status.toString(LocalContext.current, isAnime),
                icon = status.getIconResource()
            )
        }
//        ModalSheetTextButton(
//            filterFunction = setFilter,
//            hideFilterSheet = hideFilterSheet,
//            filter = filter,
//            thisFilter = PersonalMediaStatus.UNKNOWN,
//            name = stringResource(R.string.all),
//            icon = R.drawable.baseline_done_all_24
//        )
//        ModalSheetTextButton(
//            filterFunction = setFilter,
//            hideFilterSheet = hideFilterSheet,
//            filter = filter,
//            thisFilter = PersonalMediaStatus.CURRENT,
//            name = if (isAnime) stringResource(R.string.watching) else stringResource(R.string.reading),
//            icon = R.drawable.outline_play_circle_24
//        )
//        ModalSheetTextButton(
//            filterFunction = setFilter,
//            hideFilterSheet = hideFilterSheet,
//            filter = filter,
//            thisFilter = PersonalMediaStatus.REPEATING,
//            name = if (isAnime) stringResource(R.string.rewatching) else stringResource(R.string.rereading),
//            icon = R.drawable.outline_replay_24
//        )
//        ModalSheetTextButton(
//            filterFunction = setFilter,
//            hideFilterSheet = hideFilterSheet,
//            filter = filter,
//            thisFilter = PersonalMediaStatus.COMPLETED,
//            name = stringResource(id = R.string.completed),
////            icon = R.drawable.outline_done_24
//            icon = R.drawable.outline_check_circle_24
//        )
//        ModalSheetTextButton(
//            filterFunction = setFilter,
//            hideFilterSheet = hideFilterSheet,
//            filter = filter,
//            thisFilter = PersonalMediaStatus.PAUSED,
//            name = stringResource(id = R.string.paused),
//            icon = R.drawable.outline_pause_circle_24
//        )
//        ModalSheetTextButton(
//            filterFunction = setFilter,
//            hideFilterSheet = hideFilterSheet,
//            filter = filter,
//            thisFilter = PersonalMediaStatus.DROPPED,
//            name = stringResource(id = R.string.dropped),
////            icon = R.drawable.outline_arrow_drop_down_circle_24
//            icon = R.drawable.outline_cancel_24
//        )
//        ModalSheetTextButton(
//            filterFunction = setFilter,
//            hideFilterSheet = hideFilterSheet,
//            filter = filter,
//            thisFilter = PersonalMediaStatus.PLANNING,
//            name = stringResource(id = R.string.planning),
//            icon = R.drawable.outline_task_alt_24
//        )
    }
}

@Composable
fun ModalSheetTextButton(
    filterFunction: (AniPersonalMediaStatus) -> Unit,
    hideFilterSheet: () -> Unit,
    filter: AniPersonalMediaStatus,
    thisFilter: AniPersonalMediaStatus,
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
                contentDescription = null,
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

@Preview(showBackground = true, group = "Filter")
@Composable
fun FilterComponentPreview() {
    ModalSheetTextButton(
        filterFunction = {},
        hideFilterSheet = { },
        filter = AniPersonalMediaStatus.PLANNING,
        thisFilter = AniPersonalMediaStatus.REPEATING,
        name = "Repeating",
        icon = R.drawable.outline_replay_24
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, group = "Filter")
@Composable
fun FilterModalSheetPreview() {
    val state = rememberModalBottomSheetState()
    rememberCoroutineScope()
    LaunchedEffect(key1 = state.currentValue, block = { state.show() })
    FilterSheet(
        filterSheetState = state,
        hideFilterSheet = { },
        filter = AniPersonalMediaStatus.CURRENT,
        setFilter = {},
        isAnime = true
    )
}