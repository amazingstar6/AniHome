package com.example.anilist.data.models

import android.content.Context
import com.example.anilist.R

enum class AniMediaFormat {
    TV,
    TV_SHORT,
    MOVIE,
    SPECIAL,
    OVA,
    ONA,
    MUSIC,
    MANGA,
    NOVEL,
    ONE_SHOT,
    UNKNOWN,
    ;

    fun toString(context: Context): String {
        return when (this) {
            TV -> context.getString(R.string.tv)
            TV_SHORT -> context.getString(R.string.tv_short)
            MOVIE -> context.getString(R.string.movie)
            SPECIAL -> context.getString(R.string.special)
            OVA -> context.getString(R.string.ova)
            ONA -> context.getString(R.string.ona)
            MUSIC -> context.getString(R.string.music)
            MANGA -> context.getString(R.string.manga)
            NOVEL -> context.getString(R.string.novel)
            ONE_SHOT -> context.getString(R.string.one_shot)
            UNKNOWN -> context.getString(R.string.unknown)
        }
    }
}
