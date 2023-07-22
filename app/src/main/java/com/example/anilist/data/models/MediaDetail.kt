package com.example.anilist.data.models

data class MediaDetail(
    val media: Media = Media(),
    val characters: List<Character> = emptyList(),
    val staff: List<Staff> = emptyList()
)
