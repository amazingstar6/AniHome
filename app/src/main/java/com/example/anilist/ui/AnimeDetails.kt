package com.example.anilist.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.anilist.GetAnimeInfoQuery
import com.example.anilist.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeDetails(
    id: Int,
    aniHomeViewModel: AniHomeViewModel = viewModel(),
    navigateToHome: () -> Unit
) {
    aniHomeViewModel.getAnimeDetails(id)

    val trendingAnimeUiState by aniHomeViewModel.uiState.collectAsState()

    val anime = trendingAnimeUiState.currentDetailAnime
    Scaffold(topBar = {
        TopAppBar(title = {
            Text(
                text = anime?.title?.english ?: stringResource(R.string.failed_to_load)
            )
        }, navigationIcon = {
            IconButton(onClick = navigateToHome) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = Icons.Default.ArrowBack.toString()
                )
            }
        }, actions = { Icon(Icons.Default.MoreVert, "More") })
    }) {
        if (trendingAnimeUiState.currentDetailAnime != null) {
            Content(it, anime!!, id)
        } else {
            FailedToLoad { aniHomeViewModel.getAnimeDetails(id) }
        }
    }
}

@Composable
private fun Content(
    it: PaddingValues,
    anime: GetAnimeInfoQuery.Media,
    id: Int
) {
    val content: @Composable (ColumnScope.() -> Unit) = {
        var tabIndex by remember { mutableStateOf(0) }
        val tabTitles = listOf("Overview", "Characters", "Staff", "Reviews", "Stats")
        ScrollableTabRow(
            selectedTabIndex = tabIndex,
            modifier = Modifier.padding(top = it.calculateTopPadding())
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = tabIndex == index,
                    onClick = { tabIndex = index },
                    text = { Text(text = title) })
            }
        }
        when (tabIndex) {
            //fixme
//            0 -> Overview()
            1 -> Charachters()
        }

    }
    Column(content = content)
}

@Composable
fun Charachters() {
    Text("Charachters")
}

@Composable
fun Overview(
    title: String,
    coverImage: String,
    format: String,
    seasonYear: String,
    episodeAmount: Int,
    averageScore: Int,
    tags: List<String>
) {
    Column {
        Row(
            modifier = Modifier
                .padding(20.dp)
        ) {
//        AsyncImage(
//            model = ImageRequest.Builder(LocalContext.current)
//                .data(coverImage)
//                .crossfade(true).build(),
////                placeholder = PlaceholderPainter(MaterialTheme.colorScheme.surfaceTint),
//            placeholder = painterResource(id = R.drawable.kimetsu_no_yaiba),
//            contentDescription = "Cover of $title",
//            modifier = Modifier
//                .clip(RoundedCornerShape(12.dp))
//                .padding(20.dp)
//        )
            Image(
                painter = painterResource(id = R.drawable.kimetsu_no_yaiba),
                contentDescription = title,
                modifier = Modifier.clip(RoundedCornerShape(20.dp))
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp)
            ) {
                Text(
                    text = title,
                    modifier = Modifier.padding(bottom = 20.dp),
                    style = MaterialTheme.typography.titleLarge
                )
                IconWithText(R.drawable.anime_details_movie, format)
                IconWithText(R.drawable.anime_details_calendar, seasonYear)
                IconWithText(
                    R.drawable.anime_details_timer,
                    if (episodeAmount == 1) "$episodeAmount Episode" else "$episodeAmount Episodes"
                )
                IconWithText(R.drawable.anime_details_heart, "$averageScore% Average score")
            }
        }
        Row(modifier = Modifier.padding(horizontal = 20.dp)) {
            for (tag in tags) {
                val color = MaterialTheme.colorScheme.secondaryContainer
                Text(
                    text = tag,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(4.dp)
                        .drawBehind {
                            drawRoundRect(color = color, cornerRadius = CornerRadius(12f, 12f))
                        }.padding(2.dp)

                )
            }
        }
    }
}

@Composable
fun IconWithText(icon: Int, text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = Modifier
            .padding(vertical = 5.dp)
            .then(modifier)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = text,
            tint = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text,
            modifier = Modifier.padding(horizontal = 6.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun FailedToLoad(reload: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(text = "Failed to load anime")
        Button(onClick = { reload() }) {
            Text(text = "Reload")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AnimeDetailsPreview() {
    Overview(
        title = "鬼滅の刃 刀鍛冶の里編",
        coverImage = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx145139-rRimpHGWLhym.png",
        format = "TV",
        seasonYear = "Spring 2023",
        episodeAmount = 11,
        averageScore = 83,
        tags = listOf("Action", "Adventure", "Drama", "Fantasy", "Supernatural")
    )
}