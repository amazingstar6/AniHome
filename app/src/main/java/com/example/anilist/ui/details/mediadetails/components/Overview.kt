package com.example.anilist.ui.details.mediadetails.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ChipColors
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RichTooltipBox
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberRichTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.anilist.R
import com.example.anilist.data.models.AniLink
import com.example.anilist.data.models.AniLinkType
import com.example.anilist.data.models.AniMediaFormat
import com.example.anilist.data.models.AniMediaRelation
import com.example.anilist.data.models.AniMediaStatus
import com.example.anilist.data.models.AniMediaType
import com.example.anilist.data.models.AniSeason
import com.example.anilist.data.models.Media
import com.example.anilist.data.models.MediaDetailInfoList
import com.example.anilist.data.models.Tag
import com.example.anilist.ui.Dimens
import com.example.anilist.ui.details.mediadetails.IconWithText
import com.example.anilist.ui.details.mediadetails.QuickInfo
import com.example.anilist.ui.details.mediadetails.formatFuzzyDateToYearMonthDayString
import com.example.anilist.ui.theme.AnilistTheme
import com.example.anilist.utils.AsyncImageRoundedCorners
import com.example.anilist.utils.FormattedHtmlWebView
import com.example.anilist.utils.LARGE_MEDIA_HEIGHT
import com.example.anilist.utils.LARGE_MEDIA_WIDTH
import com.example.anilist.utils.SMALL_MEDIA_HEIGHT
import com.example.anilist.utils.Utils
import com.example.anilist.utils.defaultPlaceholder
import io.github.fornewid.placeholder.foundation.PlaceholderHighlight
import io.github.fornewid.placeholder.material3.placeholder
import io.github.fornewid.placeholder.material3.shimmer
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.URLEncoder

@Composable
fun Overview(
    media: Media,
    isLoading: Boolean,
    onNavigateToDetails: (Int) -> Unit,
    onNavigateToLargeCover: (String) -> Unit,
    navigateToStudioDetails: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = 100.dp),
        //            .padding(20.dp)
    ) {
        val anime: Media = media
        if (!isLoading) {
            OverviewAnimeCoverDetails(anime, anime.genres, onNavigateToLargeCover)
            OverviewDescription(anime.description)
            OverviewRelations(anime.relations, onNavigateToDetails)
            OverViewInfo(anime, navigateToStudioDetails)
            OverViewStudios(anime, navigateToStudioDetails)
            var showSpoilers by remember {
                mutableStateOf(false)
            }
            OverViewTags(anime.tags, showSpoilers) { showSpoilers = !showSpoilers }
            val uriHandler = LocalUriHandler.current
            OverviewTrailer(anime.trailerImage) { uriHandler.openUri(anime.trailerLink) }
            OverviewExternalLinks(anime) { uriHandler.openUri(it) }
        } else {
            AnimeCoverDetailsPlaceHolder()
            DescriptionPlaceHolder()
            RelationsPlaceHolder()
            // not necessary i think
//            InfoPlaceHolder()
//            StudiosPlaceholder()
//            TagsPlaceholder()
//            TrailerPlaceholder()
//            ExternalLinksPlaceholder()
        }
    }

}

@Composable
fun RelationsPlaceHolder() {
    Column(
        modifier = Modifier
            .padding(Dimens.PaddingNormal)
    ) {
        Text(
            text = "Relations placeholder",
            modifier = Modifier
                .padding(bottom = Dimens.PaddingNormal)
                .defaultPlaceholder()
        )
        Box(
            modifier = Modifier
                .height(SMALL_MEDIA_HEIGHT.dp)
                .fillMaxWidth()
                .defaultPlaceholder()
        )
    }
}

@Composable
fun AnimeCoverDetailsPlaceHolder() {
    Column {
        Row(modifier = Modifier.padding(Dimens.PaddingNormal)) {
            Box(
                modifier = Modifier
                    .size(LARGE_MEDIA_WIDTH.dp, LARGE_MEDIA_HEIGHT.dp)
                    .defaultPlaceholder()
            )
            Column(modifier = Modifier.padding(start = Dimens.PaddingNormal)) {
                Text(
                    text = "Placeholder for title",
                    modifier = Modifier
                        .padding(bottom = Dimens.PaddingNormal)
                        .defaultPlaceholder()
                )
                Box(
                    modifier = Modifier
                        .size((LARGE_MEDIA_HEIGHT * 3 / 4).dp)
                        .defaultPlaceholder()
                ) {

                }
            }
        }
        Box(
            modifier = Modifier
                .padding(Dimens.PaddingNormal)
                .fillMaxWidth()
                .height(160.dp)
                .defaultPlaceholder()
        )
    }
}

@Composable
fun DescriptionPlaceHolder() {
    Column(modifier = Modifier.padding(Dimens.PaddingNormal)) {
        Text(
            text = "Description",
            modifier = Modifier
                .padding(bottom = Dimens.PaddingNormal)
                .placeholder(
                    visible = true,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .placeholder(
                    visible = true,
                    highlight = PlaceholderHighlight.shimmer()
                )
        ) {

        }
    }
//    Box(
//        modifier = Modifier
//            .size(200.dp)
////            .background(Color.Red)
//            .background(shimmerBrush())
//    )
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun OverViewStudios(
    anime: Media,
    navigateToStudioDetails: (Int) -> Unit
) {
    if (anime.studios.isNotEmpty()) {
        HeadLine("Studios")
        FlowRow(modifier = Modifier.padding(horizontal = Dimens.PaddingNormal)) {
            anime.studios.forEach {
                OutlinedButton(
                    onClick = { navigateToStudioDetails(it.id) },
                    modifier = Modifier.padding(end = Dimens.PaddingSmall)
                ) {
                    Text(
                        text = it.name,
//                            style = MaterialTheme.typography.bodyMedium,
//                            color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewRelations(
    relations: List<AniMediaRelation>,
    onNavigateToDetails: (Int) -> Unit,
) {
    if (relations.isNotEmpty()) {
        HeadLine("Relations")
        LazyRow(contentPadding = PaddingValues(end = Dimens.PaddingNormal)) {
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
                            .height(SMALL_MEDIA_HEIGHT.dp)
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


@Composable
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
private fun OverviewAnimeCoverDetails(
    media: Media,
    genres: List<String>,
    showImageLarge: (String) -> Unit
) {
    val isAnime = media.type == AniMediaType.ANIME
    Row(modifier = Modifier.padding(Dimens.PaddingNormal)) {
        if (media.coverImage != "") {
            AsyncImageRoundedCorners(
                coverImage = media.coverImage,
                contentDescription = "Cover of ${media.title}",
                width = LARGE_MEDIA_WIDTH.dp,
                height = LARGE_MEDIA_HEIGHT.dp,
                modifier = Modifier.                    clickable {
                    showImageLarge(
                        //fixme
                        URLEncoder.encode(
                            media.coverImage,
//                                StandardCharsets.UTF_8
                        )
                    )
                },
                padding = 0.dp
            )
//            AsyncImage(
//                model = ImageRequest.Builder(LocalContext.current).data(media.coverImage)
//                    .crossfade(true).build(),
//                contentDescription = "Cover of ${media.title}",
//                placeholder = painterResource(id = R.drawable.no_image),
//                fallback = painterResource(id = R.drawable.no_image),
//                contentScale = ContentScale.Crop,
//                modifier = Modifier
//                    .height(LARGE_MEDIA_HEIGHT.dp)
//                    .width(LARGE_MEDIA_WIDTH.dp)
//                    .clip(RoundedCornerShape(12.dp))
//
//            )
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

    if (media.nextAiringEpisode.id != -1) {
        Timber.d("Time at airing is ${media.nextAiringEpisode.airingAt}")
        val tooltipState = rememberRichTooltipState(isPersistent = true)
        val tooltipScope = rememberCoroutineScope()
        RichTooltipBox(
            tooltipState = tooltipState,
            text = { Text(text = Utils.convertEpochToDateTimeTimeZoneString(media.nextAiringEpisode.airingAt.toLong() /* - Clock.System.now().epochSeconds*/)) }) {
            Text(
                text = "Episode ${media.nextAiringEpisode.episode} airs in ${
                    Utils.getRelativeTimeFuture(
                        media.nextAiringEpisode.timeUntilAiring.toLong()
                    )
                }",
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = FontFamily.SansSerif,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(
                        start = Dimens.PaddingNormal,
                        end = Dimens.PaddingNormal,
                        bottom = Dimens.PaddingSmall
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        tooltipScope.launch {
                            tooltipState.show()
                        }
                    }
                    .tooltipTrigger()
            )
        }
    }

    FlowRow(
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .padding(start = Dimens.PaddingNormal, end = Dimens.PaddingNormal)
            .fillMaxWidth(),
    ) {
        for (genre in genres) {
            ElevatedButton(
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
private fun OverviewExternalLinks(
    anime1: Media,
    openUri: (String) -> Unit,
) {
    if (anime1.externalLinks.isNotEmpty()) {
        val info by remember { mutableStateOf(anime1.externalLinks.filter { it.type == AniLinkType.INFO }) }
        val streaming by remember { mutableStateOf(anime1.externalLinks.filter { it.type == AniLinkType.STREAMING }) }
        val social by remember { mutableStateOf(anime1.externalLinks.filter { it.type == AniLinkType.SOCIAL }) }

        HeadLine("External links")
        if (info.isNotEmpty()) {
            SmallHeadLine("Info")
            LinkFlowRow(info, openUri)
        }
        if (streaming.isNotEmpty()) {
            SmallHeadLine("Streaming")
            LinkFlowRow(streaming, openUri)
        }
        if (social.isNotEmpty()) {
            SmallHeadLine("Social")
            LinkFlowRow(social, openUri)
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun LinkFlowRow(
    externalLinks: List<AniLink>,
    openUri: (String) -> Unit
) {
    FlowRow(modifier = Modifier.padding(horizontal = Dimens.PaddingNormal)) {
        for (link in externalLinks) {
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
                    Text(text = "${link.site} ${if (link.language != "") "(${link.language})" else ""}")
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
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
                RichTooltipBox(
                    text = { Text(text = tag.description) },
                    tooltipState = rememberRichTooltipState(
                        isPersistent = true
                    )
                ) {
                    SuggestionChip(
                        onClick = { },
                        modifier = Modifier
                            .padding(end = Dimens.PaddingNormal)
                            .tooltipTrigger(),
                        label = {
                            Text(
                                buildAnnotatedString {
                                    withStyle(
                                        style = SpanStyle(color = MaterialTheme.colorScheme.primary),
                                    ) {
                                        append("${tag.rank}% ")
                                    }
                                    withStyle(style = SpanStyle()) {
                                        append(tag.name)
                                    }
                                },
                            )
//                        Text(text = tag.name, style = MaterialTheme.typography.labelMedium)
                        })
                }
            } else if (showSpoilers) {
                RichTooltipBox(
                    text = { Text(text = tag.description) },
                    tooltipState = rememberRichTooltipState(
                        isPersistent = true
                    )
                ) {
                    SuggestionChip(
                        onClick = { },
                        modifier = Modifier
                            .padding(end = 12.dp, bottom = 4.dp)
                            .tooltipTrigger(),
                        colors = ChipColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            labelColor = MaterialTheme.colorScheme.onErrorContainer,
                            disabledContainerColor = MaterialTheme.colorScheme.onErrorContainer,
                            disabledLabelColor = MaterialTheme.colorScheme.onErrorContainer,
                            leadingIconContentColor = MaterialTheme.colorScheme.onErrorContainer,
                            trailingIconContentColor = MaterialTheme.colorScheme.onErrorContainer,
                            disabledLeadingIconContentColor = MaterialTheme.colorScheme.onErrorContainer,
                            disabledTrailingIconContentColor = MaterialTheme.colorScheme.onErrorContainer
                        ), label = {
                            Text(
                                buildAnnotatedString {
                                    withStyle(
                                        style = SpanStyle(
                                            color = MaterialTheme.colorScheme.error,
                                        ),
                                    ) {
                                        append("${tag.rank}% ")
                                    }
                                    withStyle(style = SpanStyle()) {
                                        append(tag.name)
                                    }
                                },
                            )
                        })
                }
            }
        }
    }
}

@Composable
private fun OverViewInfo(media: Media, navigateToStudioDetails: (Int) -> Unit) {
    HeadLine("Info")
    Column(
        modifier = Modifier
            .padding(horizontal = Dimens.PaddingNormal),
    ) {
        InfoDataItem("Format", media.infoList.format.ifBlank { "?" })


        InfoDataItem(
            "Status",
            if (media.infoList.status != AniMediaStatus.UNKNOWN) media.infoList.status.toString(
                LocalContext.current
            ) else "?"
        )
        InfoDataItem(
            "Start date",
            if (media.startDate != null) formatFuzzyDateToYearMonthDayString(startDate = media.startDate) else "?"
        )
        InfoDataItem(
            "End date",
            if (media.endDate != null) formatFuzzyDateToYearMonthDayString(startDate = media.endDate) else "?"
        )
        InfoDataItem(
            if (media.type == AniMediaType.ANIME) "Episodes" else "Chapters",
            if (media.type == AniMediaType.ANIME) {
                if (media.episodeAmount == -1) stringResource(id = R.string.question_mark) else media.episodeAmount.toString()
            } else {
                if (media.chapters == -1) stringResource(id = R.string.question_mark) else media.chapters.toString()
            }
        )
        InfoDataItem(
            "Duration",
            if (media.infoList.duration != -1) media.infoList.duration.toString() else "?"
        )

        InfoDataItem("Country", media.infoList.country.ifBlank { "?" })

        InfoDataItem("Source", media.infoList.source.ifBlank { "?" })
        val uriHandler = LocalUriHandler.current
        val uri = getTwitterUriFromHashtags(media.infoList.hashtag)
        Timber.d("Uri is $uri")
        //todo make text primary color
        InfoDataItem("Hashtag", media.infoList.hashtag.ifBlank { "?" },
            modifier = if (media.infoList.hashtag != "") Modifier
                .fillMaxWidth()
                .clickable {
                    uriHandler.openUri(uri)
                } else Modifier)
        InfoDataItem(
            "Licensed",
            if (media.infoList.licensed != null) media.infoList.licensed.toString() else "?",
        )
        InfoDataItem("Updated at", media.infoList.updatedAt.ifBlank { "?" })
        InfoDataItem(
            "NSFW",
            if (media.infoList.nsfw != null) media.infoList.nsfw.toString() else "?"
        )
        InfoDataItem("Synonyms", if (media.infoList.synonyms.isNotEmpty()) buildString {
            media.infoList.synonyms.forEachIndexed { index, synonym ->
                if (index != media.infoList.synonyms.lastIndex) {
                    append("$synonym, ")
                } else {
                    append(synonym)
                }
            }
        } else "?")
    }
}

fun getTwitterUriFromHashtags(hashtags: String): String {
    val amountOfHashtags = hashtags.count { it == '#' }
    if (amountOfHashtags == 1) {
        return "https://twitter.com/search?q=${hashtags.replace("#", "%23")}&src=typd"
    } else {
        val split = hashtags.split("#")
        Timber.d("Split is $split")
        return buildString {
            append("https://twitter.com/search?q=")
            split.forEachIndexed { index, string ->
                if (string.isNotBlank()) {
                    append("%23${string.trim()}")
                    if (index != split.lastIndex) {
                        append("+OR%20")
                    }
                }
            }
        }
    }
}

@Composable
fun InfoDataItem(infoName: String, infoData: String, modifier: Modifier = Modifier) {
    Row(
        modifier = Modifier
            .padding(bottom = Dimens.PaddingSmall)
            .then(modifier)
    ) {
        Text(
            text = infoName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = infoData,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
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

@Composable
private fun SmallHeadLine(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 4.dp, top = 8.dp, start = Dimens.PaddingNormal),
    )
}

@Preview(
    name = "Light mode",
    showBackground = true,
    heightDp = 2000,
    group = "Overview",
)
//@Preview(
//    name = "Night mode",
//    showBackground = true,
//    uiMode = Configuration.UI_MODE_NIGHT_YES,
//    group = "Overview",
//)
@Composable
fun OverviewPreview() {
    AnilistTheme {
        Surface {
            Overview(
                onNavigateToLargeCover = {},
                onNavigateToDetails = {},
                media = Media(
                    type = AniMediaType.ANIME,
                    title = "鬼滅の刃 刀鍛冶の里編",
                    coverImage = "",
                    format = AniMediaFormat.TV,
                    season = AniSeason.SPRING,
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
                        status = AniMediaStatus.FINISHED,
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
                        Tag(
                            name = "Demons",
                            96,
                            false,
                            description = "Media that involves a whole lot of demons."
                        ),
                        Tag(
                            name = "Shounen",
                            rank = 40,
                            true,
                            description = "Media meant for young virgins."
                        ),
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
                        AniLink(
                            "https://kimetsu.com/anime/katanakajinosatohen/",
                            "Official Site",
                            "Japanese",
                            "",
                            "",
                            type = AniLinkType.SOCIAL
                        ),
                    ),
                ), isLoading = true, navigateToStudioDetails = {})
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