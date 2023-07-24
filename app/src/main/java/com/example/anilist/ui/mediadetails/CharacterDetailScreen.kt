package com.example.anilist.ui.mediadetails

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.anilist.R
import com.example.anilist.data.models.CharacterDetail
import com.example.anilist.data.models.CharacterMediaConnection
import com.example.anilist.data.models.StaffDetail
import com.example.anilist.ui.Dimens
import de.charlex.compose.HtmlText

@Composable
fun CharacterDetailScreen(
    id: Int,
    mediaDetailsViewModel: MediaDetailsViewModel = hiltViewModel(),
    onNavigateToStaff: (Int) -> Unit,
    onNavigateToMedia: (Int) -> Unit
) {
    val character by mediaDetailsViewModel.character.observeAsState()
    mediaDetailsViewModel.fetchCharacter(id)
    AnimatedVisibility(visible = character != null) {
        CharacterDetail(character ?: CharacterDetail(), onNavigateToStaff, onNavigateToMedia)
    }
//    if (character != null) {
//    } else {
//        LoadingCircle()
//    }
}

@Composable
private fun CharacterDetail(
    character: CharacterDetail,
    onNavigateToStaff: (Int) -> Unit,
    onNavigateToMedia: (Int) -> Unit
) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        AvatarAndName(
            character.coverImage,
            character.userPreferredName,
            character.alternativeNames,
            character.alternativeSpoilerNames,
            character.favorites,
            modifier = Modifier.padding(Dimens.PaddingNormal)
        )
        Description(character.description)
        Headline("Voice actors")
        VoiceActors(character.voiceActors, onNavigateToStaff)
        Headline("Related media")
        RelatedMedia(character.relatedMedia, onNavigateToMedia)
    }
}

@Composable
fun RelatedMedia(
    relatedMedia: List<CharacterMediaConnection>,
    onNavigateToMedia: (Int) -> Unit
) {
    LazyRow {
        items(relatedMedia) { media ->
            ImageWithTitleAndSubTitle(
                media.coverImage,
                media.title,
                media.characterRole,
                media.id,
                onNavigateToMedia
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
                staff.name,
                staff.language,
                staff.id,
                onNavigateToStaff
            )
        }
    }
}

@Composable
private fun ImageWithTitleAndSubTitle(
    coverImage: String,
    title: String,
    subTitle: String,
    id: Int,
    onClick: (Int) -> Unit
) {
    Column(Modifier.padding(start = Dimens.PaddingNormal).clickable { onClick(id) }) {
        CoverImage(
            coverImage = coverImage,
            userPreferredName = title,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = Dimens.PaddingSmall)
        )
        Text(
            text = subTitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
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
            end = Dimens.PaddingNormal
        )
    )
}

@Composable
fun Description(description: String) {
    HtmlText(
        text = description,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 16.sp,
        modifier = Modifier.padding(horizontal = Dimens.PaddingNormal)
    )
}

@Composable
fun AvatarAndName(
    coverImage: String,
    userPreferredName: String,
    alternativeNames: List<String>,
    alternativeSpoilerNames: List<String>,
    favorites: Int,
    modifier: Modifier = Modifier
) {
    Row(modifier = Modifier.padding(bottom = Dimens.PaddingNormal).then(modifier)) {
        CoverImage(
            coverImage,
            userPreferredName
        )
        Column(modifier = Modifier.padding(start = Dimens.PaddingNormal)) {
            Text(
                text = userPreferredName,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = alternativeNames.joinToString(separator = ", "),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (alternativeSpoilerNames.isNotEmpty()) {
                var showSpoilerNames by remember { mutableStateOf(false) }
                val modifier = if (!showSpoilerNames) {
                    Modifier.background(
                        MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.medium
                    ).padding(Dimens.PaddingSmall)
                } else {
                    Modifier
                }
                Text(
                    text = if (showSpoilerNames) {
                        alternativeSpoilerNames.joinToString(
                            separator = ", "
                        )
                    } else {
                        "Show spoiler names"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = if (showSpoilerNames) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.clickable {
                        showSpoilerNames = !showSpoilerNames
                    }.then(modifier)

                )
            }
            IconWithText(
                icon = R.drawable.anime_details_heart,
                text = favorites.toString(),
                textColor = MaterialTheme.colorScheme.onSurface,
                iconTint = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun CoverImage(coverImage: String, userPreferredName: String, modifier: Modifier = Modifier) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(coverImage)
            .crossfade(true).build(),
        contentDescription = "Profile image of $userPreferredName",
        contentScale = ContentScale.FillHeight,
        modifier = Modifier
            .height(200.dp)
            .then(modifier)
            .clip(RoundedCornerShape(12.dp))
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
            alternativeNames = listOf("Footabller", "Cool dude", "Not so cool dude"),
            alternativeSpoilerNames = listOf("Secret name", "Actually a spy")
        ),
        onNavigateToMedia = {},
        onNavigateToStaff = {}
    )
}
