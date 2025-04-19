package com.example.anilist.utils

import androidx.compose.ui.graphics.Color
import com.example.anilist.data.models.FuzzyDate
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import timber.log.Timber

class Utils {
    companion object {
        /**
         * Converts a timestamp in epoch seconds to a string in the format like "03-12-2012"
         */
        fun convertEpochToDateString(epochSeconds: Long): String {
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

        /**
         * Gets the relative time from now taken from an epoch timestamp in seconds.
         */
        fun getRelativeTimeFromNow(timestamp: Long): String {
//            val currentTime = System.currentTimeMillis() / 1000
            val currentTime = Clock.System.now().epochSeconds
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

        fun convertEpochToFuzzyDate(epochSeconds: Long): FuzzyDate {
            val time =
                Instant.fromEpochSeconds(
                    epochSeconds,
                ).toLocalDateTime(TimeZone.UTC)
            return FuzzyDate(
                time.year,
                time.monthNumber,
                time.dayOfMonth,
            )
        }

        fun Color.toHexString(): String {
            val red = (this.red * 255).toInt()
            val green = (this.green * 255).toInt()
            val blue = (this.blue * 255).toInt()
            return String.format("#%02X%02X%02X", red, green, blue)
        }

        fun currentYear(): Int {
            return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
        }

        fun nextYear(): Int {
            return currentYear() + 1
        }

        fun Int?.orMinusOne(): Int {
            return this ?: -1
        }

        fun getCurrentDay(): FuzzyDate {
            val date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            return FuzzyDate(year = date.year, month = date.monthNumber, day = date.dayOfMonth)
        }

        fun getCurrentDayMillisEpoch(): Long {
            return Clock.System.now().toEpochMilliseconds()
        }

        /**
         * Gets the relative time from current time + [timestamp] (seconds) in the future.
         */
        fun getRelativeTimeFuture(timestamp: Long): String {
//            val time = Instant.fromEpochSeconds(timestamp + Clock.System.now().epochSeconds)
//                .toLocalDateTime(
//                    TimeZone.currentSystemDefault()
//                )
//            val futureTime = Clock.System.now().epochSeconds + timestamp

            val seconds = timestamp
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24
            val weeks = days / 7
            val months = weeks / 4
            val years = months / 12

            return when {
                years > 0 -> "$years ${if (years == (1).toLong()) "year" else "years"}"
                months > 0 -> "$months ${if (months == (1).toLong()) "month" else "months"}"
//                weeks > 0 -> "$weeks ${if (weeks == (1).toLong()) "week" else "weeks"} ago"
                days > 0 -> "$days ${if (days == (1).toLong()) "day" else "days"}"
                hours > 0 -> "$hours ${if (hours == (1).toLong()) "hour" else "hours"}"
                minutes > 0 -> "$minutes ${if (minutes == (1).toLong()) "minute" else "minutes"}"
                else -> "a few seconds"
            }
        }

        /**
         *
         * @param timestamp in epoch seconds
         */
        fun convertEpochToDateTimeTimeZoneString(timestamp: Long): String {
            val time =
                Instant.fromEpochSeconds(
                    timestamp,
                ).toLocalDateTime(TimeZone.currentSystemDefault())
            Timber.d("Current timezone is ${TimeZone.currentSystemDefault().id}")
            return String.format(
                "%04d-%02d-%02d, %02d:%02d %s timezone",
                time.year,
                time.monthNumber,
                time.dayOfMonth,
                time.hour,
                time.minute,
                TimeZone.currentSystemDefault().toString(),
            )
        }
    }
}
