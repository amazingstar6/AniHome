package com.example.anilist.ui.my_media

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.anilist.R
import com.example.anilist.data.models.Anime
import com.example.anilist.ui.home.AniHomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAnime(aniHomeViewModel: AniHomeViewModel, navigateToDetails: (Int) -> Unit, isAnime: Boolean, accessCode: String) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(text = if (isAnime) "Watching" else "Reading") })
    }) {

        aniHomeViewModel.loadMyAnime(accessCode)
        val trendingAnimeUiState by aniHomeViewModel.uiState.collectAsState()
        val mediaList = trendingAnimeUiState.personalAnimeList

        LazyColumn(modifier = Modifier.padding(top = it.calculateTopPadding())) {
            items(mediaList) { media ->
                MediaCard(navigateToDetails, media)
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MediaCard(
    navigateToDetails: (Int) -> Unit,
    media: Anime
) {
    Card(onClick = { navigateToDetails(media.id) }) {
        Row {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(media.coverImage)
                    .crossfade(true).build(),
                contentDescription = "Cover of ${media.title}",
                fallback = painterResource(id = R.drawable.no_image),
                modifier = Modifier.clip(RoundedCornerShape(20.dp))
            )
            Column {
                Row {
                    Text(text = media.title)
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "edit")
                }
                Text(text = media.format)
                Row {
                    Icon(
                        painterResource(id = R.drawable.anime_details_rating_star),
                        contentDescription = "star"
                    )
                    Text(text = media.personalRating.toString())
                }
                Row {
                    Text(text = "${media.personalEpisodeProgress}/${media.episodeAmount}")
                    Button(onClick = { /*TODO*/ }) {
                        Row {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "add")
                            Text(text = "1")
                        }
                    }
                }
            }
            LinearProgressIndicator(progress = (media.personalEpisodeProgress / media.episodeAmount.toFloat()) * 100)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyAnimePreview() {
    MediaCard(navigateToDetails = {}, media = Anime(title = "鬼滅の刃"))
}