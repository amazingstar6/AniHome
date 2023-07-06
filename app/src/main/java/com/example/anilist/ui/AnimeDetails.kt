package com.example.anilist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.LocalContext
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
    val content: @Composable() (ColumnScope.() -> Unit) = {
        var tabIndex by remember { mutableStateOf(0) }
//    val tabTitles = AniListRoute::class.java.fields.map { field -> field.get(null) as String }
        val tabTitles = listOf("Overview", "Characters")
        TabRow(
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
            0 -> Overview(anime)
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
fun Overview(anime: GetAnimeInfoQuery.Media) {
    Text(anime?.title?.english ?: "Failed to load")
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(anime?.coverImage?.extraLarge)
            .crossfade(true).build(),
//                placeholder = PlaceholderPainter(MaterialTheme.colorScheme.surfaceTint),
        contentDescription = "Cover of ${anime?.title?.english}",
        modifier = Modifier
            // cards have a corner radius of 12dp: https://m3.material.io/components/cards/specs#:~:text=Shape-,12dp%20corner%20radius,-Left/right%20padding
            .clip(RoundedCornerShape(12.dp))
            .fillMaxWidth()
    )
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
    AnimeDetails(
        id = 150672,
        navigateToHome = { }
    )
}