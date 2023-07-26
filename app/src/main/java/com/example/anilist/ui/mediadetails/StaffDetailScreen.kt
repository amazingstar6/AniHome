package com.example.anilist.ui.mediadetails

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.anilist.R
import com.example.anilist.data.models.Character
import com.example.anilist.data.models.CharacterMediaConnection
import com.example.anilist.data.models.StaffDetail
import com.example.anilist.data.repository.MediaDetailsRepository
import com.example.anilist.ui.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffDetailScreen(
    id: Int,
    mediaDetailsViewModel: MediaDetailsViewModel = hiltViewModel(),
    onNavigateToCharacter: (Int) -> Unit,
    onNavigateToMedia: (Int) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val staff by mediaDetailsViewModel.staff.observeAsState()
    mediaDetailsViewModel.fetchStaff(id)

    Scaffold(topBar = {
        TopAppBar(title = {
            AnimatedVisibility(visible = staff?.userPreferredName != null, enter = fadeIn()) {
                Text(
                    text = staff?.userPreferredName ?: stringResource(R.string.question_mark),
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
        AnimatedVisibility(visible = staff != null) {
            StaffDetail(
                staff = staff ?: StaffDetail(),
                onNavigateToCharacter = onNavigateToCharacter,
                onNavigateToMedia = onNavigateToMedia,
                modifier = Modifier.padding(
                    top = it.calculateTopPadding(),
                    bottom = Dimens.PaddingNormal,
                ),
                toggleFavourite = {
                    mediaDetailsViewModel.toggleFavourite(
                        MediaDetailsRepository.LikeAbleType.STAFF,
                        id,
                    )
                },
            )
        }
    }
}

@Composable
private fun StaffDetail(
    staff: StaffDetail,
    onNavigateToCharacter: (Int) -> Unit,
    onNavigateToMedia: (Int) -> Unit,
    modifier: Modifier = Modifier,
    toggleFavourite: () -> Unit,
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .then(modifier),
    ) {
        AvatarAndName(
            staff.coverImage,
            staff.userPreferredName,
            staff.alternativeNames,
            emptyList(),
            staff.favourites,
            modifier = Modifier.padding(Dimens.PaddingNormal),
            isFavorite = staff.isFavourite,
            toggleFavourite = toggleFavourite,
        )
        Description(staff.description)
        if (staff.voicedCharacters.isNotEmpty()) {
            Headline("Voiced characters")
            VoiceCharacters(staff.voicedCharacters, onNavigateToCharacter)
        }
        if (staff.animeStaffRole.isNotEmpty()) {
            Headline("Anime staff role")
            AnimeStaffRole(staff.animeStaffRole, onNavigateToMedia)
        }
        if (staff.mangaStaffRole.isNotEmpty()) {
            Headline("Manga staff role")
            MangaStaffRole(staff.mangaStaffRole, onNavigateToMedia)
        }
    }
}

@Composable
fun MangaStaffRole(media: List<CharacterMediaConnection>, onNavigateToMedia: (Int) -> Unit) {
    LazyRow {
        items(media) { media ->
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
fun AnimeStaffRole(media: List<CharacterMediaConnection>, onNavigateToMedia: (Int) -> Unit) {
    LazyRow {
        items(media) { media ->
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
fun VoiceCharacters(characters: List<Character>, onNavigateToCharacter: (Int) -> Unit) {
    LazyRow {
        items(characters) { character ->
            ImageWithTitleAndSubTitle(
                character.coverImage,
                character.name,
                character.role,
                character.id,
                onNavigateToCharacter,
            )
        }
    }
}
