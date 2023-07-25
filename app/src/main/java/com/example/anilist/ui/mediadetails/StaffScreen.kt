package com.example.anilist.ui.mediadetails

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.anilist.R
import com.example.anilist.data.models.Staff
import com.example.anilist.ui.Dimens
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun StaffScreen(
    staffList: List<Staff>,
    getMoreStaff: (Int) -> Unit,
    onNavigateToStaff: (Int) -> Unit
) {
    val state = rememberLazyGridState()
    var page by remember {
        mutableStateOf(1)
    }
    LazyVerticalGrid(state = state, columns = GridCells.Fixed(2), content = {
        items(
            staffList
        ) { staff ->
            Row(
                modifier = Modifier.padding(Dimens.PaddingNormal).clickable {
                    onNavigateToStaff(staff.id)
                }
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(staff.coverImage)
                        .crossfade(true).build(),
                    contentDescription = "",
                    placeholder = painterResource(id = R.drawable.no_image),
                    fallback = painterResource(id = R.drawable.no_image),
                    contentScale = ContentScale.FillHeight,
                    modifier = Modifier
                        .fillMaxHeight()
//                        .width(100.dp)
                        .padding(end = Dimens.PaddingNormal)
                        .clip(MaterialTheme.shapes.medium)

                )
                Column() {
                    Text(
                        text = staff.name,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = staff.role,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(text = staff.hasNextPage.toString())
                }
            }

            val needNextPage by remember {
                derivedStateOf {
                    val layoutInfo = state.layoutInfo
                    val totalItems = layoutInfo.totalItemsCount
                    val lastItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1
                    val buffer = 5
                    lastItemIndex > (totalItems - buffer)
                }
            }
            LaunchedEffect(needNextPage) {
                snapshotFlow {
                    needNextPage
                }.distinctUntilChanged().collect {
                    if (needNextPage) getMoreStaff(++page)
                }
            }
        }
    })
}

@Preview(showBackground = true)
@Composable
fun StaffPreview() {
    StaffScreen(
        listOf(Staff(123, "吾峠呼世晴", "Original Creator"), Staff(1234, "外崎春雄", "Director")),
        {}
    ) {
    }
}
