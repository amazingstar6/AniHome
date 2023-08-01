package com.example.anilist.utils

import androidx.paging.PagingState
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class Utils {
    companion object {
        fun convertEpochToString(epochSeconds: Long): String {
            val time =
                Instant.fromEpochSeconds(
                    epochSeconds,
                ).toLocalDateTime(TimeZone.UTC)
            return String.format(
                "%04d-%02d-%02d",
                time.year,
                time.monthNumber,
                time.dayOfMonth,
            )
        }

        fun getRelativeTime(timestamp: Long): String {
            val currentTime = System.currentTimeMillis() / 1000
            val elapsedTime = currentTime - timestamp

            val seconds = elapsedTime
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24
            val weeks = days / 7
            val months = weeks / 4
            val years = months / 12

            return when {
                years > 0 -> "$years ${if (years == (1).toLong()) "year" else "years"} ago"
                months > 0 -> "$months ${if (months == (1).toLong()) "month" else "months"} ago"
                weeks > 0 -> "$weeks ${if (weeks == (1).toLong()) "week" else "weeks"} ago"
                days > 0 -> "$days ${if (days == (1).toLong()) "day" else "days"} ago"
                hours > 0 -> "$hours ${if (hours == (1).toLong()) "hour" else "hours"} ago"
                minutes > 0 -> "$minutes ${if (minutes == (1).toLong()) "minute" else "minutes"} ago"
                else -> "Just now"
            }
        }

        fun getRefreshKey(state: PagingState<Int, Any>): Int? {
            return state.anchorPosition?.let { anchorPosition ->
                // anchor position is the last index that successfully fetched data
                val anchorPage = state.closestPageToPosition(anchorPosition)
                anchorPage?.prevKey ?: anchorPage?.nextKey
            }
        }

        fun convertToRFC3339(secondsToAdd: Long): String {
            val currentTime = ZonedDateTime.now().plusSeconds(secondsToAdd)
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
            return currentTime.format(formatter)
        }
    }
}
