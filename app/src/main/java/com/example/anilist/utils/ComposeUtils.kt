package com.example.anilist.utils

import android.content.Intent
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.annotation.PluralsRes
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.anilist.R
import com.example.anilist.ui.Dimens
import com.example.anilist.utils.Utils.Companion.toHexString
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewStateWithHTMLData

private const val TAG = "ComposeUtils"

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
    var showShimmer by remember { mutableStateOf(true) }
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current).data(coverImage)
            .crossfade(true).build(),
        contentDescription = contentDescription,
//        placeholder = painterResource(id = R.drawable.no_image),
//        fallback = painterResource(id = R.drawable.no_image),
        onSuccess = { showShimmer = false },
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .height(height)
            .width(width)
            .padding(
                Dimens.PaddingSmall
            )
            .clip(RoundedCornerShape(12.dp))
            .background(shimmerBrush(showShimmer = showShimmer))
    )
}

@Composable
fun FormattedHtmlWebView(html: String) {
    //fixme font not loading
    val formattedHtml = """
            <html>
            
            <style type="text/css">
                @font-face {
                    font-family: aclonica;
                    src: url("file:///android_asset/font/aclonica.ttf")
                }
                body {
                    color: ${MaterialTheme.colorScheme.onSurface.toHexString()};
                    background-color: ${MaterialTheme.colorScheme.surface.toHexString()};
                    line-height: 1.25;
                    font-family: aclonica;
                }
                .markdown_spoiler:not(hover), .markdown_spoiler:not(active) {
                    background-color: ${MaterialTheme.colorScheme.onSurface.toHexString()}
                }
                .markdown_spoiler:hover, .markdown_spoiler:active {
                    background-color: ${MaterialTheme.colorScheme.surface.toHexString()};
                }
            </style>
            
            <body>
            $html
            </body>
            </html>
        """.trimIndent()


    val state = rememberWebViewStateWithHTMLData(formattedHtml)
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    WebView(
        modifier = Modifier.padding(horizontal = Dimens.PaddingSmall),
        state = state,
        onCreated = {
//            it.settings.javaScriptEnabled = true
//            it.settings.standardFontFamily = "Monospace"
            it.webViewClient = object : AccompanistWebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    Log.i(TAG, "shouldOverrideUrlLoading was called")
                    val url = request?.url.toString()
                    uriHandler.openUri(url)
                    val intent = Intent(Intent.ACTION_VIEW, request!!.url)
                    ContextCompat.startActivity(context, intent, null)
//                    if (url.startsWith("http://") || url.startsWith("https://")) {
//                    } else {
//                        view?.loadUrl(url)
//                    }
                    return false
                }
            }
        })
}

@Composable
fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

@Composable
fun LoadingCircle(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center, modifier = Modifier
            .fillMaxSize()
            .then(modifier)
    ) {
        CircularProgressIndicator(
            modifier = Modifier,
        )
    }
}

@Composable
fun shimmerBrush(showShimmer: Boolean = true, targetValue:Float = 1000f): Brush {
    return if (showShimmer) {
        val shimmerColors = listOf(
            Color.LightGray.copy(alpha = 0.6f),
            Color.LightGray.copy(alpha = 0.2f),
            Color.LightGray.copy(alpha = 0.6f),
        )

        val transition = rememberInfiniteTransition(label = "Shimmer")
        val translateAnimation = transition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(800), repeatMode = RepeatMode.Reverse
            ), label = "Shimmer"
        )
        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnimation.value, y = translateAnimation.value)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.Transparent,Color.Transparent),
            start = Offset.Zero,
            end = Offset.Zero
        )
    }
}