package com.example.anilist.ui.details.characterdetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.anilist.R
import com.example.anilist.data.models.AniCharacterDetail
import com.example.anilist.data.models.AniCharacterMediaConnection
import com.example.anilist.data.models.AniStaffDetail
import com.example.anilist.ui.Dimens
import com.example.anilist.ui.details.mediadetails.IconWithText
import com.example.anilist.utils.FormattedHtmlWebView
import com.example.anilist.utils.quantityStringResource
import org.jsoup.Jsoup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(
    id: Int,
    characterDetailViewModel: CharacterDetailViewModel = hiltViewModel(),
    onNavigateToStaff: (Int) -> Unit,
    onNavigateToMedia: (Int) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val character by characterDetailViewModel.character.observeAsState()
    characterDetailViewModel.fetchCharacter(id)
    Scaffold(topBar = {
        TopAppBar(title = {
            AnimatedVisibility(visible = character?.userPreferredName != null, enter = fadeIn()) {
                Text(
                    text = character?.userPreferredName ?: stringResource(R.string.question_mark),
                )
            }
        }, navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = Icons.Default.ArrowBack.toString(),
                )
            }
        })
    }) {
        AnimatedVisibility(visible = character != null) {
            CharacterDetail(
                character = character ?: AniCharacterDetail(),
                isFavorite = character?.isFavourite ?: false,
                onNavigateToStaff = onNavigateToStaff,
                onNavigateToMedia = onNavigateToMedia,
                modifier = Modifier.padding(
                    top = it.calculateTopPadding(),
                    bottom = Dimens.PaddingNormal,
                ),
                toggleFavorite = {
                    characterDetailViewModel.toggleFavourite(
                        id
                    )
                },
            )
        }
    }
}

@Composable
private fun CharacterDetail(
    character: AniCharacterDetail,
    isFavorite: Boolean,
    onNavigateToStaff: (Int) -> Unit,
    onNavigateToMedia: (Int) -> Unit,
    modifier: Modifier = Modifier,
    toggleFavorite: () -> Unit,
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .then(modifier),
    ) {
        AvatarAndName(
            character.coverImage,
            character.userPreferredName,
            character.alternativeNames,
            character.alternativeSpoilerNames,
            character.favorites,
            modifier = Modifier.padding(Dimens.PaddingNormal),
            isFavorite = isFavorite,
            toggleFavourite = toggleFavorite,
        )
        if (character.description != "") {
            Description(character.description)
        }
        if (character.voiceActors.isNotEmpty()) {
            Headline("Voice actors")
            VoiceActors(character.voiceActors, onNavigateToStaff)
        }
        if (character.relatedMedia.isNotEmpty()) {
            Headline("Related media")
            RelatedMedia(character.relatedMedia, onNavigateToMedia)
        }
    }
}

@Composable
fun RelatedMedia(
    relatedMedia: List<AniCharacterMediaConnection>,
    onNavigateToMedia: (Int) -> Unit,
) {
    LazyRow {
        items(relatedMedia) { media ->
            ImageWithTitleAndSubTitle(
                media.coverImage,
                media.title,
                media.characterRole,
                media.id,
                onNavigateToMedia,
            )
        }
    }
}

@Composable
fun VoiceActors(voiceActors: List<AniStaffDetail>, onNavigateToStaff: (Int) -> Unit) {
    LazyRow {
        items(voiceActors) { staff ->
            ImageWithTitleAndSubTitle(
                staff.coverImage,
                staff.userPreferredName,
                staff.language,
                staff.id,
                onNavigateToStaff,
            )
        }
    }
}

@Composable
fun ImageWithTitleAndSubTitle(
    coverImage: String,
    title: String,
    subTitle: String,
    id: Int,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
//    startPadding: Dp = Dimens.PaddingNormal //todo remove start padding for every lazy using this composable
) {
    Column(
        Modifier
            .padding(start = Dimens.PaddingNormal)
            .clickable { onClick(id) }
            .height(300.dp)
            .then(modifier),
    ) {
        CoverImage(
            coverImage = coverImage,
            userPreferredName = title,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(vertical = Dimens.PaddingSmall)
                .width(120.dp),
            overflow = TextOverflow.Ellipsis,
            maxLines = 2
        )
        Text(
            text = subTitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(120.dp),
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun Headline(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(
            bottom = Dimens.PaddingSmall,
            top = Dimens.PaddingNormal,
            start = Dimens.PaddingNormal,
            end = Dimens.PaddingNormal,
        ),
    )
}

@Composable
fun Description(description: String) {
    Column {
        Headline("Description")
        FormattedHtmlWebView(html = description)
//        val state = rememberWebViewStateWithHTMLData(description)
//        val uriHandler = LocalUriHandler.current
//        val context = LocalContext.current
//        WebView(state = state, onCreated = {
//            it.settings.javaScriptEnabled = true
//            it.webViewClient = object : WebViewClient() {
//                override fun shouldOverrideUrlLoading(
//                    view: WebView?,
//                    request: WebResourceRequest?
//                ): Boolean {
//                    Log.i(TAG, "shouldOverrideUrlLoading was called")
//                    val url = request?.url.toString()
//                    uriHandler.openUri(url)
//                    val intent = Intent(Intent.ACTION_VIEW, request!!.url)
//                    startActivity(context, intent, null)
////                    if (url.startsWith("http://") || url.startsWith("https://")) {
////                    } else {
////                        view?.loadUrl(url)
////                    }
//                    return true
//                }
//            }
//        })
//        HtmlText(
//            text = description,
//            color = MaterialTheme.colorScheme.onSurface,
//            fontSize = 16.sp,
//            modifier = Modifier.padding(horizontal = Dimens.PaddingNormal),
//        )
//        var isSpoilerVisible by remember { mutableStateOf(false) }
//        val annotatedString = htmlToAnnotatedString(description, isSpoilerVisible)
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween,
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Headline("Description")
//            if (htmlToAnnotatedString(
//                    htmlString = description,
//                    isSpoilerVisible = false
//                ).text != htmlToAnnotatedString(
//                    htmlString = description,
//                    isSpoilerVisible = true
//                ).text
//            ) {
//                ShowHideSpoiler(showSpoilers = isSpoilerVisible) {
//                    isSpoilerVisible = !isSpoilerVisible
//                }
//            }
//        }
//        ClickableText(
//            text = annotatedString,
//            onClick = { offset ->
//                annotatedString.getStringAnnotations(
//                    tag = "spoiler",
//                    start = offset,
//                    end = offset
//                ).firstOrNull()?.let { annotation ->
////                    if (annotation.tag == "spoiler") {
//                    isSpoilerVisible = !isSpoilerVisible
////                    }
//                }
//            },
//            modifier = Modifier.padding(horizontal = Dimens.PaddingNormal)
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//        Text(text = description)
    }
}

@Composable
fun htmlToAnnotatedString(htmlString: String, isSpoilerVisible: Boolean): AnnotatedString {
    val replacedSpans = htmlString.replace(
        """
        <p><span class='markdown_spoiler'><span>
    """.trimIndent().trim(), """
        <span class='markdown_spoiler'>
    """.trimIndent().trim(), ignoreCase = true
    ).replace(
        """
        </span></span></p>
    """.trimIndent().trim(), """
        </span>
    """.trimIndent().trim(), ignoreCase = true
    )
    val doc = Jsoup.parse(replacedSpans)
//    this returns only the spoilers
//    val elements = doc.body().allElements.not("*:not(span.markdown_spoiler:has(span))")
    val elements = doc.body().allElements
//    val processedBody = processElements(elements, StringBuilder())
    val annotatedString = buildAnnotatedString {
        elements.forEach { element ->
            when (element.tagName()) {
                "p" -> append(element.text() + "\n")
                "span" -> {
                    val classNames = element.classNames()
                    val spoiler = classNames.contains("markdown_spoiler")
                    if (!spoiler || isSpoilerVisible) {
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.error)) {
                            append(element.text())
                            if (spoiler) {
                                addStringAnnotation(
                                    tag = "spoiler",
                                    annotation = "spoiler",
                                    start = length - element.text().length,
                                    end = length
                                )
                            }
                        }
                    } else {

                    }

                }

                "br" -> append("\n")
                "strong" -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(element.text())
                    }
                }

                "div" -> append(element.text() + "\n")

                else -> {
//                    this.append(element.text())
                }
            }
        }
    }
    return annotatedString
}

@Composable
fun AvatarAndName(
    coverImage: String,
    userPreferredName: String,
    alternativeNames: List<String>,
    alternativeSpoilerNames: List<String>,
    favorites: Int,
    isFavorite: Boolean,
    modifier: Modifier = Modifier,
    toggleFavourite: () -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(bottom = Dimens.PaddingNormal)
            .then(modifier),
    ) {
        CoverImage(
            coverImage,
            userPreferredName,
        )
        Column(modifier = Modifier.padding(start = Dimens.PaddingNormal)) {
            Text(
                text = userPreferredName,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (alternativeNames.isNotEmpty()) {
                Text(
                    text = alternativeNames.joinToString(separator = ", "),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (alternativeSpoilerNames.isNotEmpty()) {
                var showSpoilerNames by remember { mutableStateOf(false) }
                val textModifier = if (!showSpoilerNames) {
                    Modifier
                        .background(
                            MaterialTheme.colorScheme.errorContainer,
                            shape = MaterialTheme.shapes.medium,
                        )
                        .padding(Dimens.PaddingSmall)
                } else {
                    Modifier
                }
                Text(
                    text = if (showSpoilerNames) {
                        alternativeSpoilerNames.joinToString(
                            separator = ", ",
                        )
                    } else {
                        quantityStringResource(
                            id = R.plurals.show_spoiler_name,
                            quantity = alternativeSpoilerNames.size
                        )
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = if (showSpoilerNames) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier
                        .clickable {
                            showSpoilerNames = !showSpoilerNames
                        }
                        .padding(vertical = Dimens.PaddingSmall)
                        .then(textModifier),

                    )
            }
            IconWithText(
                icon = if (isFavorite) R.drawable.baseline_favorite_24 else R.drawable.anime_details_heart,
                text = favorites.toString(),
                textColor = MaterialTheme.colorScheme.onSurface,
                iconTint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.clickable {
                    toggleFavourite()
                },
            )
        }
    }
}

@Composable
private fun CoverImage(
    coverImage: String,
    userPreferredName: String,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(coverImage)
            .crossfade(true).build(),
        contentDescription = "Profile image of $userPreferredName",
        contentScale = ContentScale.FillHeight,
        placeholder = painterResource(id = R.drawable.no_image),
        fallback = painterResource(id = R.drawable.no_image),
        modifier = Modifier
            .height(200.dp)
            .width(120.dp)
            .then(modifier)
            .clip(RoundedCornerShape(12.dp)),
    )
}

@Preview(showBackground = true)
@Composable
fun CharacterDetailPreview() {
    CharacterDetail(
        character = AniCharacterDetail(
            id = 12312,
            userPreferredName = "虎杖悠仁",
            description = "<p><strong>Height:</strong> 156cm (5'1&quot;)</p>\n<p>The protagonist of Satsuriku no Tenshi. Rachel is a 13-year-old girl that awakes in a building with no recollection of why she is there.</p>\n<p><span class='markdown_spoiler'><span>It is eventually revealed that she is actually the B1 floor master. The other floor masters say that she is ruthless, selfish, and manipulative, and willing to do anything to get what she wants, although it's a misnomer to call her an an evil or malicious person, as she doesn't inherently understand human morality and doesn't have actual cruel impulses.</span></span></p>\n<p>Ray is initially presented as extremely calm and collected, however she has issues with empathy and difficulty expressing or understanding emotions. While she has shown fear, anger, compassion, and happiness, these moments are rare and fleeting. She does not, however, fake emotions, and will not pretend to have feelings that aren't present. Ray is fixated on the idea of the existence of God. </p>\n<p><span class='markdown_spoiler'><span>Her desperation in needing a God she believes made her so delusional that she saw Zack as her God. Only until Zack helped her accept her actions that she finally came to terms with her warped view and realized that her true wish was simply to be wished in life and death. Afterwards, Ray started to act like a normal person, particularly in regards to Zack who she developed a close bond with over time. Ray becomes more expressive as the series goes on.</span></span></p>\n<p>(Source: Satsuriku no Tenshi Wiki, edited)</p>",
            alternativeNames = listOf("Footballer", "Cool dude", "Not so cool dude"),
            alternativeSpoilerNames = listOf("Secret name", "Actually a spy"),
        ),
        isFavorite = true,
        onNavigateToMedia = {},
        onNavigateToStaff = {},
        toggleFavorite = {},
    )
}
