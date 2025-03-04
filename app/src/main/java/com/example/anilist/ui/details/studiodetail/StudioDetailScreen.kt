package com.example.anilist.ui.details.studiodetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.anilist.R
import com.example.anilist.data.models.Media
import com.example.anilist.data.models.AniLikeAbleType
import com.example.anilist.ui.Dimens
import com.example.anilist.utils.AsyncImageRoundedCorners

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioDetailScreen(
    id: Int,
    navigateToMedia: (Int) -> Unit,
    navigateBack: () -> Unit,
    studioDetailViewModel: StudioDetailViewModel = hiltViewModel()
) {
    val studio by studioDetailViewModel.studio.collectAsState()
    val mediaList = studioDetailViewModel.mediaOfStudio.collectAsLazyPagingItems()
    LaunchedEffect(key1 = Unit) {
        studioDetailViewModel.getStudioDetails(id)
    }
    Scaffold(topBar = {
        TopAppBar(title = { Text(text = studio.name) }, navigationIcon = {
            IconButton(onClick = navigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(id = R.string.back)
                )
            }
        }, actions = {
            PlainTooltipBox(tooltip = { Text(text = "Add to favourites") }) {
                IconButton(onClick = {
                    studioDetailViewModel.toggleFavourite(
                        AniLikeAbleType.STUDIO,
                        studio.id
                    )
                }, modifier = Modifier.tooltipTrigger()) {
                    Icon(
                        painter = painterResource(if (studio.isFavourite) R.drawable.baseline_favorite_24 else R.drawable.anime_details_heart),
                        contentDescription = "Favourite"
                    )
                }
            }
            val uriHandler = LocalUriHandler.current
            val uri = "https://anilist.co/studio/${studio.id}"
            PlainTooltipBox(tooltip = {
                Text(
                    text = "Open in browser",
                )
            }) {
                IconButton(
                    onClick = { uriHandler.openUri(uri) },
                    modifier = Modifier.tooltipTrigger(),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_open_in_browser_24),
                        contentDescription = "open in browser",
                    )
                }
            }
        })
    }) {
        Column(modifier = Modifier.padding(top = it.calculateTopPadding())) {
            if (!studio.isAnimationStudio) {
                Text(
                    text = "This studio is not an animation studio",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                Text(
                    text = "This studio is an animation studio",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            LazyVerticalGrid(
                columns = GridCells.Adaptive(120.dp),
            ) {
                items(mediaList.itemCount) { index ->
                    val media = mediaList[index]
                    if (media != null) {
                        MediaCard(media, navigateToMedia)
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaCard(media: Media, navigateToMedia: (Int) -> Unit) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.clickable { navigateToMedia(media.id) }) {
        AsyncImageRoundedCorners(
            coverImage = media.coverImage,
            contentDescription = "Cover of ${media.title}"
        )
        Text(
            text = media.title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(horizontal = Dimens.PaddingNormal, vertical = Dimens.PaddingSmall)
                .width(125.dp)
        )
    }
}

@Preview
@Composable
fun StudioDetailPreview() {
    MediaCard(media = Media(title = "鬼滅の刃",), navigateToMedia = { })
}