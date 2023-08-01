package com.example.anilist.ui.mediadetails

import android.content.Intent
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.anilist.R
import com.example.anilist.data.models.CharacterDetail
import com.example.anilist.data.models.CharacterMediaConnection
import com.example.anilist.data.models.StaffDetail
import com.example.anilist.data.repository.MediaDetailsRepository
import com.example.anilist.utils.quantityStringResource
import com.example.anilist.ui.Dimens
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewStateWithHTMLData
import org.jsoup.Jsoup


private const val TAG = "CharacterDetailScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(
    id: Int,
    mediaDetailsViewModel: MediaDetailsViewModel = hiltViewModel(),
    onNavigateToStaff: (Int) -> Unit,
    onNavigateToMedia: (Int) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val character by mediaDetailsViewModel.character.observeAsState()
//    val isFavourite by mediaDetailsViewModel.isFavouriteCharacter.observeAsState(false)
    mediaDetailsViewModel.fetchCharacter(id)
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
                character = character ?: CharacterDetail(),
                isFavorite = character?.isFavourite ?: false,
                onNavigateToStaff = onNavigateToStaff,
                onNavigateToMedia = onNavigateToMedia,
                modifier = Modifier.padding(
                    top = it.calculateTopPadding(),
                    bottom = Dimens.PaddingNormal,
                ),
                toggleFavorite = {
                    mediaDetailsViewModel.toggleFavourite(
                        MediaDetailsRepository.LikeAbleType.CHARACTER,
                        id
                    )
                },
            )
        }
    }
//    if (character != null) {
//    } else {
//        LoadingCircle()
//    }
}

@Composable
private fun CharacterDetail(
    character: CharacterDetail,
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
        Headline("Voice actors")
        VoiceActors(character.voiceActors, onNavigateToStaff)
        Headline("Related media")
        RelatedMedia(character.relatedMedia, onNavigateToMedia)
    }
}

@Composable
fun RelatedMedia(
    relatedMedia: List<CharacterMediaConnection>,
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
fun VoiceActors(voiceActors: List<StaffDetail>, onNavigateToStaff: (Int) -> Unit) {
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
) {
    Column(
        Modifier
            .padding(start = Dimens.PaddingNormal)
            .clickable { onClick(id) },
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
        )
        Text(
            text = subTitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(120.dp)
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
    val state = rememberWebViewStateWithHTMLData(description)
    Column {
        val uriHandler = LocalUriHandler.current
        val context = LocalContext.current
        WebView(state = state, onCreated = {
            it.settings.javaScriptEnabled = true
            it.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    Log.i(TAG, "shouldOverrideUrlLoading was called")
                    val url = request?.url.toString()
                    uriHandler.openUri(url)
                    val intent = Intent(Intent.ACTION_VIEW, request!!.url)
                    startActivity(context, intent, null)
//                    if (url.startsWith("http://") || url.startsWith("https://")) {
//                    } else {
//                        view?.loadUrl(url)
//                    }
                    return true
                }
            }
        })
//        HtmlText(
//            text = description,
//            color = MaterialTheme.colorScheme.onSurface,
//            fontSize = 16.sp,
//            modifier = Modifier.padding(horizontal = Dimens.PaddingNormal),
//        )
        var isSpoilerVisible by remember { mutableStateOf(false) }
        val annotatedString = htmlToAnnotatedString(description, isSpoilerVisible)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Headline("Description")
            if (htmlToAnnotatedString(
                    htmlString = description,
                    isSpoilerVisible = false
                ).text != htmlToAnnotatedString(
                    htmlString = description,
                    isSpoilerVisible = true
                ).text
            ) {
                ShowHideSpoiler(showSpoilers = isSpoilerVisible) {
                    isSpoilerVisible = !isSpoilerVisible
                }
            }
        }
        ClickableText(
            text = annotatedString,
            onClick = { offset ->
                annotatedString.getStringAnnotations(
                    tag = "spoiler",
                    start = offset,
                    end = offset
                ).firstOrNull()?.let { annotation ->
//                    if (annotation.tag == "spoiler") {
                    isSpoilerVisible = !isSpoilerVisible
//                    }
                }
            },
            modifier = Modifier.padding(horizontal = Dimens.PaddingNormal)
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun htmlToAnnotatedString(htmlString: String, isSpoilerVisible: Boolean): AnnotatedString {
    Log.d(TAG, "original text is $htmlString")
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
    Log.d(TAG, "replace text is: $replacedSpans")
    val doc = Jsoup.parse(replacedSpans)
//    this returns only the spoilers
//    val elements = doc.body().allElements.not("*:not(span.markdown_spoiler:has(span))")
    val elements = doc.body().allElements
//    val processedBody = processElements(elements, StringBuilder())
    Log.d(TAG, elements.toString())
    val annotatedString = buildAnnotatedString {
        elements.forEach { element ->
            Log.d(TAG, "Current element tag being processed is ${element.tagName()}")
            when (element.tagName()) {
                "p" -> append(element.text() + "\n")
                "span" -> {
                    Log.d(TAG, "The span tag contains this text: ${element.text()}")
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
            Log.i(TAG, "Current favourite status is $isFavorite")
            IconWithText(
                icon = if (isFavorite) R.drawable.baseline_favorite_24 else R.drawable.anime_details_heart,
                text = favorites.toString(),
                textColor = MaterialTheme.colorScheme.onSurface,
                iconTint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.clickable {
                    Log.i(TAG, "Clicked on favourite")
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
        character = CharacterDetail(
            id = 12312,
            userPreferredName = "虎杖悠仁",
            description = "A muscular teenager with big light brown eyes and light brown spiky hair. Yuuji is a fair person who cares greatly for not only his comrades but anyone he views as people with their own wills, despite how deep or shallow his connection to them is. He cares greatly for the \"value of a life\" and to this end, he will ensure that others receive a \"proper death.\" He is easy to anger in the face of pure cruelty and unfair judgment of other people.\n" +
                    "He doesn't have the born talent required to use cursed techniques, but he has incredible athletic abilities and he is considered very strong for his age, as shown by when he easily beat a coach in Steel Ball Throw.",
            alternativeNames = listOf("Footballer", "Cool dude", "Not so cool dude"),
            alternativeSpoilerNames = listOf("Secret name", "Actually a spy"),
        ),
        isFavorite = true,
        onNavigateToMedia = {},
        onNavigateToStaff = {},
        toggleFavorite = {},
    )
}
