package com.example.anilist.data.models

data class Anime(
    val id: Int = -1,
    val title: String = "?",
    val coverImage: String = "",
    val format: String = "?",
    val seasonYear: String = "?",
    val episodeAmount: Int = -1,
    val averageScore: Int = -1,
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
    // todo fill these
    val personalRating: Double = (-1).toDouble(),
    val personalEpisodeProgress: Int = -1,
)
