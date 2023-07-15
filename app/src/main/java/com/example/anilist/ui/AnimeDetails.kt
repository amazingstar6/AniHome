package com.example.anilist.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.anilist.GetAnimeInfoQuery
import com.example.anilist.R
import com.example.anilist.TextViewCustom
import com.example.anilist.ui.theme.AnilistTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeDetails(
    id: Int, aniHomeViewModel: AniHomeViewModel = viewModel(), navigateToHome: () -> Unit
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
    it: PaddingValues, anime: GetAnimeInfoQuery.Media, id: Int
) {
    val content: @Composable (ColumnScope.() -> Unit) = {
        var tabIndex by remember { mutableStateOf(0) }
        val tabTitles = listOf("Overview", "Characters", "Staff", "Reviews", "Stats")
        ScrollableTabRow(
            selectedTabIndex = tabIndex, modifier = Modifier.padding(top = it.calculateTopPadding())
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(selected = tabIndex == index,
                    onClick = { tabIndex = index },
                    text = { Text(text = title) })
            }
        }
        when (tabIndex) {
            //fixme
            0 -> Overview(
                title = anime.title?.native ?: "Unknown",
                coverImage = anime.coverImage?.extraLarge ?: "",
                format = anime.format?.name ?: "Unknown",
                seasonYear = anime.seasonYear.toString(),
                episodeAmount = anime.episodes ?: 0,
                averageScore = anime.averageScore ?: 0,
                tags = emptyList(),
                description = anime.description ?: "No description found",
                infoList = mapOf(
                    "format" to anime.format?.name.orEmpty(),
                    "status" to anime.status?.name?.lowercase()
                        ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                        .orEmpty(),
                    "startDate" to if (anime.startDate != null) "${anime.startDate.day}-${anime.startDate.month}-${anime.startDate.year}" else "Unknown",
                    "endDate" to if (anime.endDate?.year != null && anime.endDate.month != null && anime.endDate.day != null) "${anime.endDate.day}-${anime.endDate.month}-${anime.endDate.year}" else "Unknown",
                    "duration" to anime.duration.toString(),
                    "country" to anime.countryOfOrigin.toString(),
                    "source" to (anime.source?.rawValue ?: "Unknown"),
                    "hashtag" to (anime.hashtag ?: "Unknown"),
                    "licensed" to anime.isLicensed.toString(),
                    "updatedAt" to anime.updatedAt.toString(),
                    "synonyms" to (anime.synonyms.toString()),
                    "nsfw" to anime.isAdult.toString()),
                genres = emptyList(),
                trailerImage = anime.trailer?.thumbnail ?: "",
                // todo add dailymotion
                trailerLink = if (anime.trailer?.site == "youtube") "https://www.youtube.com/watch?v=${anime.trailer.id}" else if (anime.trailer?.site == "dailymotion") "" else ""
            )

            1 -> Charachters()
        }

    }
    Column(content = content)
}

@Composable
fun Charachters() {
    Text("Charachters")
}

data class InfoDetail(val format: String = "", val status: String = "")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Overview(
    title: String,
    coverImage: String,
    format: String,
    seasonYear: String,
    episodeAmount: Int,
    averageScore: Int,
    genres: List<String>,
    highestRated: String = "",
    mostPopular: String = "",
    description: String,
    infoList: Map<String, String>,
    tags: List<String>,
    trailerImage: String,
    trailerLink: String
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(
        ) {
            if (coverImage != "") {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(coverImage)
                        .crossfade(true).build(),
//                placeholder = PlaceholderPainter(MaterialTheme.colorScheme.surfaceTint),
//                placeholder = painterResource(id = R.drawable.kimetsu_no_yaiba),
                    contentDescription = "Cover of $title",
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .padding(20.dp)
                )
            } else {
                RoundedImage(title)
            }
            Column {
                Text(
                    text = title,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Column(modifier = Modifier.padding(start = 24.dp)) {
                    IconWithText(R.drawable.anime_details_movie, format)
                    IconWithText(R.drawable.anime_details_calendar, seasonYear)
                    IconWithText(
                        R.drawable.anime_details_timer,
                        if (episodeAmount == 1) "$episodeAmount Episode" else "$episodeAmount Episodes"
                    )
                    IconWithText(R.drawable.anime_details_heart, "$averageScore% Average score")
                }
            }
        }
        FlowRow(
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth()
        ) {
            for (genre in genres) {
                val colorContainer = MaterialTheme.colorScheme.secondaryContainer
                BackgroundTextButton(genre, colorContainer)
            }
        }
        if (highestRated != "") {
            IconWithText(
                icon = R.drawable.anime_details_rating_star,
                text = highestRated,
                iconTint = MaterialTheme.colorScheme.secondary
            )
        }
        if (mostPopular != "") {
            IconWithText(
                icon = R.drawable.anime_details_heart,
                text = mostPopular,
                iconTint = MaterialTheme.colorScheme.secondary
            )
        }
        HeadLine("Description")
        val color = MaterialTheme.colorScheme.onSurface.toArgb()
        AndroidView(factory = { context ->
            TextViewCustom(context, description, color)
        })
//        Text(
//            text = description,
//            style = MaterialTheme.typography.bodySmall,
//            color = MaterialTheme.colorScheme.onSurface
//        )
        HeadLine("Relations")
        LazyRow {
            items(10) {
                Column(modifier = Modifier.padding(end = 22.dp)) {
                    RoundedImage(title)
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(4.dp)
                    )
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 4.dp)
                    )
                }
            }
        }
        HeadLine("Info")
        Row {
            Column(modifier = Modifier.padding(end = 8.dp)) {
                if (infoList.containsKey("format")) {
                    InfoName("Format")
                }
                if (infoList.containsKey("status")) {
                    InfoName("Status")
                }
                if (infoList.containsKey("startDate")) {
                    InfoName("Start date")
                }
                if (infoList.containsKey("endDate")) {
                    InfoName(text = "End date")
                }
                InfoName("Episodes")
                if (infoList.containsKey("duration")) {
                    InfoName("Duration")
                }
                if (infoList.containsKey("country")) {
                    InfoName("Country")
                }
                if (infoList.containsKey("source")) {
                    InfoName("Source")
                }
                if (infoList.containsKey("hashtag")) {
                    InfoName("Hashtag")
                }
                if (infoList.containsKey("licensed")) {
                    InfoName("Licensed")
                }
                if (infoList.containsKey("updatedAt")) {
                    InfoName("Updated at")
                }
                if (infoList.containsKey("synonyms")) {
                    InfoName("Synonyms")
                }
                if (infoList.containsKey("nsfw")) {
                    InfoName("NSFW")
                }
            }
            Column {
                if (infoList.containsKey("format")) {
                    InfoData(infoList["format"]!!)
                }
                if (infoList.containsKey("status")) {
                    InfoData(infoList["status"]!!)
                }
                if (infoList.containsKey("startDate")) {
                    InfoData(infoList["startDate"]!!)
                }
                if (infoList.containsKey("endDate")) {
                    InfoData(infoList["endDate"]!!)
                }
                InfoData(episodeAmount.toString())
                if (infoList.containsKey("duration")) {
                    InfoData(infoList["duration"]!!)
                }
                if (infoList.containsKey("country")) {
                    InfoData(infoList["country"]!!)
                }
                if (infoList.containsKey("source")) {
                    InfoData(infoList["source"]!!)
                }
                if (infoList.containsKey("hashtag")) {
                    InfoData(infoList["hashtag"]!!)
                }
                if (infoList.containsKey("licensed")) {
                    InfoData(infoList["licensed"]!!)
                }
                if (infoList.containsKey("updatedAt")) {
                    InfoData(infoList["updatedAt"]!!)
                }
                if (infoList.containsKey("synonyms")) {
                    InfoData(infoList["synonyms"]!!)
                }
                if (infoList.containsKey("nsfw")) {
                    InfoData(infoList["nsfw"]!!)
                }
            }
        }
        HeadLine("Tags")
        FlowRow(horizontalArrangement = Arrangement.Start) {
            for (tag in tags) {
                ElevatedButton(
                    onClick = { },
                    modifier = Modifier.padding(end = 12.dp, bottom = 4.dp)
                ) {
                    Text(text = tag)
                }
            }
        }
        HeadLine("Trailer")
        val uriHandler = LocalUriHandler.current
        if (trailerImage != "") {
            Image(
                painter = painterResource(id = R.drawable.kimetsu_no_yaiba_trailer),
                contentDescription = "Trailer",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { uriHandler.openUri(trailerLink) }
            )
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(trailerImage)
                    .crossfade(true).build(),
//                placeholder = PlaceholderPainter(MaterialTheme.colorScheme.surfaceTint),
//                placeholder = painterResource(id = R.drawable.kimetsu_no_yaiba),
                contentDescription = "Trailer of $title",
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .padding(20.dp)
            )
        }
        HeadLine("External links")
    }
}

@Composable
private fun BackgroundTextButton(tag: String, colorContainer: Color) {
    Text(text = tag,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .padding(vertical = 6.dp, horizontal = 4.dp)
            .drawBehind {
                drawRoundRect(
                    color = colorContainer, cornerRadius = CornerRadius(40f, 40f)
                )
            }
            .padding(6.dp))
}

@Composable
private fun InfoName(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun InfoData(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun RoundedImage(title: String, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.kimetsu_no_yaiba),
        contentDescription = title,
        modifier = Modifier
            .then(modifier)
            .clip(RoundedCornerShape(20.dp))
    )
}

@Composable
private fun HeadLine(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
    )
}

@Composable
fun IconWithText(
    icon: Int,
    text: String,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = 4.dp)
            .then(modifier)
    ) {
        Icon(
            painter = painterResource(id = icon), contentDescription = text, tint = iconTint
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

@Preview(name = "Light mode", showBackground = true, heightDp = 2000)
@Preview(name = "Night mode", showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun OverviewPreview() {
    AnilistTheme() {
        Surface() {
            Overview(
                title = "鬼滅の刃 刀鍛冶の里編",
                coverImage = "",
                format = "TV",
                seasonYear = "Spring 2023",
                episodeAmount = 11,
                averageScore = 83,
                genres = listOf("Action", "Adventure", "Drama", "Fantasy", "Supernatural"),
                highestRated = "#99 Highest rated all time",
                mostPopular = "#183 Most popular all time",
                description = "Adaptation of the Swordsmith Village Arc.<br>\n<br>\nTanjiro\u2019s journey leads him to the Swordsmith Village, where he reunites with two Hashira, members of the Demon Slayer Corps\u2019 highest-ranking swordsmen - Mist Hashira Muichiro Tokito and Love Hashira Mitsuri Kanroji. With the shadows of demons lurking near, a new battle begins for Tanjiro and his comrades.\n<br><br>\n<i>Notes:<br>\n\u2022 The first episode has a runtime of ~49 minutes, and received an early premiere in cinemas worldwide as part of a special screening alongside the final two episodes of Kimetsu no Yaiba: Yuukaku-hen.<br>\n\u2022 The final episode has a runtime of ~52 minutes. </i>",
                infoList = mapOf(
                    "format" to "TV",
                    "status" to "Finished",
                    "startDate" to "04-09-2023",
                    "endDate" to "06-18-2023",
                    "duration" to "24",
                    "country" to "Japan",
                    "source" to "Manga",
                    "hashtag" to "#鬼滅の刃",
                    "licensed" to "Yes",
                    "updatedAt" to "04-06-2023",
                    "synonyms" to "KnY 3ดาบพิฆาตอสูร ภาค 3 บทหมู่บ้านช่างตีดาบ\n" +
                            "Demon Slayer: Kimetsu no Yaiba - Le village des forgerons\n" +
                            "Истребитель демонов: Kimetsu no Yaiba. Деревня кузнецов",
                    "nsfw" to "No"
                ),
                tags = listOf(
                    "Demons",
                    "Shounen",
                    "Swordplay",
                    "Male Protagonist",
                    "Super Power",
                    "Gore",
                    "Monster Girl",
                    "Body Horror",
                    "Historical",
                    "CGI",
                    "Femaile Protagonist",
                    "Orphan",
                    "Rural"
                ),
                trailerImage = "",
                trailerLink = "https://www.youtube.com/watch?v=a9tq0aS5Zu8"
            )
        }
    }
}