package com.example.anilist.data.models

import android.content.Context
import com.example.anilist.R

data class Stats(
    val ranksIsNotEmpty: Boolean = true,
    val highestRatedAllTime: Int = -1,
    val highestRatedYearRank: Int = -1,
    val highestRatedYearNumber: Int = -1,
    val highestRatedSeasonRank: Int = -1,
    val highestRatedSeasonSeason: Season = Season.UNKNOWN,
    val highestRatedSeasonYear: Int = -1,
    val mostPopularAllTime: Int = -1,
    val mostPopularYearRank: Int = -1,
    val mostPopularYearNumber: Int = -1,
    val mostPopularSeasonRank: Int = -1,
    val mostPopularSeasonSeason: Season = Season.UNKNOWN,
    val mostPopularSeasonYear: Int = -1,
    val scoreDistribution: ScoreDistribution = ScoreDistribution(),
    val statusDistribution: Map<Status, Int> = mapOf(
        Status.CURRENT to 0,
        Status.PLANNING to 0,
        Status.COMPLETED to 0,
        Status.DROPPED to 0,
        Status.PAUSED to 0,
        Status.UNKNOWN to 0,
    ),
)

data class ScoreDistribution(
    val ten: Int = 0,
    val twenty: Int = 0,
    val thirty: Int = 0,
    val forty: Int = 0,
    val fifty: Int = 0,
    val sixty: Int = 0,
    val seventy: Int = 0,
    val eighty: Int = 0,
    val ninety: Int = 0,
    val hundred: Int = 0,
)

enum class Status {
    CURRENT,
    PLANNING,
    COMPLETED,
    DROPPED,
    PAUSED,
    UNKNOWN,
}

enum class Season {
    UNKNOWN,
    SPRING,
    SUMMER,
    FALL,
    WINTER;

    fun getString(context: Context): String {
        return when (this) {
            UNKNOWN -> context.getString(R.string.unknown)
            SPRING -> context.getString(R.string.spring)
            SUMMER -> context.getString(R.string.summer)
            FALL -> context.getString(R.string.fall)
            WINTER -> context.getString(R.string.winter)
        }
    }
}
