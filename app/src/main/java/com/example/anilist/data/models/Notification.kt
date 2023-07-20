package com.example.anilist.data.models

data class Notification(
    val type: String = "",
    val context: String = "",
    val createdAt: Int = -1
)
