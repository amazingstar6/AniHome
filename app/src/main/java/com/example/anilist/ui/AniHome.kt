package com.example.anilist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.anilist.GetTrendsQuery
import com.example.anilist.R
import kotlinx.coroutines.flow.distinctUntilChanged

private const val TAG = "AniHome"

@Composable
fun AniHome(
    aniHomeViewModel: AniHomeViewModel = viewModel(),
    onNavigateToDetails: (Int) -> Unit
) {
    val trendingAnimeUiState by aniHomeViewModel.uiState.collectAsState()
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
//        AniSearchBar()
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
        query = "Search Anime",
        onQueryChange = { text = it },
        onSearch = {},
        active = active,
        onActiveChange = { active = it },
        placeholder = {
            Text(text = "Search for Anime...")
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
        modifier = Modifier.padding(10.dp)
    ) {
        Text(text = "Show top/trending anime/search history")
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AnimeRow(
    onNavigateToDetails: (Int) -> Unit,
    animeList: List<GetTrendsQuery.Medium>,
    loadMoreAnime: () -> Unit,
    reloadAnime: () -> Unit
) {
    if (animeList.isNotEmpty()) {
        val state = rememberLazyListState()
        LazyRow(
            state = state, modifier = Modifier.height(400.dp)
        ) {
            items(animeList) { anime ->
                AnimeCard(anime, onNavigateToDetails = ({
                    onNavigateToDetails(anime.id)
                }))
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
        text, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(10.dp)
    )
}

@ExperimentalMaterial3Api
@Composable
@NonRestartableComposable
fun AnimeCard(
    anime: GetTrendsQuery.Medium,
    onNavigateToDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = Modifier.then(modifier)) {
        Card(
            onClick = { onNavigateToDetails() },
            modifier = Modifier
                .padding(5.dp)
                .width(200.dp)
                .fillMaxHeight()
        ) {
            //todo change not null assertion
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(anime.coverImage!!.extraLarge).crossfade(true).build(),
//                placeholder = PlaceholderPainter(MaterialTheme.colorScheme.surfaceTint),
                contentDescription = "Cover of ${anime.title!!.english}",
                modifier = Modifier
                    // cards have a corner radius of 12dp: https://m3.material.io/components/cards/specs#:~:text=Shape-,12dp%20corner%20radius,-Left/right%20padding
                    .clip(RoundedCornerShape(12.dp))
                    .fillMaxWidth()
            )
            Text(
                anime.title.native ?: "Native title could not be loaded/does not exist",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(10.dp),
                overflow = TextOverflow.Ellipsis

            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyAppPreview() {
    AniHome(onNavigateToDetails = {})
}