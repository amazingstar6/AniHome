package com.example.anilist.utils

import androidx.annotation.PluralsRes
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.anilist.R
import com.example.anilist.ui.Dimens

/**
 * Load a quantity string resource.
 *
 * @param id the resource identifier
 * @param quantity The number used to get the string for the current language's plural rules.
 * @return the string data associated with the resource
 */
@Composable
fun quantityStringResource(@PluralsRes id: Int, quantity: Int): String {
    val context = LocalContext.current
    return context.resources.getQuantityString(id, quantity)
}

/**
 * Load a quantity string resource with formatting.
 *
 * @param id the resource identifier
 * @param quantity The number used to get the string for the current language's plural rules.
 * @param formatArgs the format arguments
 * @return the string data associated with the resource
 */
@Composable
fun quantityStringResource(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any): String {
    val context = LocalContext.current
    return context.resources.getQuantityString(id, quantity, *formatArgs)
}

@Composable
fun AsyncImageRoundedCorners(
    coverImage: String,
    contentDescription: String,
    width: Dp = 125.dp,
    height: Dp = 175.dp
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current).data(coverImage)
            .crossfade(true).build(),
        contentDescription = contentDescription,
        placeholder = painterResource(id = R.drawable.no_image),
        fallback = painterResource(id = R.drawable.no_image),
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .height(height)
            .width(width)
            .padding(
                Dimens.PaddingSmall
            )
            .clip(RoundedCornerShape(12.dp))
    )
}