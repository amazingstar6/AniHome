package com.kevin.anihome.ui.details.mediadetails

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kevin.anihome.R
import com.kevin.anihome.ui.Dimens

@Composable
fun CoverLarge(
    coverImage: String,
    navigateBack: () -> Unit,
) {
//    var scale by remember { mutableFloatStateOf(1f) }
    Box {
        AnimatedVisibility(visible = coverImage != "", enter = fadeIn(), exit = fadeOut()) {
//        Box {
            AsyncImage(
                model =
                    ImageRequest.Builder(LocalContext.current).data(coverImage)
                        .crossfade(true).build(),
                contentDescription = "Cover of media",
                fallback = painterResource(id = R.drawable.no_image),
                contentScale = ContentScale.Fit,
                modifier =
                    Modifier
                        .fillMaxSize()
//                    .align(Alignment.Center)
//                    .graphicsLayer(
//                        scaleX = scale,
//                        scaleY = scale
//                    )
//                    .pointerInput(Unit) {
//                        detectTransformGestures { _, _, zoom, _ ->
//                            scale = when {
//                                scale < 1f -> 1f
//                                scale > 3f -> 3f
//                                else -> scale * zoom
//                            }
//                        }
//                    }
                        .clickable { navigateBack() },
            )
//        }
        }
        IconButton(onClick = navigateBack, modifier = Modifier.padding(Dimens.PaddingSmall)) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = stringResource(id = R.string.back),
            )
        }
    }
}
