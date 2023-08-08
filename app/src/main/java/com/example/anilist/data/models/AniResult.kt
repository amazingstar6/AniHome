package com.example.anilist.data.models

sealed class AniResult<out T: Any> {
    data class Success<out T: Any>(val data: T) : AniResult<T>()
    data class Failure(val error: String) : AniResult<Nothing>()
}
