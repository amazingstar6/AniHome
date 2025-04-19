package com.kevin.anihome.data.models

import android.content.Context
import com.kevin.anihome.R

enum class AniMediaStatus {
    FINISHED,
    RELEASING,
    NOT_YET_RELEASED,
    CANCELLED,
    HIATUS,
    UNKNOWN,
    ;

    fun toString(context: Context): String {
        return when (this) {
            FINISHED -> context.getString(R.string.finished)
            RELEASING -> context.getString(R.string.releasing)
            NOT_YET_RELEASED -> context.getString(R.string.not_yet_released)
            CANCELLED -> context.getString(R.string.cancelled)
            HIATUS -> context.getString(R.string.hiatus)
            UNKNOWN -> context.getString(R.string.unknown)
        }
    }
}
