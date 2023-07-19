package com.example.anilist.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.anilist.R
import com.example.anilist.data.models.Anime
import kotlinx.coroutines.flow.distinctUntilChanged

private const val TAG = "AniHome"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AniHome(
    aniHomeViewModel: AniHomeViewModel,
    onNavigateToDetails: (Int) -> Unit
) {
    val trendingAnimeUiState by aniHomeViewModel.uiState.collectAsState()
    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = {
            Text("Home")
        },
            navigationIcon = {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    modifier = Modifier.padding(12.dp)
                )
            },
            actions = {
                Icon(
                    imageVector = Icons.Default.AccountBox,
                    contentDescription = "Profile",
                    modifier = Modifier.padding(12.dp)
                )
            })
    }) {
        Column(
            modifier = Modifier
                .padding(top = it.calculateTopPadding())
                .verticalScroll(rememberScrollState())
        ) {
            AniSearchBar()
            HeadlineText("Popular this season")
            AnimeRow(
                onNavigateToDetails,
                trendingAnimeUiState.popularAnime,
                { aniHomeViewModel.loadPopularAnime(true) },
                aniHomeViewModel::loadPopularAnime
            )
            HeadlineText("Trending now")
            AnimeRow(
                onNavigateToDetails,
                trendingAnimeUiState.trendingAnime,
                { aniHomeViewModel.loadTrendingAnime(true) },
                aniHomeViewModel::loadTrendingAnime
            )
            HeadlineText("Upcoming next season")
            AnimeRow(
                onNavigateToDetails,
                trendingAnimeUiState.upcomingNextSeason,
                { aniHomeViewModel.loadUpcomingNextSeason(true) },
                aniHomeViewModel::loadUpcomingNextSeason
            )
            HeadlineText("All time popular")
            AnimeRow(
                onNavigateToDetails,
                trendingAnimeUiState.allTimePopular,
                { aniHomeViewModel.loadAllTimePopular(true) },
                aniHomeViewModel::loadAllTimePopular
            )
            HeadlineText("Top 100 anime")
            AnimeRow(
                onNavigateToDetails,
                trendingAnimeUiState.top100Anime,
                { aniHomeViewModel.loadTop100Anime(true) },
                aniHomeViewModel::loadTop100Anime
            )
        }
    }

}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AniSearchBar() {
    var text by remember {
        mutableStateOf("")
    }
    var active by remember {
        mutableStateOf(false)
    }
    SearchBar(
        query = text,
        onQueryChange = { },
        onSearch = { },
        active = active,
        onActiveChange = { },
        placeholder = {
            Text(text = "Search for Anime, Manga...")
        },
        leadingIcon = {
            Icon(
                painterResource(id = R.drawable.baseline_menu_24), "Menu"
            )
        },
        trailingIcon = {
            Row {
                Icon(
                    painterResource(id = R.drawable.baseline_search_24),
                    "Search",
                    modifier = Modifier.padding(end = 16.dp)
                )
                Icon(
                    painterResource(id = R.drawable.baseline_more_vert_24),
                    "More options",
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Text(text = "Show top/trending anime/search history")
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AnimeRow(
    onNavigateToDetails: (Int) -> Unit,
    animeList: List<Anime>,
    loadMoreAnime: () -> Unit,
    reloadAnime: () -> Unit
) {
    if (animeList.isNotEmpty()) {
        val state = rememberLazyListState()
        LazyRow(
            state = state,
        ) {
            items(animeList) { anime ->
                AnimeCard(
                    title = anime.title,
                    coverImage = anime.coverImage,
                    onNavigateToDetails = ({
                        onNavigateToDetails(anime.id)
                    })
                )
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
                if (needNextPage) loadMoreAnime()
            }
        }
    } else {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(400.dp)
                .fillMaxWidth()
        ) {
            Button(
                onClick = reloadAnime,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(text = "Reload Anime")
            }
        }
    }
}

@Composable
fun HeadlineText(text: String) {
    Text(
        text, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(12.dp)
    )
}

@ExperimentalMaterial3Api
@Composable
@NonRestartableComposable
fun AnimeCard(
    title: String,
    coverImage: String,
    onNavigateToDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = Modifier
        .padding(start = 12.dp)
        .width(120.dp)
        .height(240.dp)
        .then(modifier)
        .clickable { onNavigateToDetails() }) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(coverImage).crossfade(true).build(),
            contentDescription = "Cover of $title",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
//                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(12.dp))

        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(10.dp),
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MyAppPreview() {
//    AniHome(onNavigateToDetails = {}, aniHomeViewModel = null)
}