package com.example.anilist.ui.mediadetails

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.anilist.R
import com.example.anilist.data.models.CharacterDetail
import com.example.anilist.data.models.CharacterMediaConnectoin
import com.example.anilist.data.models.StaffDetail
import com.example.anilist.ui.Dimens
import de.charlex.compose.HtmlText

@Composable
fun CharacterDetailScreen(id: Int, mediaDetailsViewModel: MediaDetailsViewModel = hiltViewModel()) {
    val character by mediaDetailsViewModel.character.observeAsState(initial = CharacterDetail())
    mediaDetailsViewModel.fetchCharacter(id)
    CharacterDetail(character)
}

@Composable
private fun CharacterDetail(character: CharacterDetail) {
    Column(modifier = Modifier.padding(Dimens.PaddingNormal)) {
        AvatarAndName(
            character.coverImage,
            character.userPreferredName,
            character.nativeName,
            character.favorites
        )
        Description(character.description)
        Headline("Voice actors")
        VoiceActors(character.voiceActors)
        Headline("Related media")
        RelatedMedia(character.relatedMedia)
    }
}

@Composable
fun RelatedMedia(relatedMedia: List<CharacterMediaConnectoin>) {
    LazyRow {
        items(relatedMedia) { media ->
            ImageWithTitleAndSubTitle(media.coverImage, media.title, media.characterRole)
        }
    }
}

@Composable
private fun ImageWithTitleAndSubTitle(coverImage: String, title: String, subTitle: String) {
    Column() {
        CoverImage(coverImage = coverImage, userPreferredName = title)
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subTitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun VoiceActors(voiceActors: List<StaffDetail>) {
    LazyRow {
        items(voiceActors) { staff ->
            ImageWithTitleAndSubTitle(staff.coverImage, staff.name, staff.language)
        }
    }
}

@Composable
fun Headline(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun Description(description: String) {
    HtmlText(text = description, color = MaterialTheme.colorScheme.onSurface)
}

@Composable
fun AvatarAndName(
    coverImage: String,
    userPreferredName: String,
    nativeName: String,
    favorites: Int
) {
    Row(modifier = Modifier.padding(bottom = Dimens.PaddingNormal)) {
        CoverImage(
            coverImage,
            userPreferredName,
        )
        Column(modifier = Modifier.padding(start = Dimens.PaddingNormal)) {
            Text(text = userPreferredName, style = MaterialTheme.typography.headlineMedium)
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
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(150.dp)
            .clip(RoundedCornerShape(12.dp)).then(modifier)
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
                "He doesn't have the born talent required to use cursed techniques, but he has incredible athletic abilities and he is considered very strong for his age, as shown by when he easily beat a coach in Steel Ball Throw."
        )
    )
}
