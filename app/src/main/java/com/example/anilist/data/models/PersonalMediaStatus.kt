package com.example.anilist.data.models

enum class PersonalMediaStatus {
    CURRENT,
    PLANNING,
    COMPLETED,
    REPEATING,
    PAUSED,
    DROPPED,
    UNKNOWN, ;

    fun getString(isAnime: Boolean): String {
        return when (this) {
            CURRENT -> if (isAnime) "Watching" else "Reading"
            PLANNING -> if (isAnime) "Plan to watch" else "Plan to read"
            COMPLETED -> "Completed"
            DROPPED -> "Dropped"
            PAUSED -> "Paused"
            REPEATING -> if (isAnime) "Rewatching" else "Rereading"
            UNKNOWN -> "None"
        }
    }
}