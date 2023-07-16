package com.example.anilist.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.util.Log
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.anilist.GetAnimeInfoQuery
import com.example.anilist.R
import com.example.anilist.TextViewCustom
import com.example.anilist.data.Anime
import com.example.anilist.data.Link
import com.example.anilist.data.Relation
import com.example.anilist.data.Tag
import com.example.anilist.ui.theme.AnilistTheme
import kotlinx.coroutines.launch
import java.util.Locale


private const val TAG = "AnimeDetails"


enum class detailTabs {
    Overview,
    Charachters,
    Staff,
    Reviews,
    Stats
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AnimeDetails(
    id: Int,
    aniHomeViewModel: AniHomeViewModel = viewModel(),
    navigateToHome: () -> Unit,
    onNavigateToDetails: (Int) -> Unit
) {
    aniHomeViewModel.getAnimeDetails(id)

    val trendingAnimeUiState by aniHomeViewModel.uiState.collectAsState()

    val anime = trendingAnimeUiState.currentDetailAnime

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { detailTabs.values().size })

    val coroutineScope = rememberCoroutineScope()

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
            Column {
                AniDetailTabs(modifier = Modifier.padding(top = it.calculateTopPadding()),
                    titles = listOf(
                        "Overview", "Characters", "Staff", "Reviews", "Stats"
                    ),
                    tabSelected = pagerState.currentPage,
                    onTabSelected = { coroutineScope.launch { pagerState.scrollToPage(it) } })

                HorizontalPager(
                    state = pagerState
                ) { page ->
                    when (page) {
                        0 -> Content(
                            it,
                            anime!!,
                            id,
                            onNavigateToDetails,
                            tabSelected = pagerState.currentPage,
                            onTabSelected = {
//                coroutineScope.launch {
//                    pagerState.animateScrollToPage(
//                        it,
//                    )
//                }
                                coroutineScope.launch {
                                    Log.i(TAG, "Scrolling to page with index $it")
                                    pagerState.animateScrollToPage(it)
                                }
                            })
                        1 -> Characters()
                        2 -> Staff()
                        3 -> Reviews()
                        4 -> Stats()
                    }
                }
            }


        } else {
            FailedToLoad { aniHomeViewModel.getAnimeDetails(id) }
        }
    }
}

@Composable
fun Stats() {
    Text(text = "Stats", modifier = Modifier.fillMaxSize())
}

@Composable
fun Reviews() {
    Text(text = "Reviews", modifier = Modifier.fillMaxSize())
}

@Composable
fun Staff() {
    Text(text = "Staff", modifier = Modifier.fillMaxSize())
}

@Composable
private fun Content(
    it: PaddingValues,
    anime: GetAnimeInfoQuery.Media,
    id: Int,
    onNavigateToDetails: (Int) -> Unit,
    onTabSelected: (Int) -> Unit,
    tabSelected: Int
) {
    val tags: MutableList<Tag> = mutableListOf()
    for (tag in anime.tags.orEmpty()) {
        if (tag != null) {
            tags.add(Tag(tag.name, tag.rank ?: 0, tag.isMediaSpoiler ?: true))
        }
    }
    val synonyms = buildString {
        for (synonym in anime.synonyms.orEmpty()) {
            append(synonym)
            if (anime.synonyms?.last() != synonym) {
                append("\n")
            }
        }
    }
    val genres: MutableList<String> = mutableListOf()
    for (genre in anime.genres.orEmpty()) {
        if (genre != null) {
            genres.add(genre)
        }
    }
    val externalLinks: MutableList<Link> = mutableListOf()
    for (link in anime.externalLinks.orEmpty()) {
        if (link != null) {
            externalLinks.add(
                Link(
                    link.url ?: "",
                    link.site,
                    link.language ?: "",
                    link.color ?: "",
                    link.icon ?: ""
                )
            )
        }
    }
    val relations: MutableList<Relation> = mutableListOf()
    for (relation in anime.relations?.edges.orEmpty()) {
        relations.add(
            Relation(
                id = relation?.node?.id ?: 0,
                coverImage = relation?.node?.coverImage?.extraLarge ?: "",
                title = relation?.node?.title?.native ?: "",
                relation = relation?.relationType?.rawValue ?: ""
            )
        )
    }
    Overview(onNavigateToDetails = onNavigateToDetails,
        anime = Anime(title = anime.title?.native ?: "Unknown",
            coverImage = anime.coverImage?.extraLarge ?: "",
            format = anime.format?.name ?: "Unknown",
            seasonYear = anime.seasonYear.toString(),
            episodeAmount = anime.episodes ?: 0,
            averageScore = anime.averageScore ?: 0,
            tags = tags,
            description = anime.description ?: "No description found",
            relations = relations,
            infoList = mapOf("format" to anime.format?.name.orEmpty(),
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
                "synonyms" to synonyms,
                "nsfw" to anime.isAdult.toString()),
            genres = genres,
            trailerImage = anime.trailer?.thumbnail ?: "",
            // todo add dailymotion
            trailerLink = if (anime.trailer?.site == "youtube") "https://www.youtube.com/watch?v=${anime.trailer.id}" else if (anime.trailer?.site == "dailymotion") "" else "",
            externalLinks = externalLinks))

}

@Composable
private fun AniDetailTabs(
    modifier: Modifier = Modifier,
    titles: List<String>,
    tabSelected: Int,
    onTabSelected: (Int) -> Unit,
) {
    ScrollableTabRow(selectedTabIndex = tabSelected, modifier = modifier, tabs = {
        titles.forEachIndexed { index, title ->
            val selected = index == tabSelected
            Tab(selected = selected,
                onClick = { onTabSelected(index) },
                text = { Text(text = title) })
        }
    })
}

@Composable
fun Characters() {
    Text("Characters", modifier = Modifier.fillMaxSize())
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Overview(
    anime: Anime,
    onNavigateToDetails: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row {
            if (anime.coverImage != "") {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(anime.coverImage)
                        .crossfade(true).build(),
//                placeholder = PlaceholderPainter(MaterialTheme.colorScheme.surfaceTint),
//                placeholder = painterResource(id = R.drawable.kimetsu_no_yaiba),
                    contentDescription = "Cover of ${anime.title}",
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))

                )
            } else {
                RoundedImage(anime.title)
            }
            Column {
                Text(
                    text = anime.title,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Column(modifier = Modifier.padding(start = 24.dp)) {
                    IconWithText(
                        R.drawable.anime_details_movie,
                        anime.format,
                        textColor = MaterialTheme.colorScheme.onSurface
                    )
                    IconWithText(
                        R.drawable.anime_details_calendar,
                        anime.seasonYear,
                        textColor = MaterialTheme.colorScheme.onSurface
                    )
                    IconWithText(
                        R.drawable.anime_details_timer,
                        if (anime.episodeAmount == 1) "${anime.episodeAmount} Episode" else "${anime.episodeAmount} Episodes",
                        textColor = MaterialTheme.colorScheme.onSurface
                    )
                    IconWithText(
                        R.drawable.anime_details_heart,
                        "${anime.averageScore}% Average score",
                        textColor = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        FlowRow(
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth()
        ) {
            for (genre in anime.genres) {
                val colorContainer = MaterialTheme.colorScheme.secondaryContainer
//                BackgroundTextButton(genre, colorContainer)
                FilledTonalButton(
                    onClick = { }, modifier = Modifier.padding(bottom = 6.dp, end = 12.dp)
                ) {
                    Text(text = genre)
                }
            }
        }
        if (anime.highestRated != "") {
            IconWithText(
                icon = R.drawable.anime_details_rating_star,
                text = anime.highestRated,
                iconTint = MaterialTheme.colorScheme.secondary,
                textColor = MaterialTheme.colorScheme.onSurface
            )
        }
        if (anime.mostPopular != "") {
            IconWithText(
                icon = R.drawable.anime_details_heart,
                text = anime.mostPopular,
                iconTint = MaterialTheme.colorScheme.secondary,
                textColor = MaterialTheme.colorScheme.onSurface
            )
        }
        HeadLine("Description")
        val color = MaterialTheme.colorScheme.onSurface.toArgb()
        AndroidView(factory = { context ->
            TextViewCustom(context, anime.description, color)
        })
        HeadLine("Relations")
        LazyRow {
            items(anime.relations) { relation ->
                Column(modifier = Modifier
                    .padding(end = 12.dp)
                    .width(120.dp)
                    .height(320.dp)
                    .clickable {
                        onNavigateToDetails(relation.id)
                    }
                ) {
                    //todo change not null assertion
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(relation.coverImage).crossfade(true).build(),
//                placeholder = PlaceholderPainter(MaterialTheme.colorScheme.surfaceTint),
                        contentDescription = "Cover of ${relation.title}",
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .fillMaxWidth()
                    )
                    Text(
                        text = relation.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(10.dp),
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = relation.relation,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(10.dp),
                        overflow = TextOverflow.Ellipsis
                    )
                }
//                Column(modifier = Modifier.padding(end = 22.dp)) {
//                    AsyncImage(model = ImageRequest.Builder(LocalContext.current)
//                        .data(relation.coverImage)
//                        .crossfade(true).build(),
//                        contentDescription = "Trailer of ${relation.title}",
//                        contentScale = ContentScale.FillWidth,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .clickable { }
//                            .clip(RoundedCornerShape(12.dp)))
//                    Text(
//                        text = relation.title,
//                        color = MaterialTheme.colorScheme.onSurface,
//                        style = MaterialTheme.typography.labelLarge,
//                        modifier = Modifier.padding(4.dp)
//                    )
//                    Text(
//                        text = relation.relation,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant,
//                        style = MaterialTheme.typography.labelLarge,
//                        modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 4.dp)
//                    )
//                }
            }
        }
        HeadLine("Info")
        Row {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                if (anime.infoList.containsKey("format")) {
                    InfoName("Format")
                }
                if (anime.infoList.containsKey("status")) {
                    InfoName("Status")
                }
                if (anime.infoList.containsKey("startDate")) {
                    InfoName("Start date")
                }
                if (anime.infoList.containsKey("endDate")) {
                    InfoName(text = "End date")
                }
                InfoName("Episodes")
                if (anime.infoList.containsKey("duration")) {
                    InfoName("Duration")
                }
                if (anime.infoList.containsKey("country")) {
                    InfoName("Country")
                }
                if (anime.infoList.containsKey("source")) {
                    InfoName("Source")
                }
                if (anime.infoList.containsKey("hashtag")) {
                    InfoName("Hashtag")
                }
                if (anime.infoList.containsKey("licensed")) {
                    InfoName("Licensed")
                }
                if (anime.infoList.containsKey("updatedAt")) {
                    InfoName("Updated at")
                }
                if (anime.infoList.containsKey("nsfw")) {
                    InfoName("NSFW")
                }
                if (anime.infoList.containsKey("synonyms")) {
                    InfoName("Synonyms")
                }
            }
            Column {
                if (anime.infoList.containsKey("format")) {
                    InfoData(anime.infoList["format"]!!)
                }
                if (anime.infoList.containsKey("status")) {
                    InfoData(anime.infoList["status"]!!)
                }
                if (anime.infoList.containsKey("startDate")) {
                    InfoData(anime.infoList["startDate"]!!)
                }
                if (anime.infoList.containsKey("endDate")) {
                    InfoData(anime.infoList["endDate"]!!)
                }
                InfoData(anime.episodeAmount.toString())
                if (anime.infoList.containsKey("duration")) {
                    InfoData(anime.infoList["duration"]!!)
                }
                if (anime.infoList.containsKey("country")) {
                    InfoData(anime.infoList["country"]!!)
                }
                if (anime.infoList.containsKey("source")) {
                    InfoData(anime.infoList["source"]!!)
                }
                if (anime.infoList.containsKey("hashtag")) {
                    InfoData(anime.infoList["hashtag"]!!)
                }
                if (anime.infoList.containsKey("licensed")) {
                    InfoData(anime.infoList["licensed"]!!)
                }
                if (anime.infoList.containsKey("updatedAt")) {
                    InfoData(anime.infoList["updatedAt"]!!)
                }
                if (anime.infoList.containsKey("nsfw")) {
                    InfoData(anime.infoList["nsfw"]!!)
                }
                if (anime.infoList.containsKey("synonyms")) {
                    InfoData(anime.infoList["synonyms"]!!)
                }
            }
        }
        var showSpoilers by remember {
            mutableStateOf(false)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            HeadLine("Tags")
            if (anime.tags.any { tag -> tag.isMediaSpoiler }) {
                IconWithText(icon = if (showSpoilers) R.drawable.anime_detail_not_visible else R.drawable.anime_detail_visible,
                    text = if (showSpoilers) "Hide spoilers" else "Show spoilers",
                    iconTint = MaterialTheme.colorScheme.error,
                    textColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.clickable { showSpoilers = !showSpoilers })
            }
        }
        FlowRow(horizontalArrangement = Arrangement.Start) {
            for (tag in anime.tags) {
                if (!tag.isMediaSpoiler) {
                    ElevatedButton(
                        onClick = { }, modifier = Modifier.padding(end = 12.dp, bottom = 4.dp)
                    ) {
                        Text(
                            buildAnnotatedString {
                                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                                    append("${tag.rank}% ")
                                }
                                withStyle(style = SpanStyle()) {
                                    append(tag.name)
                                }
                            })
//                        Text(text = tag.name, style = MaterialTheme.typography.labelMedium)
                    }
                } else if (showSpoilers) {
                    ElevatedButton(
                        onClick = { },
                        modifier = Modifier.padding(end = 12.dp, bottom = 4.dp),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            disabledContainerColor = MaterialTheme.colorScheme.onErrorContainer,
                            disabledContentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text(
                            buildAnnotatedString {
                                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                                    append("${tag.rank}% ")
                                }
                                withStyle(style = SpanStyle()) {
                                    append(tag.name)
                                }
                            })
                    }
                }
            }
        }
        HeadLine("Trailer")
        val uriHandler = LocalUriHandler.current
        if (anime.trailerImage == "") {
            Image(painter = painterResource(id = R.drawable.kimetsu_no_yaiba_trailer),
                contentDescription = "Trailer",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { uriHandler.openUri(anime.trailerLink) })
        } else {
            AsyncImage(model = ImageRequest.Builder(LocalContext.current)
                .data(anime.trailerImage)
                .crossfade(true).build(),
//                placeholder = PlaceholderPainter(MaterialTheme.colorScheme.surfaceTint),
//                placeholder = painterResource(id = R.drawable.kimetsu_no_yaiba),
                contentDescription = "Trailer of ${anime.title}",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { uriHandler.openUri(anime.trailerLink) }
                    .clip(RoundedCornerShape(12.dp)))
        }
        HeadLine("External links")
        FlowRow() {
            for (link in anime.externalLinks) {
                OutlinedButton(
                    onClick = { uriHandler.openUri(link.url) },
                    modifier = Modifier.padding(end = 8.dp, bottom = 4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (link.icon.isNotBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(link.icon)
                                    .crossfade(true).build(),
                                contentDescription = link.site,
                                colorFilter = ColorFilter.tint(
                                    Color(link.color.toColorInt())
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(text = link.site)
                    }
                }
            }
        }
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
private fun InfoName(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

@Composable
private fun InfoData(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
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
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    textColor: Color
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
            color = textColor
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
    AnilistTheme {
        Surface {
            Overview(
                onNavigateToDetails = {},
                anime = Anime(
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
                    relations = emptyList(),
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
                        "synonyms" to "KnY 3ดาบพิฆาตอสูร ภาค 3 บทหมู่บ้านช่างตีดาบ\n" + "Demon Slayer: Kimetsu no Yaiba - Le village des forgerons\n" + "Истребитель демонов: Kimetsu no Yaiba. Деревня кузнецов",
                        "nsfw" to "No"
                    ),
                    tags = listOf(
                        Tag(name = "Demons", 96, false), Tag(name = "Shounen", rank = 40, true)
//                        "Shounen",
//                        "Swordplay",
//                        "Male Protagonist",
//                        "Super Power",
//                        "Gore",
//                        "Monster Girl",
//                        "Body Horror",
//                        "Historical",
//                        "CGI",
//                        "Femaile Protagonist",
//                        "Orphan",
//                        "Rural"
                    ),
                    trailerImage = "",
                    trailerLink = "https://www.youtube.com/watch?v=a9tq0aS5Zu8",
                    externalLinks = listOf(
                        Link(
                            "https://kimetsu.com/anime/katanakajinosatohen/",
                            "Official Site",
                            "Japanese",
                            "",
                            ""
                        )
                    )
                )
            )
        }
    }
}