package com.kevin.anihome.ui.details.mediadetails.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.kevin.anihome.data.models.AniStaff
import com.kevin.anihome.ui.Dimens
import com.kevin.anihome.utils.AsyncImageRoundedCorners
import com.kevin.anihome.utils.SMALL_MEDIA_HEIGHT
import com.kevin.anihome.utils.SMALL_MEDIA_WIDTH
import kotlinx.coroutines.flow.flowOf

@Composable
fun StaffScreen(
    staffList: LazyPagingItems<AniStaff>,
    onNavigateToStaff: (Int) -> Unit,
) {
    val state = rememberLazyGridState()
    LazyVerticalGrid(
        modifier = Modifier.fillMaxHeight(),
        state = state,
        columns = GridCells.Fixed(2),
        content = {
            items(
                staffList.itemCount,
            ) { index ->
                val staff = staffList[index]
                if (staff != null) {
                    Row(
                        modifier =
                            Modifier
                                .padding(Dimens.PaddingNormal)
                                .clickable {
                                    onNavigateToStaff(staff.id)
                                },
                    ) {
//                        AsyncImage(
//                            model = ImageRequest.Builder(LocalContext.current)
//                                .data(staff.coverImage)
//                                .crossfade(true).build(),
//                            contentDescription = "",
//                            placeholder = painterResource(id = R.drawable.no_image),
//                            fallback = painterResource(id = R.drawable.no_image),
//                            contentScale = ContentScale.Crop,
//                            modifier = Modifier
//                                .fillMaxHeight()
// //                        .width(100.dp)
//                                .padding(end = Dimens.PaddingNormal)
//                                .clip(MaterialTheme.shapes.medium),
//
//                            )
                        AsyncImageRoundedCorners(
                            coverImage = staff.coverImage,
                            contentDescription = "Cover image of ${staff.name}",
                            height = SMALL_MEDIA_HEIGHT.dp,
                            width = SMALL_MEDIA_WIDTH.dp,
                            padding = 0.dp,
                        )
                        Column(modifier = Modifier.padding(start = Dimens.PaddingSmall)) {
                            Text(
                                text = staff.name,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = staff.role,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
fun StaffPreview() {
    val data =
        flowOf(
            PagingData.from(
                listOf(
                    AniStaff(123, "吾峠呼世晴", "Original Creator"),
                    AniStaff(1234, "外崎春雄", "Director"),
                ),
            ),
        ).collectAsLazyPagingItems()
    StaffScreen(
        staffList = data,
        onNavigateToStaff = {},
    )
}
