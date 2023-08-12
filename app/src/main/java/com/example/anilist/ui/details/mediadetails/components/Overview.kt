package com.example.anilist.ui.details.mediadetails.components

import android.content.res.Configuration
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.anilist.R
import com.example.anilist.data.models.AniMediaFormat
import com.example.anilist.data.models.Link
import com.example.anilist.data.models.Media
import com.example.anilist.data.models.MediaDetailInfoList
import com.example.anilist.data.models.MediaType
import com.example.anilist.data.models.Relation
import com.example.anilist.data.models.Season
import com.example.anilist.data.models.Tag
import com.example.anilist.ui.Dimens
import com.example.anilist.ui.details.mediadetails.IconWithText
import com.example.anilist.ui.details.mediadetails.QuickInfo
import com.example.anilist.ui.theme.AnilistTheme
import com.example.anilist.utils.FormattedHtmlWebView
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalLayoutApi::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun Overview(
    media: Media?,
    onNavigateToDetails: (Int) -> Unit,
    onNavigateToLargeCover: (String) -> Unit,
    navigateToStudioDetails: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState()),
        //            .padding(20.dp)
    ) {
        val anime: Media = media ?: Media()
        OverviewAnimeCoverDetails(anime, anime.genres, onNavigateToLargeCover)
        OverviewDescription(anime.description)
        OverviewRelations(anime.relations, onNavigateToDetails)
        OverViewInfo(anime, navigateToStudioDetails)

        if (anime.studios.isNotEmpty()) {
            HeadLine("Studios")
            FlowRow(modifier = Modifier.padding(horizontal = Dimens.PaddingNormal)) {
                anime.studios.forEach {
                    TextButton(onClick = { navigateToStudioDetails(it.id) }) {
                        Text(
                            text = it.name,
//                            style = MaterialTheme.typography.bodyMedium,
//                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
        var showSpoilers by remember {
            mutableStateOf(false)
        }
        OverViewTags(anime.tags, showSpoilers) { showSpoilers = !showSpoilers }
        val uriHandler = LocalUriHandler.current
        OverviewTrailer(anime.trailerImage) { uriHandler.openUri(anime.trailerLink) }
        OverviewExternalLinks(anime) { uriHandler.openUri(it) }
    }

}

@Composable
private fun OverviewRelations(
    relations: List<Relation>,
    onNavigateToDetails: (Int) -> Unit,
) {
    if (relations.isNotEmpty()) {
        HeadLine("Relations")
        LazyRow {
            items(relations) { relation ->
                Column(
                    modifier = Modifier
                        .padding(start = Dimens.PaddingNormal)
                        .width(80.dp)
                        .clickable {
                            onNavigateToDetails(relation.id)
                        },
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(relation.coverImage).crossfade(true).build(),
                        contentDescription = "Cover of ${relation.title}",
                        contentScale = ContentScale.FillHeight,
                        modifier = Modifier
                            .height(140.dp)
                            .padding(bottom = Dimens.PaddingSmall)
                            .clip(RoundedCornerShape(12.dp)),
                    )
                    Text(
                        text = relation.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(bottom = 10.dp)
                            .width(80.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = relation.relation.toString(LocalContext.current),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 10.dp),
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewTrailer(
    trailerImage: String,
    openUri: () -> Unit,
) {
    if (trailerImage != "") {
        HeadLine("Trailer")
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(trailerImage)
                .crossfade(true).build(),
            contentDescription = "Trailer",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { openUri() }
                .padding(Dimens.PaddingNormal)
                .clip(RoundedCornerShape(12.dp)),
        )
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun OverviewAnimeCoverDetails(
    media: Media,
    genres: List<String>,
    showImageLarge: (String) -> Unit
) {
    val isAnime = media.type == MediaType.ANIME
    Row(modifier = Modifier.padding(Dimens.PaddingNormal)) {
        if (media.coverImage != "") {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(media.coverImage)
                    .crossfade(true).build(),
                contentDescription = "Cover of ${media.title}",
                placeholder = painterResource(id = R.drawable.no_image),
                fallback = painterResource(id = R.drawable.no_image),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(250.dp)
                    .width(175.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        showImageLarge(
                            URLEncoder.encode(
                                media.coverImage,
                                StandardCharsets.UTF_8
                            )
                        )
                    },
            )
        }
        Column {
            Text(
                text = media.title,
                modifier = Modifier.padding(start = 24.dp, bottom = 8.dp),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            QuickInfo(media, isAnime)
        }
    }
    FlowRow(
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .padding(top = 12.dp, start = Dimens.PaddingNormal, end = Dimens.PaddingNormal)
            .fillMaxWidth(),
    ) {
        for (genre in genres) {
            FilledTonalButton(
                onClick = { },
                modifier = Modifier.padding(bottom = 6.dp, end = 12.dp),
            ) {
                Text(text = genre)
            }
        }
    }
    Rankings(stats = media.stats, modifier = Modifier.padding(horizontal = Dimens.PaddingNormal))
//    if (anime1.highestRated != "") {
//        IconWithText(
//            icon = R.drawable.anime_details_rating_star,
//            text = anime1.highestRated,
//            iconTint = MaterialTheme.colorScheme.secondary,
//            textColor = MaterialTheme.colorScheme.onSurface,
//            modifier = Modifier.padding(horizontal = Dimens.PaddingNormal)
//        )
//    }
//    if (anime1.mostPopular != "") {
//        IconWithText(
//            icon = R.drawable.anime_details_heart,
//            text = anime1.mostPopular,
//            iconTint = MaterialTheme.colorScheme.secondary,
//            textColor = MaterialTheme.colorScheme.onSurface,
//            modifier = Modifier.padding(horizontal = Dimens.PaddingNormal)
//        )
//    }
}

@Composable
private fun OverviewDescription(description: String) {

//    val color = MaterialTheme.colorScheme.onSurface.toArgb()
//    HtmlText(text = description, style = MaterialTheme.typography.bodyMedium)
//    Column {
//        de.charlex.compose.HtmlText(
//            text = description,
//            style = MaterialTheme.typography.bodyMedium,
//            color = MaterialTheme.colorScheme.onSurfaceVariant,
//            modifier = Modifier.padding(horizontal = Dimens.PaddingNormal),
////                    colorMapping = mapOf(Color.Black to MaterialTheme.colorScheme.onSurface),
//        )
//    }
    Column {
        HeadLine("Description")
        FormattedHtmlWebView(html = description)
//    AndroidView(factory = { context ->
//        HtmlText(context, description, color)
//    })
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun OverviewExternalLinks(
    anime1: Media,
    openUri: (String) -> Unit,
) {
    if (anime1.externalLinks.isNotEmpty()) {
        HeadLine("External links")
        FlowRow(modifier = Modifier.padding(horizontal = Dimens.PaddingNormal)) {
            for (link in anime1.externalLinks) {
                OutlinedButton(
                    onClick = { openUri(link.url) },
                    modifier = Modifier.padding(end = 8.dp, bottom = 4.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (link.icon.isNotBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(link.icon)
                                    .crossfade(true).build(),
                                contentDescription = link.site,
                                colorFilter = ColorFilter.tint(
                                    Color(link.color.toColorInt()),
                                ),
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
@OptIn(ExperimentalLayoutApi::class)
private fun OverViewTags(
    tags: List<Tag>,
    showSpoilers: Boolean,
    toggleSpoilers: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
    ) {
        HeadLine("Tags")
        if (tags.any { tag -> tag.isMediaSpoiler }) {
            ShowHideSpoiler(showSpoilers, toggleSpoilers)
        }
    }
    FlowRow(
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier.padding(horizontal = Dimens.PaddingNormal),
    ) {
        for (tag in tags) {
            if (!tag.isMediaSpoiler) {
                ElevatedButton(
                    onClick = { },
                    modifier = Modifier.padding(end = 12.dp, bottom = 4.dp),
                ) {
                    Text(
                        buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(color = MaterialTheme.colorScheme.secondary),
                            ) {
                                append("${tag.rank}% ")
                            }
                            withStyle(style = SpanStyle()) {
                                append(tag.name)
                            }
                        },
                    )
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
                        disabledContentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                ) {
                    Text(
                        buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            ) {
                                append("${tag.rank}% ")
                            }
                            withStyle(style = SpanStyle()) {
                                append(tag.name)
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun OverViewInfo(anime1: Media, navigateToStudioDetails: (Int) -> Unit) {
    HeadLine("Info")
    Row {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = Dimens.PaddingNormal),
            verticalArrangement = Arrangement.Bottom,
        ) {
            if (anime1.infoList.format != "") {
                InfoName("Format")
            }
            if (anime1.infoList.status != "") {
                InfoName("Status")
            }
            if (anime1.infoList.startDate != "") {
                InfoName("Start date")
            }
            if (anime1.infoList.endDate != "") {
                InfoName(text = "End date")
            }
            InfoName("Episodes")
            if (anime1.infoList.duration != -1) {
                InfoName("Duration")
            }
            if (anime1.infoList.country != "") {
                InfoName("Country")
            }
            if (anime1.infoList.source != "") {
                InfoName("Source")
            }
            if (anime1.infoList.hashtag.isNotEmpty()) {
                InfoName("Hashtag")
            }
            if (anime1.infoList.licensed != null) {
                InfoName("Licensed")
            }
            if (anime1.infoList.updatedAt.isNotEmpty()) {
                InfoName("Updated at")
            }
            if (anime1.infoList.nsfw != null) {
                InfoName("NSFW")
            }
            if (anime1.infoList.synonyms.isNotEmpty()) {
                InfoName("Synonyms")
            }
//            if (anime1.infoList.containsKey("format")) {
//                InfoName("Format")
//            }
//            if (anime1.infoList.containsKey("status")) {
//                InfoName("Status")
//            }
//            if (anime1.infoList.containsKey("startDate")) {
//                InfoName("Start date")
//            }
//            if (anime1.infoList.containsKey("endDate")) {
//                InfoName(text = "End date")
//            }
//            InfoName("Episodes")
//            if (anime1.infoList.containsKey("duration")) {
//                InfoName("Duration")
//            }
//            if (anime1.infoList.containsKey("country")) {
//                InfoName("Country")
//            }
//            if (anime1.infoList.containsKey("source")) {
//                InfoName("Source")
//            }
//            if (anime1.infoList.containsKey("hashtag")) {
//                InfoName("Hashtag")
//            }
//            if (anime1.infoList.containsKey("licensed")) {
//                InfoName("Licensed")
//            }
//            if (anime1.infoList.containsKey("updatedAt")) {
//                InfoName("Updated at")
//            }
//            if (anime1.infoList.containsKey("nsfw")) {
//                InfoName("NSFW")
//            }
//            if (anime1.infoList.containsKey("synonyms")) {
//                InfoName("Synonyms")
//            }
        }
        Column {
            if (anime1.infoList.format != "") {
                InfoData(anime1.infoList.format)
            }
            if (anime1.infoList.status != "") {
                InfoData(anime1.infoList.status)
            }
            if (anime1.infoList.startDate != "") {
                InfoData(anime1.infoList.startDate)
            }
            if (anime1.infoList.endDate != "") {
                InfoData(anime1.infoList.endDate)
            }
            if (anime1.episodeAmount != -1) {
                InfoData(anime1.episodeAmount.toString())
            }
            if (anime1.infoList.duration != -1) {
                InfoData(anime1.infoList.duration.toString())
            }
            if (anime1.infoList.country != "") {
                InfoData(anime1.infoList.country)
            }
            if (anime1.infoList.source != "") {
                InfoData(anime1.infoList.source)
            }
            if (anime1.infoList.hashtag.isNotEmpty()) {
                InfoData(anime1.infoList.hashtag)
            }
            if (anime1.infoList.licensed != null) {
                InfoData(anime1.infoList.licensed.toString())
            }
            if (anime1.infoList.updatedAt.isNotEmpty()) {
                InfoData(anime1.infoList.updatedAt)
            }
            if (anime1.infoList.nsfw != null) {
                InfoData(anime1.infoList.nsfw.toString())
            }
            if (anime1.infoList.synonyms.isNotEmpty()) {
                InfoData(buildString {
                    anime1.infoList.synonyms.forEachIndexed { index, synonym ->
                        if (index != anime1.infoList.synonyms.lastIndex) {
                            append("$synonym, ")
                        } else {
                            append(synonym)
                        }
                    }
                })
            }
//            if (anime1.infoList.containsKey("format")) {
//                InfoData(anime1.infoList["format"]!!)
//            }
//            if (anime1.infoList.containsKey("status")) {
//                InfoData(anime1.infoList["status"]!!)
//            }
//            if (anime1.infoList.containsKey("startDate")) {
//                InfoData(anime1.infoList["startDate"]!!)
//            }
//            if (anime1.infoList.containsKey("endDate")) {
//                InfoData(anime1.infoList["endDate"]!!)
//            }
//            InfoData(anime1.episodeAmount.toString())
//            if (anime1.infoList.containsKey("duration")) {
//                InfoData(anime1.infoList["duration"]!!)
//            }
//            if (anime1.infoList.containsKey("country")) {
//                InfoData(anime1.infoList["country"]!!)
//            }
//            if (anime1.infoList.containsKey("source")) {
//                InfoData(anime1.infoList["source"]!!)
//            }
//            if (anime1.infoList.containsKey("hashtag")) {
//                InfoData(anime1.infoList["hashtag"]!!)
//            }
//            if (anime1.infoList.containsKey("licensed")) {
//                InfoData(anime1.infoList["licensed"]!!)
//            }
//            if (anime1.infoList.containsKey("updatedAt")) {
//                InfoData(anime1.infoList["updatedAt"]!!)
//            }
//            if (anime1.infoList.containsKey("nsfw")) {
//                InfoData(anime1.infoList["nsfw"]!!)
//            }
//            if (anime1.infoList.containsKey("synonyms")) {
//                InfoData(anime1.infoList["synonyms"]!!)
//            }
        }
    }
}

@Composable
private fun InfoName(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Composable
private fun InfoData(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
    )
}

@Composable
private fun HeadLine(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 4.dp, top = 8.dp, start = Dimens.PaddingNormal),
    )
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Preview(
    name = "Light mode",
    showBackground = true,
    heightDp = 2000,
    group = "Overview",
)
@Preview(
    name = "Night mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    group = "Overview",
)
@Composable
fun OverviewPreview() {
    AnilistTheme {
        Surface {
            Overview(
                onNavigateToLargeCover = {},
                onNavigateToDetails = {},
                media = Media(
                    type = MediaType.ANIME,
                    title = "鬼滅の刃 刀鍛冶の里編",
                    coverImage = "",
                    format = AniMediaFormat.TV,
                    season = Season.SPRING,
                    seasonYear = 2023,
                    episodeAmount = 11,
                    averageScore = 83,
                    genres = listOf("Action", "Adventure", "Drama", "Fantasy", "Supernatural"),
                    highestRated = "#99 Highest rated all time",
                    mostPopular = "#183 Most popular all time",
                    description = "Adaptation of the Swordsmith Village Arc.<br>\n<br>\nTanjiro\u2019s journey leads him to the Swordsmith Village, where he reunites with two Hashira, members of the Demon Slayer Corps\u2019 highest-ranking swordsmen - Mist Hashira Muichiro Tokito and Love Hashira Mitsuri Kanroji. With the shadows of demons lurking near, a new battle begins for Tanjiro and his comrades.\n<br><br>\n<i>Notes:<br>\n\u2022 The first episode has a runtime of ~49 minutes, and received an early premiere in cinemas worldwide as part of a special screening alongside the final two episodes of Kimetsu no Yaiba: Yuukaku-hen.<br>\n\u2022 The final episode has a runtime of ~52 minutes. </i>",
                    relations = emptyList(),
                    infoList = MediaDetailInfoList(
                        format = "TV",
                        status = "Finished",
                        startDate = "04-09-2023",
                        endDate = "06-18-2023",
                        duration = 24,
                        country = "Japan",
                        source = "Manga",
                        hashtag = "#鬼滅の刃",
                        licensed = true,
                        updatedAt = "04-06-2023",
                        synonyms = listOf(
                            "KnY 3ดาบพิฆาตอสูร ภาค 3 บทหมู่บ้านช่างตีดาบ",
                            "Demon Slayer: Kimetsu no Yaiba - Le village des forgerons",
                            "Истребитель демонов: Kimetsu no Yaiba. Деревня кузнецов"
                        ),
                        nsfw = false,
                    ),
                    tags = listOf(
                        Tag(name = "Demons", 96, false),
                        Tag(name = "Shounen", rank = 40, true),
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
                            "",
                        ),
                    ),
                ), navigateToStudioDetails = {})
        }
    }
}

@Preview(showBackground = true, group = "Description")
@Composable
fun OverviewDescriptionPreview() {
    OverviewDescription(
        "Gold Roger was known as the Pirate King, the strongest and most infamous being to have sailed the Grand Line. The capture and death of Roger by the World Government brought a change throughout the world. His last words before his death revealed the location of the greatest treasure in the world, One Piece. It was this revelation that brought about the Grand Age of Pirates, men who dreamed of finding One Piece (which promises an unlimited amount of riches and fame), and quite possibly the most coveted of titles for the person who found it, the title of the Pirate King.<br><br>\\nEnter Monkey D. Luffy, a 17-year-old boy that defies your standard definition of a pirate. Rather than the popular persona of a wicked, hardened, toothless pirate who ransacks villages for fun, Luffy’s reason for being a pirate is one of pure wonder; the thought of an exciting adventure and meeting new and intriguing people, along with finding One Piece, are his reasons of becoming a pirate. Following in the footsteps of his childhood hero, Luffy and his crew travel across the Grand Line, experiencing crazy adventures, unveiling dark mysteries and battling strong enemies, all in order to reach One Piece.<br><br>\\n<b>*This includes following special episodes:<\\/b><br>\\n- Chopperman to the Rescue! Protect the TV Station by the Shore! (Episode 336)<br>\\n- The Strongest Tag-Team! Luffy and Toriko's Hard Struggle! (Episode 492)<br>\\n- Team Formation! Save Chopper (Episode 542)<br>\\n- History's Strongest Collaboration vs. Glutton of the Sea (Episode 590)<br>\\n- 20th Anniversary! Special Romance Dawn (Episode 907)"
    )
}

@Composable
fun ShowHideSpoiler(showSpoilers: Boolean, toggleSpoilers: () -> Unit) {
    IconWithText(
        icon = if (showSpoilers) R.drawable.anime_detail_not_visible else R.drawable.anime_detail_visible,
        text = if (showSpoilers) "Hide spoilers" else "Show spoilers",
        iconTint = MaterialTheme.colorScheme.error,
        textColor = MaterialTheme.colorScheme.error,
        modifier = Modifier
            .clickable { toggleSpoilers() }
            .padding(end = Dimens.PaddingNormal),
    )
}