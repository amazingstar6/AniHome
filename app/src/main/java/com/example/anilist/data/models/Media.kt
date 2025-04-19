package com.example.anilist.data.models

import android.content.Context
import com.example.anilist.R

data class Media(
    val id: Int = -1,
    val type: AniMediaType = AniMediaType.UNKNOWN,
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
    val volumes: Int = -1,
    val chapters: Int = -1,
    val stats: AniStats = AniStats(),
    val startDate: FuzzyDate? = null,
    val endDate: FuzzyDate? = null,
    val favourites: Int = -1,
    val isFavourite: Boolean = false,
    val isFavouriteBlocked: Boolean = false,
    val studios: List<AniStudio> = emptyList(),
    // todo not used
    val priority: Int = -1,
    /**
     * Only used to know what languages there are for filtering the character's voice actors based on language
     */
    val languages: List<String> = emptyList(),
    val mediaListEntry: AniMediaListEntry = AniMediaListEntry(),
    val nextAiringEpisode: AniAiringSchedule = AniAiringSchedule(),
)

data class AniAiringSchedule(
    val id: Int = -1,
    val airingAt: Int = -1,
    /**
     * In seconds
     */
    val timeUntilAiring: Int = -1,
    val episode: Int = -1,
    val mediaId: Int = -1,
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
    val nsfw: Boolean? = false,
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
    val createdAt: FuzzyDate? = null,
)

enum class AniMediaType {
    ANIME,
    MANGA,
    UNKNOWN,
}

enum class AniSeason {
    UNKNOWN,
    SPRING,
    SUMMER,
    FALL,
    WINTER,
    ;

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
