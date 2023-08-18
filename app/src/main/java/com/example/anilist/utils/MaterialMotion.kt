package com.example.anilist.utils

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Stable

private val MaterialTheme.motion: Unit
    get() {
        TODO("Not yet implemented")
    }

const val MATERIAL_MOTION_DURATION_MEDIUM_4 = 400 // milliseconds
const val MATERIAL_MOTION_DURATION_SHORT_4 = 200 // milliseconds
val MATERIAL_MOTION_EASING_EMPHASIZED = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
val MATERIAL_MOTION_EASING_EMPHASIZED_DECELERATE = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
val MATERIAL_MOTION_EASING_EMPHASIZED_ACCELERATE = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

@Stable
fun <T> materialEnterTransitionSpec(
    durationMillis: Int = MATERIAL_MOTION_DURATION_MEDIUM_4,
    delayMillis: Int = 0,
    easing: Easing = MATERIAL_MOTION_EASING_EMPHASIZED_DECELERATE
): TweenSpec<T> = TweenSpec(durationMillis, delayMillis, easing)

@Stable
fun <T> materialExitTransitionSpec(
    durationMillis: Int = MATERIAL_MOTION_DURATION_SHORT_4,
    delayMillis: Int = 0,
    easing: Easing = MATERIAL_MOTION_EASING_EMPHASIZED_ACCELERATE
): TweenSpec<T> = TweenSpec(durationMillis, delayMillis, easing)