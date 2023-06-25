package com.example.anilist.ui

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.anilist.GetTrendsQuery
import com.example.anilist.R
import com.example.anilist.ui.theme.AnilistTheme
import java.net.HttpURLConnection
import java.net.URL

private const val TAG = "TrendingAnime"

fun main() {
    var query = """
        query (${'$'}id: Int) { # Define which variables will be used in the query (id)
          Media (id: ${'$'}id, type: ANIME) { # Insert our variables into the query arguments (id) (type: ANIME is hard-coded in the query)
            id
            title {
              romaji
              english
              native
            }
          }
        }
    """.trimIndent()
    var variables = mapOf("id" to 15125)
    var url2 = """
        https://graphql.anilist.co',
    options = {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
        },
        body: JSON.stringify({
            query: query,
            variables: variables
        })
    }
    """.trimIndent()

    val url: URL = URL("https://graphql.anilist.co")
    val con: HttpURLConnection = url.openConnection() as HttpURLConnection
    con.requestMethod = "POST"
    con.setRequestProperty("Content-Type", "application/json")
    con.setRequestProperty("Accept", "application/json")

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendingAnime(
    trendingAnimeViewModel: TrendingAnimeViewModel = viewModel(),
    onNavigateToDetails: (Int) -> Unit
) {
    val trendingAnimeUiState by trendingAnimeViewModel.uiState.collectAsState()
    Column {
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
            leadingIcon = {
                Icon(painterResource(id = R.drawable.baseline_menu_24), "Menu")
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
        HeadlineText("Popular this season")
        LazyHorizontalGrid(rows = GridCells.Adaptive(600.dp)) {
            items(trendingAnimeUiState.names) { anime ->
                AnimeCard(
                    anime,
                    {
                        onNavigateToDetails(anime.id)
                    })
            }
        }
        HeadlineText("Trending now")
        HeadlineText("Upcoming next season")
    }
}

@Composable
fun HeadlineText(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(10.dp)
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
    Column {
        Card(
            onClick = onNavigateToDetails,
            modifier = Modifier
                .padding(5.dp)
                .width(200.dp)
        ) {
            //todo change not null assertion
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(anime.coverImage!!.extraLarge)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(id = R.drawable.kimetsu_no_yaiba),
                contentDescription = "Cover of ${anime.title!!.english}",
                modifier = Modifier
                    .clip(RoundedCornerShape(10))
                    .fillMaxWidth()
            )
            Text(
                anime.title?.native ?: "Native title could not be loaded/does not exist",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(10.dp),
                overflow = TextOverflow.Ellipsis

            )
            Text(
                anime.title?.romaji ?: "Romaji title could not be loaded/does not exist",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(5.dp)
            )
            Text(
                anime.title?.english ?: "English title could not be loaded/does not exist",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(5.dp, bottom = 10.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyAppPreview() {
    TrendingAnime(onNavigateToDetails = {})
}