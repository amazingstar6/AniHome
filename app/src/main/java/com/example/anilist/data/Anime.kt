package com.example.anilist.data

data class Anime(
    val title: String = "?",
    val coverImage: String = "",
    val format: String = "?",
    val seasonYear: String = "?",
    val episodeAmount: Int = 0,
    val averageScore: Int = 0,
    val genres: List<String> = emptyList(),
    val highestRated: String = "",
    val mostPopular: String = "",
    val description: String = "",
    val relations: List<Relation> = emptyList(),
    val infoList: Map<String, String> = emptyMap(),
    val tags: List<Tag> = emptyList(),
    val trailerImage: String = "",
    val trailerLink: String = "",
    val externalLinks: List<Link> = emptyList(),
)
