package com.example.anilist.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import io.github.fornewid.placeholder.foundation.PlaceholderHighlight
import io.github.fornewid.placeholder.material3.placeholder
import io.github.fornewid.placeholder.material3.shimmer

fun Modifier.defaultPlaceholder(): Modifier = composed {
    Modifier.placeholder(
        visible = true,
        highlight = PlaceholderHighlight.shimmer(),
    )
}