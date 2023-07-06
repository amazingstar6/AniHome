package com.example.anilist.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter

class PlaceholderPainter constructor(val backgroundColor: Color): Painter() {
    /**
     * Return the intrinsic size of the [Painter].
     * If the there is no intrinsic size (i.e. filling bounds with an arbitrary color) return
     * [Size.Unspecified].
     * If there is no intrinsic size in a single dimension, return [Size] with
     * [Float.NaN] in the desired dimension.
     * If a [Painter] does not have an intrinsic size, it will always draw within the full
     * bounds of the destination
     */
    override val intrinsicSize: Size
        get() = Size(Float.NaN, Float.NaN)

    /**
     * Implementation of drawing logic for instances of [Painter]. This is invoked
     * internally within [draw] after the positioning and configuring the [Painter]
     */
    override fun DrawScope.onDraw() {
        this.drawRect(backgroundColor)
    }
}