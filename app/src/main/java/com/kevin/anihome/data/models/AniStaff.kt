package com.kevin.anihome.data.models

data class AniStaff(
    val id: Int,
    val name: String = "",
    val role: String = "",
    val coverImage: String = "",
    val hasNextPage: Boolean = false,
)
