package com.example.anilist.data.models

import android.content.Context
import com.example.anilist.R

enum class HomeTrendingTypes {
    TRENDING_NOW,
    POPULAR_THIS_SEASON,
    UPCOMING_NEXT_SEASON,
    ALL_TIME_POPULAR,
    TOP_100_ANIME,
    POPULAR_MANHWA,
    ;

    fun toString(context: Context): String {
        return when (this) {
            TRENDING_NOW -> context.getString(R.string.trending_now)
            POPULAR_THIS_SEASON -> context.getString(R.string.popular_this_season)
            UPCOMING_NEXT_SEASON -> context.getString(R.string.upcoming_next_season)
            ALL_TIME_POPULAR -> context.getString(R.string.all_time_popular)
            TOP_100_ANIME -> context.getString(R.string.top_100_anime)
            POPULAR_MANHWA -> context.getString(R.string.popular_manhwa)
        }
    }
}
