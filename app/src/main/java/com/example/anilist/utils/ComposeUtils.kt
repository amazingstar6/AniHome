package com.example.anilist.utils

import android.content.Intent
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.PluralsRes
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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