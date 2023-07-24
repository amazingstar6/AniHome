package com.example.anilist.data.models

data class Staff(
    val id: Int,
    val name: String = "",
    val role: String = "",
    val coverImage: String = "",
    val hasNextPage: Boolean = false
)
