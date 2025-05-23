package com.kevin.anihome.ui.details.studiodetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.kevin.anihome.R
import com.kevin.anihome.data.models.AniLikeAbleType
import com.kevin.anihome.data.models.Media
import com.kevin.anihome.ui.Dimens
import com.kevin.anihome.utils.AsyncImageRoundedCorners
import com.kevin.anihome.utils.MEDIUM_MEDIA_HEIGHT
import com.kevin.anihome.utils.MEDIUM_MEDIA_WIDTH

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioDetailScreen(
    id: Int,
    navigateToMedia: (Int) -> Unit,
    navigateBack: () -> Unit,
    studioDetailViewModel: StudioDetailViewModel = hiltViewModel(),
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
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.back),
                )
            }
        }, actions = {
            TooltipBox(
                tooltip = { PlainTooltip { Text(text = "Add to favourites") } },
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                state = rememberTooltipState(),
            ) {
                IconButton(onClick = {
                    studioDetailViewModel.toggleFavourite(
                        AniLikeAbleType.STUDIO,
                        studio.id,
                    )
                }) {
                    Icon(
                        painter =
                            painterResource(
                                if (studio.isFavourite) R.drawable.baseline_favorite_24 else R.drawable.anime_details_heart,
                            ),
                        contentDescription = "Favourite",
                    )
                }
            }
            val uriHandler = LocalUriHandler.current
            val uri = "https://anilist.co/studio/${studio.id}"
            TooltipBox(
                tooltip = {
                    PlainTooltip {
                        Text(
                            text = "Open in browser",
                        )
                    }
                },
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                state = rememberTooltipState(),
            ) {
                IconButton(
                    onClick = { uriHandler.openUri(uri) },
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
            LazyVerticalGrid(
                columns = GridCells.Adaptive(MEDIUM_MEDIA_WIDTH.dp),
            ) {
                item(span = { GridItemSpan(this.maxLineSpan)}) {
                    val text = if (!studio.isAnimationStudio) {
                        "This studio is not an animation studio"
                    } else {
                        "This studio is an animation studio"
                    }

                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(Dimens.PaddingSmall)
                    )
                }
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
private fun MediaCard(
    media: Media,
    navigateToMedia: (Int) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(horizontal = Dimens.PaddingSmall)
            .clickable { navigateToMedia(media.id) },
    ) {
        AsyncImageRoundedCorners(
            coverImage = media.coverImage,
            contentDescription = "Cover of ${media.title}",
            width = MEDIUM_MEDIA_WIDTH.dp,
            height = MEDIUM_MEDIA_HEIGHT.dp,
            padding = 0.dp
        )
        Text(
            text = media.title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2,
            modifier =
                Modifier
                    .padding(vertical = Dimens.PaddingSmall)
                    .width(125.dp),
        )
    }
}

@Preview
@Composable
fun StudioDetailPreview() {
    MediaCard(media = Media(title = "鬼滅の刃"), navigateToMedia = { })
}
