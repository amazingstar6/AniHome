package com.kevin.anihome.ui.mymedia.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kevin.anihome.R
import com.kevin.anihome.data.models.AniMediaListSort
import com.kevin.anihome.ui.Dimens

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SortingBottomSheet(
    setShowSortingSheet: (Boolean) -> Unit,
    isDescending: Boolean,
    setIsDescending: (Boolean) -> Unit,
    setSort: (AniMediaListSort, Boolean) -> Unit,
    sort: AniMediaListSort,
) {
    ModalBottomSheet(onDismissRequest = { setShowSortingSheet(false) }) {
        Row(
            modifier =
                Modifier
                    .padding(horizontal = Dimens.PaddingNormal)
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.weight(1f)) {
                SegmentedButton(
                    selected = !isDescending,
                    onClick = {
                        setIsDescending(false)
                        setSort(sort.removeDescending(), false)
                    },
                    shape =
                        SegmentedButtonDefaults.itemShape(
                            index = 0,
                            count = 2,
                        ),
                ) {
                    Text(text = "Ascending")
                }
                SegmentedButton(
                    selected = isDescending,
                    onClick = {
                        setIsDescending(true)
                        setSort(sort.removeDescending(), true)
                    },
                    shape =
                        SegmentedButtonDefaults.itemShape(
                            index = 1,
                            count = 2,
                        ),
                ) {
                    Text(text = "Descending")
                }
            }
            TooltipBox(
                tooltip = {
                    PlainTooltip {
                        Text(text = stringResource(R.string.reset_sort))
                    }
                },
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                state = rememberTooltipState(),
            ) {
                IconButton(
                    onClick = {
                        setSort(AniMediaListSort.UPDATED_TIME, true)
                        setIsDescending(true)
                    },
                    modifier =
                        Modifier
                            .padding(start = Dimens.PaddingSmall),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_restart_alt_24),
                        contentDescription = stringResource(R.string.reset_sort),
                    )
                }
            }
        }
        val sortTypesToShow by remember {
            mutableStateOf(
                AniMediaListSort.entries.filter { !it.name.contains("DESC") },
            )
        }
        sortTypesToShow.forEach {
            TextButton(onClick = {
                setSort(it, isDescending)
            }, modifier = Modifier.padding(horizontal = Dimens.PaddingNormal)) {
                Text(
                    text = it.toString(LocalContext.current),
                    color = if (it == sort || it.name == sort.name.substringBefore("_DESC")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SortingBottomSheetPreview() {
    SortingBottomSheet(
        setShowSortingSheet = {},
        isDescending = true,
        setIsDescending = {},
        setSort = { _, _ -> },
        sort = AniMediaListSort.UPDATED_TIME_DESC,
    )
}
