package com.example.anilist.data

data class Anime(
    val title: String,
    val coverImage: String,
    val format: String,
    val seasonYear: String,
    val episodeAmount: Int,
    val averageScore: Int,
    val genres: List<String>,
    val highestRated: String = "",
    val mostPopular: String = "",
    val description: String,
    val relations: List<Relation>,
    val infoList: Map<String, String>,
    val tags: List<Tag>,
    val trailerImage: String,
    val trailerLink: String,
    val externalLinks: List<Link>
)
