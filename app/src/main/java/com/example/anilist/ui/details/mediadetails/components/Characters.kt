package com.example.anilist.ui.details.mediadetails.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.anilist.R
import com.example.anilist.data.models.CharacterWithVoiceActor
import com.example.anilist.ui.Dimens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Characters(
    isAnime: Boolean,
    languages: List<String>,
    characterWithVoiceActors: List<CharacterWithVoiceActor>,
    navigateToCharacter: (Int) -> Unit,
    navigateToStaff: (Int) -> Unit,
) {
    if (characterWithVoiceActors.isNotEmpty()) {
        var selected by remember { mutableIntStateOf(0) }
        Column(modifier = Modifier.fillMaxHeight()) {
            val lazyGridState = rememberLazyGridState()
            val coroutineScope = rememberCoroutineScope()
            if (isAnime) {
                LazyRow(
                    modifier = Modifier.padding(
//                    horizontal = Dimens.PaddingNormal,
                        vertical = Dimens.PaddingSmall
                    )
                ) {
                    itemsIndexed(languages) { index, language ->
                        FilterChip(
                            selected = selected == index,
                            onClick = {
                                coroutineScope.launch {
                                    lazyGridState.animateScrollToItem(0)
                                }
                                selected = index
                            },
                            label = { Text(text = language) },
                            modifier = Modifier.padding(start = Dimens.PaddingNormal),
                        )
                    }
                }
            }
            LazyVerticalGrid(
                state = lazyGridState,
                columns = GridCells.Adaptive(120.dp),
                modifier = Modifier.padding(horizontal = Dimens.PaddingNormal)
            ) {
                items(characterWithVoiceActors.filter { it.voiceActorLanguage == languages[selected] }) { character ->
                    Column(modifier = Modifier.padding(bottom = Dimens.PaddingLarge)) {
                        Column(
                            modifier = Modifier
                                .clickable { navigateToCharacter(character.id) }
                                .padding(12.dp)
                                .align(Alignment.CenterHorizontally),

                            ) {
                            ProfilePicture(character.coverImage, character.name)
                            Text(
                                text = character.name,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .padding(top = 6.dp, bottom = 6.dp)
                                    .width(120.dp)
//                                    .fillMaxWidth(),
                            )
                        }
                        if (isAnime) {
                            Column(
                                modifier = Modifier
                                    .clickable { navigateToStaff(character.voiceActorId) }
                                    .padding(12.dp)
                                    .align(Alignment.CenterHorizontally),

                                ) {
                                ProfilePicture(
                                    character.voiceActorCoverImage,
                                    character.voiceActorName
                                )
                                Text(
                                    text = character.voiceActorName,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .padding(top = 6.dp, bottom = 6.dp)
                                        .fillMaxWidth(),
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = stringResource(R.string.no_characters),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun ProfilePicture(coverImage: String, name: String) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(coverImage)
            .crossfade(true).build(),
        contentDescription = "Profile image of $name",
        contentScale = ContentScale.Crop,
        modifier = Modifier.Companion
            .size(150.dp)
            .clip(RoundedCornerShape(12.dp)),
    )
}