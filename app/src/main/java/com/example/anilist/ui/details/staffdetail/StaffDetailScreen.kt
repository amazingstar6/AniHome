package com.example.anilist.ui.details.staffdetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.anilist.R
import com.example.anilist.data.models.CharacterWithVoiceActor
import com.example.anilist.data.models.AniCharacterMediaConnection
import com.example.anilist.data.models.AniMediaType
import com.example.anilist.data.models.AniStaffDetail
import com.example.anilist.ui.Dimens
import com.example.anilist.ui.details.characterdetail.AvatarAndName
import com.example.anilist.ui.details.characterdetail.Description
import com.example.anilist.ui.details.characterdetail.Headline
import com.example.anilist.ui.details.characterdetail.ImageWithTitleAndSubTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffDetailScreen(
    id: Int,
    staffDetailViewModel: StaffDetailViewModel = hiltViewModel(),
    onNavigateToCharacter: (Int) -> Unit,
    onNavigateToMedia: (Int) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val staff by staffDetailViewModel.staff.collectAsStateWithLifecycle()
    val staffMedia = staffDetailViewModel.staffRoleMedia.collectAsLazyPagingItems()

    Scaffold(topBar = {
        TopAppBar(title = {
            AnimatedVisibility(visible = staff is StaffDetailUiState.Success, enter = fadeIn()) {
                Text(
                    text = (staff as? StaffDetailUiState.Success)?.staff?.userPreferredName
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
        AnimatedVisibility(visible = staff is StaffDetailUiState.Success) {
            StaffScreen(
                staff = (staff as? StaffDetailUiState.Success)?.staff ?: AniStaffDetail(),
                staffMedia = staffMedia,
                onNavigateToCharacter = onNavigateToCharacter,
                onNavigateToMedia = onNavigateToMedia,
                modifier = Modifier.padding(
                    top = it.calculateTopPadding(),
                    bottom = Dimens.PaddingNormal,
                ),
                toggleFavourite = {
                    staffDetailViewModel.toggleFavourite(
                        id,
                    )
                },
            )
        }
    }
}

@Composable
private fun StaffScreen(
    staff: AniStaffDetail,
    staffMedia: LazyPagingItems<AniCharacterMediaConnection>,
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
            modifier = Modifier.padding(horizontal = Dimens.PaddingNormal),
            isFavorite = staff.isFavourite,
            toggleFavourite = toggleFavourite,
        )
        if (staff.description.isNotBlank()) {
            Description(staff.description)
        }
        if (staff.voicedCharacters.isNotEmpty()) {
            Headline("Voiced characters")
            VoiceCharacters(staff.voicedCharacters, onNavigateToCharacter)
        }
        if (staff.animeStaffRole.isNotEmpty()) {
            Headline("Anime staff role")
            MediaStaffRole(staffMedia, onNavigateToMedia, type = AniMediaType.ANIME)
        }
        if (staff.mangaStaffRole.isNotEmpty()) {
            Headline("Manga staff role")
            MediaStaffRole(staffMedia, onNavigateToMedia, type = AniMediaType.MANGA)
        }
    }
}

@Composable
fun MangaStaffRole(media: List<AniCharacterMediaConnection>, onNavigateToMedia: (Int) -> Unit) {
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
fun MediaStaffRole(
    media: LazyPagingItems<AniCharacterMediaConnection>,
    onNavigateToMedia: (Int) -> Unit,
    type: AniMediaType
) {
    LazyRow(contentPadding = PaddingValues(end = Dimens.PaddingNormal)) {
        items(media.itemCount) { index ->
            val mediaConnection = media[index]
            if (mediaConnection != null && mediaConnection.type == type) {
                ImageWithTitleAndSubTitle(
                    mediaConnection.coverImage,
                    mediaConnection.title,
                    mediaConnection.characterRole,
                    mediaConnection.id,
                    onNavigateToMedia,
                )
            }
        }
    }
}

@Composable
fun VoiceCharacters(
    characterWithVoiceActors: List<CharacterWithVoiceActor>,
    onNavigateToCharacter: (Int) -> Unit
) {
    LazyRow {
        items(characterWithVoiceActors) { character ->
            ImageWithTitleAndSubTitle(
                character.coverImage,
                character.name,
                character.role.toString(LocalContext.current),
                character.id,
                onNavigateToCharacter,
            )
        }
    }
}
