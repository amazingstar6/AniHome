package com.example.anilist.ui.details.mediadetails.components

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.paging.compose.LazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.anilist.R
import com.example.anilist.data.models.AniStaff
import com.example.anilist.ui.Dimens

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
                        modifier = Modifier
                            .padding(Dimens.PaddingNormal)
                            .clickable {
                                onNavigateToStaff(staff.id)
                            },
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(staff.coverImage)
                                .crossfade(true).build(),
                            contentDescription = "",
                            placeholder = painterResource(id = R.drawable.no_image),
                            fallback = painterResource(id = R.drawable.no_image),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxHeight()
//                        .width(100.dp)
                                .padding(end = Dimens.PaddingNormal)
                                .clip(MaterialTheme.shapes.medium),

                            )
                        Column() {
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
        })
}

//@Preview(showBackground = true)
//@Composable
//fun StaffPreview() {
//    StaffScreen(
//        listOf(Staff(123, "吾峠呼世晴", "Original Creator"), Staff(1234, "外崎春雄", "Director")),
//        {},
//    )
//}
