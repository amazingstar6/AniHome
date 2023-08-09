package com.example.anilist.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.paging.PagingState
import kotlinx.datetime.Clock
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

//        fun convertToRFC3339(secondsToAdd: Long): String {
//            val currentTime = ZonedDateTime.now().plusSeconds(secondsToAdd)
//            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
//            return currentTime.format(formatter)
//        }

        fun Color.toHexString(): String {
            val red = (this.red * 255).toInt()
            val green = (this.green * 255).toInt()
            val blue = (this.blue * 255).toInt()
            return String.format("#%02X%02X%02X", red, green, blue)
        }

        fun currentYear(): Int {
            return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
        }
    }
}
