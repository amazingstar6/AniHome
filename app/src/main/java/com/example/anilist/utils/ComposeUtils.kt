package com.example.anilist.utils

import android.content.Intent
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.annotation.PluralsRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.anilist.R
import com.example.anilist.ui.Dimens
import com.example.anilist.utils.Utils.Companion.toHexString
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.LoadingState
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewStateWithHTMLData
import org.jsoup.Jsoup
import timber.log.Timber

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
    modifier: Modifier = Modifier,
    width: Dp = MEDIUM_MEDIA_WIDTH.dp,
    height: Dp = MEDIUM_MEDIA_HEIGHT.dp,
    padding: Dp = Dimens.PaddingSmall
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
                padding
            )
            .clip(RoundedCornerShape(12.dp))
            .background(shimmerBrush(showShimmer = showShimmer))
            .then(modifier)
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

    AnimatedVisibility(
        visible = state.loadingState !is LoadingState.Loading,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
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
                        Timber.d("shouldOverrideUrlLoading was called")
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
fun shimmerBrush(showShimmer: Boolean = true, targetValue: Float = 1000f): Brush {
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
            colors = listOf(Color.Transparent, Color.Transparent),
            start = Offset.Zero,
            end = Offset.Zero
        )
    }
}

@Composable
fun htmlToAnnotatedString(htmlString: String, isSpoilerVisible: Boolean): AnnotatedString {
    val replacedSpans = htmlString.replace(
        """
        <p><span class='markdown_spoiler'><span>
    """.trimIndent().trim(), """
        <span class='markdown_spoiler'>
    """.trimIndent().trim(), ignoreCase = true
    ).replace(
        """
        </span></span></p>
    """.trimIndent().trim(), """
        </span>
    """.trimIndent().trim(), ignoreCase = true
    )
    val doc = Jsoup.parse(replacedSpans)
//    this returns only the spoilers
//    val elements = doc.body().allElements.not("*:not(span.markdown_spoiler:has(span))")
    val elements = doc.body().allElements
//    val processedBody = processElements(elements, StringBuilder())
    val annotatedString = buildAnnotatedString {
        elements.forEach { element ->
            when (element.tagName()) {
                "p" -> append(element.text() + "\n")
                "span" -> {
                    val classNames = element.classNames()
                    val spoiler = classNames.contains("markdown_spoiler")
                    if (!spoiler || isSpoilerVisible) {
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.error)) {
                            append(element.text())
                            if (spoiler) {
                                addStringAnnotation(
                                    tag = "spoiler",
                                    annotation = "spoiler",
                                    start = length - element.text().length,
                                    end = length
                                )
                            }
                        }
                    } else {

                    }

                }

                "br" -> append("\n")
                "strong" -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(element.text())
                    }
                }

                "div" -> append(element.text() + "\n")

                else -> {
//                    this.append(element.text())
                }
            }
        }
    }
    return annotatedString
}
