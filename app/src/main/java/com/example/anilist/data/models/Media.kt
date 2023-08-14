package com.example.anilist.data.models

import android.content.Context
import com.example.anilist.R

data class Media(
    val id: Int = -1,
//    val listEntryId: Int = -1,
    val type: MediaType = MediaType.UNKNOWN,
    val title: String = "?",
    val coverImage: String = "",
    val format: AniMediaFormat = AniMediaFormat.UNKNOWN,
    val season: AniSeason = AniSeason.UNKNOWN,
    val seasonYear: Int = -1,
    val episodeAmount: Int = -1,
    val averageScore: Int = -1,
    val genres: List<String> = emptyList(),
    val highestRated: String = "",
    val mostPopular: String = "",
    val description: String = "",
    val relations: List<AniMediaRelation> = emptyList(),
    val infoList: MediaDetailInfoList = MediaDetailInfoList(),
    val tags: List<Tag> = emptyList(),
    val trailerImage: String = "",
    val trailerLink: String = "",
    val externalLinks: List<AniLink> = emptyList(),
//    val personalRating: Double = (-1).toDouble(),
//    var personalProgress: Int = -1,
//    val isPrivate: Boolean = false,
//    val note: String = "",
//    val rewatches: Int = -1,
    val volumes: Int = -1,
//    val personalVolumeProgress: Int = -1,
    val chapters: Int = -1,
    //todo unused
    val characterWithVoiceActors: List<CharacterWithVoiceActor> = emptyList(),
    val stats: AniStats = AniStats(),
    val startDate: FuzzyDate? = null,
    val endDate: FuzzyDate? = null,

    val favourites: Int = -1,
    val isFavourite: Boolean = false,
    val isFavouriteBlocked: Boolean = false,

//    val startedAt: FuzzyDate? = null,
//    val completedAt: FuzzyDate? = null,
//    val createdAt: FuzzyDate? = null,
//    val personalStatus: PersonalMediaStatus = PersonalMediaStatus.UNKNOWN,

//    val rawScore: Double = -1.0,
    val studios: List<AniStudio> = emptyList(),
    // epoch timestamp in seconds
//    val updatedAt: Int = -1,
    //todo not used
    val priority: Int = -1,
    val languages: List<String> = emptyList(),
    //todo
    val mediaListEntry: AniMediaListEntry = AniMediaListEntry()
)

data class MediaDetailInfoList(
    val format: String = "",
    val status: AniMediaStatus = AniMediaStatus.UNKNOWN,
    val duration: Int = -1,
    val country: String = "",
    val source: String = "",
    val hashtag: String = "",
    val licensed: Boolean? = false,
    val updatedAt: String = "",
    val synonyms: List<String> = emptyList(),
    val nsfw: Boolean? = false
)

data class AniMediaListEntry(
    val listEntryId: Int = -1,
    val userId: Int = -1,
    val mediaId: Int = -1,
    val status: AniPersonalMediaStatus = AniPersonalMediaStatus.UNKNOWN,
    val score: Double = 0.0,
    val progress: Int = 0,
    val progressVolumes: Int = 0,
    val repeat: Int = 0,
    val private: Boolean = false,
    val notes: String = "",
    val hiddenFromStatusLists: Boolean = false,
    val customLists: String = "", // actually json
    val advancedScores: String = "", // actually json
    val startedAt: FuzzyDate? = null,
    val completedAt: FuzzyDate? = null,
    val updatedAt: FuzzyDate? = null,
    val createdAt: FuzzyDate? = null
)

enum class MediaType {
    ANIME,
    MANGA,
    UNKNOWN,
}

enum class AniSeason {
    UNKNOWN,
    SPRING,
    SUMMER,
    FALL,
    WINTER;

    fun getString(context: Context): String {
        return when (this) {
            UNKNOWN -> context.getString(R.string.unknown)
            SPRING -> context.getString(R.string.spring)
            SUMMER -> context.getString(R.string.summer)
            FALL -> context.getString(R.string.fall)
            WINTER -> context.getString(R.string.winter)
        }
    }
}