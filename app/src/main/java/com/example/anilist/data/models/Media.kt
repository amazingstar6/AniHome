package com.example.anilist.data.models

data class Media(
    val id: Int = -1,
    val listEntryId: Int = -1,
    val type: MediaType = MediaType.UNKNOWN,
    val title: String = "?",
    val coverImage: String = "",
    val format: AniMediaFormat = AniMediaFormat.UNKNOWN,
    val season: Season = Season.UNKNOWN,
    val seasonYear: Int = -1,
    val episodeAmount: Int = -1,
    val averageScore: Int = -1,
    val genres: List<String> = emptyList(),
    val highestRated: String = "",
    val mostPopular: String = "",
    val description: String = "",
    val relations: List<Relation> = emptyList(),
    val infoList: MediaDetailInfoList = MediaDetailInfoList(),
    val tags: List<Tag> = emptyList(),
    val trailerImage: String = "",
    val trailerLink: String = "",
    val externalLinks: List<Link> = emptyList(),
    val personalRating: Double = (-1).toDouble(),
    var personalProgress: Int = -1,
    val isPrivate: Boolean = false,
    val note: String = "",
    val rewatches: Int = -1,
    val volumes: Int = -1,
    val personalVolumeProgress: Int = -1,
    val chapters: Int = -1,
    val characterWithVoiceActors: List<CharacterWithVoiceActor> = emptyList(),
    val stats: Stats = Stats(),
    val startDate: FuzzyDate? = null,
    val endDate: FuzzyDate? = null,

    val favourites: Int = -1,
    val isFavourite: Boolean = false,
    val isFavouriteBlocked: Boolean = false,

    val startedAt: FuzzyDate? = null,
    val completedAt: FuzzyDate? = null,
    val createdAt: FuzzyDate? = null,
    val personalStatus: PersonalMediaStatus = PersonalMediaStatus.UNKNOWN,

    val rawScore: Double = -1.0,
    val studios: List<AniStudio> = emptyList(),

    // epoch timestamp in seconds
    val updatedAt: Int = -1,

    //todo not used
    val priority: Int = -1
)

enum class MediaType {
    ANIME,
    MANGA,
    UNKNOWN,
}
