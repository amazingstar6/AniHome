package com.example.anilist.ui.mediadetails

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.anilist.data.models.AniStudio
import com.example.anilist.data.models.Media

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioDetailScreen(id: Int, studioDetailViewModel: StudioDetailViewModel = hiltViewModel()) {
    val studio by studioDetailViewModel.studio.collectAsState()
    val mediaList = studioDetailViewModel.mediaOfStudio.collectAsLazyPagingItems()
    studioDetailViewModel.getStudioDetails(id)
    StudioDetail(studio, id, mediaList)
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun StudioDetail(
    studio: AniStudio,
    id: Int,
    mediaList: LazyPagingItems<Media>
) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(text = studio.name) })
    }) {
        Column(modifier = Modifier.padding(top = it.calculateTopPadding())) {
            Text(text = "Showing studio with id $id")
            LazyVerticalGrid(
                columns = GridCells.Adaptive(120.dp),
            ) {
                items(mediaList.itemCount) { index ->
                    val media = mediaList[index]
                    if (media != null) {
                        Text(text = media.title)
                        Text(text = "Hello")
                    }
                }
            }
        }
    }
}

//@Preview
//@Composable
//fun StudioDetailPreview() {
//    StudioDetail(studio = AniStudio(), id = 21, mediaList = /*listOf<Media>(Media(title = "鬼滅の刃"))*/)
//}