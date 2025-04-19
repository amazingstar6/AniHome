package com.kevin.anihome.ui.details.characterdetail

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kevin.anihome.R
import com.kevin.anihome.data.models.AniCharacterDetail
import com.kevin.anihome.data.models.AniCharacterMediaConnection
import com.kevin.anihome.data.models.AniStaffDetail
import com.kevin.anihome.ui.Dimens
import com.kevin.anihome.ui.details.mediadetails.IconWithText
import com.kevin.anihome.ui.mymedia.components.ErrorScreen
import com.kevin.anihome.utils.AsyncImageRoundedCorners
import com.kevin.anihome.utils.DESCRIPTION_HEIGHT
import com.kevin.anihome.utils.FormattedHtmlWebView
import com.kevin.anihome.utils.LARGE_MEDIA_HEIGHT
import com.kevin.anihome.utils.LARGE_MEDIA_WIDTH
import com.kevin.anihome.utils.MEDIUM_MEDIA_HEIGHT
import com.kevin.anihome.utils.MEDIUM_MEDIA_WIDTH
import com.kevin.anihome.utils.defaultPlaceholder
import com.kevin.anihome.utils.quantityStringResource
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(
    id: Int,
    characterDetailViewModel: CharacterDetailViewModel = hiltViewModel(),
    onNavigateToStaff: (Int) -> Unit,
    onNavigateToMedia: (Int) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val character by characterDetailViewModel.character.collectAsStateWithLifecycle()
    val toast = characterDetailViewModel.toast
    val context = LocalContext.current
    LaunchedEffect(key1 = Unit, block = {
        launch {
            toast.collect {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }
    })
    Scaffold(topBar = {
        TopAppBar(title = {
            AnimatedVisibility(
                visible = (character as? CharacterDetailUiState.Success) != null,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Text(
                    text =
                        (character as? CharacterDetailUiState.Success)?.character?.userPreferredName
                            ?: stringResource(R.string.question_mark),
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
        when (character) {
            is CharacterDetailUiState.Success, CharacterDetailUiState.Loading -> {
                CharacterDetail(
                    character =
                        (character as? CharacterDetailUiState.Success)?.character
                            ?: AniCharacterDetail(),
                    isFavorite =
                        (character as? CharacterDetailUiState.Success)?.character?.isFavourite
                            ?: false,
                    onNavigateToStaff = onNavigateToStaff,
                    onNavigateToMedia = onNavigateToMedia,
                    modifier =
                        Modifier.padding(
                            top = it.calculateTopPadding(),
                            bottom = Dimens.PaddingNormal,
                        ),
                    toggleFavorite = {
                        characterDetailViewModel.toggleFavourite(
                            id,
                        )
                    },
                    isLoading = character is CharacterDetailUiState.Loading,
                )
            }

            is CharacterDetailUiState.Error ->
                ErrorScreen(
                    errorMessage = (character as CharacterDetailUiState.Error).message,
                    reloadMedia = { characterDetailViewModel.fetchCharacter(id) },
                )

//            is CharacterDetailUiState.Loading -> {
//                LoadingCircle()
//            }
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
    isLoading: Boolean,
) {
    Column(
        modifier =
            Modifier
                .verticalScroll(rememberScrollState())
                .then(modifier),
    ) {
//        if (true) {
        AvatarAndName(
            character.coverImage,
            character.userPreferredName,
            character.alternativeNames,
            character.alternativeSpoilerNames,
            character.favorites,
            modifier = Modifier.padding(Dimens.PaddingNormal),
            isFavorite = isFavorite,
            toggleFavourite = toggleFavorite,
            isLoading = isLoading,
        )
//        } else {
//            Row {
//                Box(
//                    modifier = Modifier
//                        .padding(Dimens.PaddingNormal)
//                        .size(LARGE_MEDIA_WIDTH.dp, LARGE_MEDIA_HEIGHT.dp)
//                        .defaultPlaceholder()
//                )
//                Column(
//                    horizontalAlignment = Alignment.Start,
//                    modifier = Modifier.padding(top = Dimens.PaddingSmall)
//                ) {
//                    Text(
//                        text = "Long Japanese name placeholder",
//                        modifier = Modifier
//                            .padding(
//                                Dimens.PaddingSmall,
//                            )
//                            .defaultPlaceholder()
//                    )
//                    Text(
//                        text = "Alternative list of names placeholder",
//                        modifier = Modifier
//                            .padding(Dimens.PaddingSmall)
//                            .defaultPlaceholder()
//                    )
//                    Text(
//                        text = "Amount of favourites place holder",
//                        modifier = Modifier
//                            .padding(Dimens.PaddingSmall)
//                            .defaultPlaceholder()
//                    )
//                }
//            }
//        }
        if (!isLoading) {
            if (character.description != "") {
//                Description(description = character.description)
                Headline("Description")
                FormattedHtmlWebView(html = character.description)
            }
        } else {
            Text(
                text = "Description placeholder",
                modifier =
                    Modifier
                        .padding(Dimens.PaddingNormal)
                        .defaultPlaceholder(),
            )
            Box(
                modifier =
                    Modifier
                        .padding(Dimens.PaddingNormal)
                        .height(DESCRIPTION_HEIGHT.dp)
                        .fillMaxWidth()
                        .defaultPlaceholder(),
            )
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
fun VoiceActors(
    voiceActors: List<AniStaffDetail>,
    onNavigateToStaff: (Int) -> Unit,
) {
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
            .height(MEDIUM_MEDIA_HEIGHT.dp + 50.dp)
            .width(MEDIUM_MEDIA_WIDTH.dp)
            .then(modifier),
    ) {
//        CoverImage(
//            coverImage = coverImage,
//            userPreferredName = title,
//        )
        AsyncImageRoundedCorners(
            coverImage = coverImage,
            contentDescription = "Cover image of $title",
            height = MEDIUM_MEDIA_HEIGHT.dp,
            width = MEDIUM_MEDIA_WIDTH.dp,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier =
                Modifier
                    .padding(vertical = Dimens.PaddingSmall)
                    .width(120.dp),
            overflow = TextOverflow.Ellipsis,
            maxLines = 2,
        )
        Text(
            text = subTitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(120.dp),
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun Headline(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier =
            Modifier.padding(
                bottom = Dimens.PaddingSmall,
                top = Dimens.PaddingNormal,
                start = Dimens.PaddingNormal,
                end = Dimens.PaddingNormal,
            ),
    )
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
    isLoading: Boolean,
) {
    Row(
        modifier =
            Modifier
                .padding(bottom = Dimens.PaddingNormal)
                .then(modifier),
    ) {
        Timber.d("Is loading is $isLoading")
        AsyncImageRoundedCorners(
            coverImage = coverImage,
            contentDescription = "Cover image of $userPreferredName",
            height = LARGE_MEDIA_HEIGHT.dp,
            width = LARGE_MEDIA_WIDTH.dp,
            padding = 0.dp,
            modifier = Modifier.defaultPlaceholder(visible = isLoading),
        )
        Column(modifier = Modifier.padding(start = Dimens.PaddingNormal)) {
            Text(
                text = userPreferredName,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.defaultPlaceholder(isLoading),
            )
            if (alternativeNames.isNotEmpty()) {
                Text(
                    text = alternativeNames.joinToString(separator = ", "),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.defaultPlaceholder(isLoading),
                )
            }
            if (alternativeSpoilerNames.isNotEmpty()) {
                var showSpoilerNames by remember { mutableStateOf(false) }
                val textModifier =
                    if (!showSpoilerNames) {
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
                    text =
                        if (showSpoilerNames) {
                            alternativeSpoilerNames.joinToString(
                                separator = ", ",
                            )
                        } else {
                            quantityStringResource(
                                id = R.plurals.show_spoiler_name,
                                quantity = alternativeSpoilerNames.size,
                            )
                        },
                    style = MaterialTheme.typography.titleSmall,
                    color = if (showSpoilerNames) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onErrorContainer,
                    modifier =
                        Modifier
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
                modifier =
                    Modifier
                        .clickable {
                            toggleFavourite()
                        }
                        .defaultPlaceholder(isLoading),
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
    AsyncImageRoundedCorners(
        coverImage = coverImage,
        contentDescription = "Cover image of $userPreferredName",
        height = LARGE_MEDIA_HEIGHT.dp,
        width = LARGE_MEDIA_WIDTH.dp,
        padding = 0.dp,
    )
//    AsyncImage(
//        model = ImageRequest.Builder(LocalContext.current)
//            .data(coverImage)
//            .crossfade(true).build(),
//        contentDescription = "Cover image of $userPreferredName",
//        contentScale = ContentScale.FillHeight,
//        placeholder = painterResource(id = R.drawable.no_image),
//        fallback = painterResource(id = R.drawable.no_image),
//        modifier = Modifier
//            .height(200.dp)
//            .width(120.dp)
//            .then(modifier)
//            .clip(RoundedCornerShape(12.dp)),
//    )
}

@Preview(showBackground = true)
@Composable
fun CharacterDetailPreview() {
    CharacterDetail(
        character =
            AniCharacterDetail(
                id = 123327,
                userPreferredName = "レイチェル・ガードナー",
                description = "<p><strong>Height:</strong> 156cm (5'1&quot;)</p>\n<p>The protagonist of Satsuriku no Tenshi. Rachel is a 13-year-old girl that awakes in a building with no recollection of why she is there.</p>\n<p><span class='markdown_spoiler'><span>It is eventually revealed that she is actually the B1 floor master. The other floor masters say that she is ruthless, selfish, and manipulative, and willing to do anything to get what she wants, although it's a misnomer to call her an an evil or malicious person, as she doesn't inherently understand human morality and doesn't have actual cruel impulses.</span></span></p>\n<p>Ray is initially presented as extremely calm and collected, however she has issues with empathy and difficulty expressing or understanding emotions. While she has shown fear, anger, compassion, and happiness, these moments are rare and fleeting. She does not, however, fake emotions, and will not pretend to have feelings that aren't present. Ray is fixated on the idea of the existence of God. </p>\n<p><span class='markdown_spoiler'><span>Her desperation in needing a God she believes made her so delusional that she saw Zack as her God. Only until Zack helped her accept her actions that she finally came to terms with her warped view and realized that her true wish was simply to be wished in life and death. Afterwards, Ray started to act like a normal person, particularly in regards to Zack who she developed a close bond with over time. Ray becomes more expressive as the series goes on.</span></span></p>\n<p>(Source: Satsuriku no Tenshi Wiki, edited)</p>",
                alternativeNames = listOf("Ray"),
                alternativeSpoilerNames = emptyList(),
                favorites = 12123,
                coverImage = "https:\\s4.anilist.co\\file\\anilistcdn\\character\\large\\123327-V47VOOqFfsVy.jpg",
                isFavourite = true,
            ),
        isFavorite = true,
        onNavigateToMedia = {},
        onNavigateToStaff = {},
        toggleFavorite = {},
        isLoading = false,
    )
}
