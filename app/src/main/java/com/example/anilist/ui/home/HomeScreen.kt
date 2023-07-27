package com.example.anilist.ui.home

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.anilist.R
import com.example.anilist.data.models.Media
import com.example.anilist.data.repository.HomeMedia
import com.example.anilist.ui.Dimens
import kotlinx.coroutines.flow.distinctUntilChanged

private const val TAG = "AniHome"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    aniHomeViewModel: AniHomeViewModel,
    onNavigateToDetails: (Int) -> Unit,
    onNavigateToNotification: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val media by aniHomeViewModel.media.observeAsState(HomeMedia())
    val popularAnime by aniHomeViewModel.popularAnime.observeAsState(emptyList())
    val trendingAnime by aniHomeViewModel.trendingAnime.observeAsState(emptyList())
    val upcomingNextSeason by aniHomeViewModel.upcomingNextSeason.observeAsState(emptyList())

    var popularPage by remember { mutableIntStateOf(1) }
    var trendingPage by remember { mutableIntStateOf(1) }

    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = {
            Text("Home")
        }, navigationIcon = {
            Box {
                PlainTooltipBox(
                    tooltip = { Text(text = "Settings   ") },
                    modifier = Modifier.align(Alignment.BottomCenter),
                ) {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.tooltipTrigger(),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "settings",
                        )
                    }
                }
            }
        }, actions = {
            IconButton(onClick = onNavigateToNotification) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "notifications",
//                    modifier = Modifier.padding(Dimens.PaddingNormal)
                )
            }
        })
    }) {
        Box {
            Column(
                modifier = Modifier
                    .padding(top = it.calculateTopPadding())
                    .verticalScroll(rememberScrollState()),
            ) {
                HeadlineText("Trending now")
                AnimeRow(
                    onNavigateToDetails,
                    trendingAnime,
                ) {
                    trendingPage += 1
                    aniHomeViewModel.fetchMedia(
                        isAnime = true,
                        page = trendingPage,
                        skipTrendingNow = false,
                    )
                }
                HeadlineText("Popular this season")
                AnimeRow(
                    onNavigateToDetails,
                    popularAnime,
                ) {
                    popularPage += 1
                    aniHomeViewModel.fetchMedia(
                        isAnime = true,
                        page = popularPage,
                        skipPopularThisSeason = false,
                    )
                }
                HeadlineText("Upcoming next season")
                AnimeRow(
                    onNavigateToDetails,
                    upcomingNextSeason,
                ) {
                    aniHomeViewModel.fetchMedia(
                        isAnime = true,
                        page = 1,
                        skipUpcomingNextSeason = false,
                    )
                }
                //            HeadlineText("All time popular")
                //            AnimeRow(
                //                onNavigateToDetails,
                //                media.allTimePopular,
                //                { aniHomeViewModel.loadAllTimePopular(true) }
                //            )
                //            HeadlineText("Top 100 anime")
                //            AnimeRow(
                //                onNavigateToDetails,
                //                media.top100Anime,
                //                { aniHomeViewModel.loadTop100Anime(true) }
                //            )
            }

            var text by remember {
                mutableStateOf("")
            }
            var active by remember {
                mutableStateOf(false)
            }
            val padding by animateDpAsState(targetValue = if (!active) Dimens.PaddingNormal else 0.dp)
            SearchBar(
                query = text,
                onQueryChange = { text = it },
                onSearch = { aniHomeViewModel.search(text) },
                active = active,
                onActiveChange = { active = it },
                placeholder = {
                    Text(text = "Search for Anime, Manga...")
                },
                trailingIcon = {
                    if (text == "") {
                        Icon(
                            painterResource(id = R.drawable.baseline_search_24),
                            "Search",
                            modifier = Modifier.padding(end = 16.dp),
                        )
                    } else {
                        IconButton(onClick = { text = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(R.string.clear)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .animateContentSize()
                    .fillMaxWidth()
                    .padding(horizontal = padding)
                    .padding(top = it.calculateTopPadding()),
            ) {
                Text(text = "Show top/trending anime/search history")
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AnimeRow(
    onNavigateToDetails: (Int) -> Unit,
    animeList: List<Media>,
    loadMoreAnime: () -> Unit,
) {
    val state = rememberLazyListState()
    LazyRow(
        state = state,
    ) {
        items(animeList) { anime ->
            AnimeCard(
                title = anime.title,
                coverImage = anime.coverImage,
                onNavigateToDetails = (
                        {
                            onNavigateToDetails(anime.id)
                        }
                        ),
            )
        }
    }

    val needNextPage by remember {
        derivedStateOf {
            val layoutInfo = state.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1
            val buffer = 3
            lastItemIndex > (totalItems - buffer)
        }
    }
    LaunchedEffect(needNextPage) {
        Log.i(TAG, "Next page is needed")
        snapshotFlow {
            needNextPage
        }.distinctUntilChanged().collect {
            if (needNextPage) loadMoreAnime()
        }
    }
}

@Composable
fun HeadlineText(text: String, onNavigateToOverview: () -> Unit = {}) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(12.dp),
        )
        IconButton(onClick = onNavigateToOverview) {
            Icon(imageVector = Icons.Outlined.ArrowForward, contentDescription = "anime overview")
        }
    }
}

@ExperimentalMaterial3Api
@Composable
@NonRestartableComposable
fun AnimeCard(
    title: String,
    coverImage: String,
    onNavigateToDetails: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = Modifier
            .padding(start = 12.dp)
            .width(120.dp)
            .height(240.dp)
            .then(modifier)
            .clickable { onNavigateToDetails() },
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(coverImage).crossfade(true)
                .build(),
            contentDescription = "Cover of $title",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
//                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(12.dp)),

            )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(10.dp),
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MyAppPreview() {
//    AniHome(onNavigateToDetails = {}, aniHomeViewModel = AniHomeViewModel(), onNavigateToNotification = {})
}
