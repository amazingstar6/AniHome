package com.example.anilist.data.models

import android.content.Context
import com.example.anilist.R

enum class AniPersonalMediaStatus {
    UNKNOWN,
    CURRENT,
    REPEATING,
    COMPLETED,
    PAUSED,
    DROPPED,
    PLANNING;

    fun toString(
        context: Context,
        isAnime: Boolean,
        unknownString: String = context.getString(R.string.all)
    ): String {
        return when (this) {
            CURRENT -> if (isAnime) context.getString(R.string.watching) else context.getString(R.string.reading)
            PLANNING -> if (isAnime) "Plan to watch" else "Plan to read"
            COMPLETED -> context.getString(R.string.completed)
            DROPPED -> context.getString(R.string.dropped)
            PAUSED -> context.getString(R.string.paused)
            REPEATING -> if (isAnime) context.getString(R.string.rewatching) else context.getString(
                R.string.rereading
            )

            UNKNOWN -> unknownString
        }
    }

    fun getIconResource(): Int {
        return when (this) {
            CURRENT -> R.drawable.outline_play_circle_24
            PLANNING -> R.drawable.outline_task_alt_24
            COMPLETED -> R.drawable.outline_check_circle_24
            REPEATING -> R.drawable.outline_replay_24
            PAUSED -> R.drawable.outline_pause_circle_24
            DROPPED -> R.drawable.outline_cancel_24
            UNKNOWN -> R.drawable.my_media_filter
        }
    }
}