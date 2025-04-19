package com.example.anilist.data.repository.mediadetail

import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.exception.ApolloException
import com.example.anilist.GetCharactersOfMediaQuery
import com.example.anilist.GetMediaDetailQuery
import com.example.anilist.GetReviewsOfMediaQuery
import com.example.anilist.GetStaffInfoQuery
import com.example.anilist.ToggleFavoriteCharacterMutation
import com.example.anilist.data.models.AniAiringSchedule
import com.example.anilist.data.models.AniLikeAbleType
import com.example.anilist.data.models.AniLink
import com.example.anilist.data.models.AniLinkType
import com.example.anilist.data.models.AniMediaFormat
import com.example.anilist.data.models.AniMediaListEntry
import com.example.anilist.data.models.AniMediaRelation
import com.example.anilist.data.models.AniMediaRelationTypes
import com.example.anilist.data.models.AniMediaStatus
import com.example.anilist.data.models.AniPersonalMediaStatus
import com.example.anilist.data.models.AniResult
import com.example.anilist.data.models.AniReview
import com.example.anilist.data.models.AniScoreDistribution
import com.example.anilist.data.models.AniSeason
import com.example.anilist.data.models.AniStaff
import com.example.anilist.data.models.AniStats
import com.example.anilist.data.models.AniStatsStatusDistribution
import com.example.anilist.data.models.AniStudio
import com.example.anilist.data.models.CharacterWithVoiceActor
import com.example.anilist.data.models.FuzzyDate
import com.example.anilist.data.models.Media
import com.example.anilist.data.models.MediaDetailInfoList
import com.example.anilist.data.models.Tag
import com.example.anilist.data.repository.homerepository.toAni
import com.example.anilist.data.repository.mymedia.toAniStatus
import com.example.anilist.data.toAni
import com.example.anilist.data.toAniHomeSeason
import com.example.anilist.data.toAniHomeType
import com.example.anilist.data.toAniRelation
import com.example.anilist.data.toAniRole
import com.example.anilist.fragment.MediaDetailFragment
import com.example.anilist.type.ExternalLinkType
import com.example.anilist.type.MediaListStatus
import com.example.anilist.type.MediaRankType
import com.example.anilist.type.MediaStatus
import com.example.anilist.type.MediaType
import com.example.anilist.utils.Apollo
import com.example.anilist.utils.Utils
import com.example.anilist.utils.Utils.Companion.orMinusOne
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaDetailsRepositoryImpl
    @Inject
    constructor() : MediaDetailsRepository {
        override suspend fun fetchMedia(mediaId: Int): AniResult<Media> {
            try {
                val result =
                    Apollo.apolloClient.newBuilder().fetchPolicy(FetchPolicy.NetworkFirst).build()
                        .query(
                            GetMediaDetailQuery(mediaId),
                        )
                        .execute()
                if (result.hasErrors()) {
                    // these errors are related to GraphQL errors
                    return AniResult.Failure(
                        buildString {
                            result.errors?.forEach { appendLine(it.message) }
                        },
                    )
                }
                val data = result.dataOrThrow()
                return AniResult.Success(parseMediaDetailFragment(data.Media?.mediaDetailFragment))
            } catch (exception: ApolloException) {
                return AniResult.Failure(exception.localizedMessage ?: "No exception message given")
            }
        }

        override suspend fun fetchStaffList(
            mediaId: Int,
            page: Int,
            pageSize: Int,
        ): AniResult<List<AniStaff>> {
            try {
                val result =
                    Apollo.apolloClient.query(
                        GetStaffInfoQuery(id = mediaId, page = page, perPage = pageSize),
                    )
                        .execute()
                if (result.hasErrors()) {
                    return AniResult.Failure(
                        buildString {
                            result.errors?.forEach { appendLine(it.message) }
                        },
                    )
                }
                val data = result.data?.Media
                return if (data != null) {
                    AniResult.Success(parseStaffList(data))
                } else {
                    AniResult.Failure("Network error")
                }
            } catch (exception: ApolloException) {
                return AniResult.Failure(exception.localizedMessage ?: "No exception message given")
            }
        }

        override suspend fun fetchCharacterList(
            mediaId: Int,
            page: Int,
            pageSize: Int,
        ): AniResult<List<CharacterWithVoiceActor>> {
            try {
                val result =
                    Apollo.apolloClient.query(
                        GetCharactersOfMediaQuery(id = mediaId, page = page, perPage = pageSize),
                    )
                        .execute()
                if (result.hasErrors()) {
                    return AniResult.Failure(
                        buildString {
                            result.errors?.forEach { appendLine(it.message) }
                        },
                    )
                }
                val data = result.data?.Media
                return if (data != null) {
                    AniResult.Success(parseCharacters(data))
                } else {
                    AniResult.Failure("Network error")
                }
            } catch (exception: ApolloException) {
                return AniResult.Failure(exception.localizedMessage ?: "No exception message given")
            }
        }

        override suspend fun fetchReviews(
            mediaId: Int,
            page: Int,
            pageSize: Int,
        ): AniResult<List<AniReview>> {
            try {
                val result =
                    Apollo.apolloClient.newBuilder().fetchPolicy(FetchPolicy.NetworkFirst).build()
                        .query(
                            GetReviewsOfMediaQuery(mediaId, page, pageSize),
                        )
                        .execute()
                if (result.hasErrors()) {
                    return AniResult.Failure(
                        buildString {
                            result.errors?.forEach { appendLine(it.message) }
                        },
                    )
                }
                val data = result.data?.Media?.reviews
                return if (data != null) {
                    AniResult.Success(parseReview(data))
                } else {
                    AniResult.Failure("Network error")
                }
            } catch (exception: ApolloException) {
                return AniResult.Failure(exception.localizedMessage ?: "No exception message given")
            }
        }

        override suspend fun toggleFavourite(
            type: AniLikeAbleType,
            id: Int,
        ): AniResult<Boolean> {
            try {
                val mutation: ToggleFavoriteCharacterMutation =
                    when (type) {
                        AniLikeAbleType.CHARACTER ->
                            ToggleFavoriteCharacterMutation(
                                characterId =
                                    Optional.present(
                                        id,
                                    ),
                            )

                        AniLikeAbleType.STAFF ->
                            ToggleFavoriteCharacterMutation(
                                staffId =
                                    Optional.present(
                                        id,
                                    ),
                            )

                        AniLikeAbleType.ANIME ->
                            ToggleFavoriteCharacterMutation(
                                animeId =
                                    Optional.present(
                                        id,
                                    ),
                            )

                        AniLikeAbleType.MANGA ->
                            ToggleFavoriteCharacterMutation(
                                mangaId =
                                    Optional.present(
                                        id,
                                    ),
                            )

                        AniLikeAbleType.STUDIO ->
                            ToggleFavoriteCharacterMutation(
                                studioId =
                                    Optional.present(
                                        id,
                                    ),
                            )
                    }
                val result =
                    Apollo.apolloClient.mutation(
                        mutation,
                    )
                        .execute()
                if (result.hasErrors()) {
                    return AniResult.Failure(
                        buildString {
                            result.errors?.forEach { appendLine(it.message) }
                        },
                    )
                }
                // the result is a list of all the things you've already liked of the same type
                val isFavourite =
                    when (type) {
                        AniLikeAbleType.CHARACTER -> result.data?.ToggleFavourite?.characters?.nodes?.any { it?.id == id }
                        AniLikeAbleType.STAFF -> result.data?.ToggleFavourite?.staff?.nodes?.any { it?.id == id }
                        AniLikeAbleType.ANIME -> result.data?.ToggleFavourite?.anime?.nodes?.any { it?.id == id }
                        AniLikeAbleType.MANGA -> result.data?.ToggleFavourite?.manga?.nodes?.any { it?.id == id }
                        AniLikeAbleType.STUDIO -> result.data?.ToggleFavourite?.studios?.nodes?.any { it?.id == id }
                    }
                return if (isFavourite != null) {
                    AniResult.Success(isFavourite)
                } else {
                    AniResult.Failure("Network error")
                }
            } catch (exception: ApolloException) {
                return AniResult.Failure(exception.localizedMessage ?: "No exception message given")
            }
        }

        private fun parseReview(reviews: GetReviewsOfMediaQuery.Reviews?): List<AniReview> {
            val list = mutableListOf<AniReview>()
            for (review in reviews?.nodes.orEmpty()) {
                Timber.d("Received review ${review?.summary} with rating ${review?.userRating}")
                list.add(
                    AniReview(
                        id = review?.id.orMinusOne(),
                        title = review?.summary.orEmpty(),
                        userName = review?.user?.name.orEmpty(),
                        createdAt = review?.createdAt.orMinusOne(),
                        body = review?.body.orEmpty(),
                        score = review?.score.orMinusOne(),
                        upvotes = review?.rating.orMinusOne(),
                        totalVotes = review?.ratingAmount.orMinusOne(),
                        userRating = review?.userRating.toAni(),
                        userAvatar = review?.user?.avatar?.large.orEmpty(),
                    ),
                )
            }
            return list
        }

        private fun parseCharacters(media: GetCharactersOfMediaQuery.Media?): List<CharacterWithVoiceActor> {
            val characterWithVoiceActors: MutableList<CharacterWithVoiceActor> = mutableListOf()
            if (media?.type == MediaType.ANIME) {
                val languages: MutableList<String> = mutableListOf()
                for (character in media.characters?.edges.orEmpty()) {
                    for (voiceActor in character?.voiceActorRoles.orEmpty()) {
                        if (languages.contains(voiceActor?.voiceActor?.languageV2) &&
                            voiceActor?.voiceActor?.languageV2 != null
                        ) {
                            languages.add(voiceActor.voiceActor.languageV2)
                        }
                        if (character != null && voiceActor != null) {
                            characterWithVoiceActors.add(
                                CharacterWithVoiceActor(
                                    id = character.node?.id ?: 0,
                                    voiceActorId = voiceActor.voiceActor?.id.orMinusOne(),
                                    name = character.node?.name?.native.orEmpty(),
                                    coverImage = character.node?.image?.large.orEmpty(),
                                    voiceActorName = voiceActor.voiceActor?.name?.userPreferred.orEmpty(),
                                    voiceActorCoverImage = voiceActor.voiceActor?.image?.large.orEmpty(),
                                    voiceActorLanguage = voiceActor.voiceActor?.languageV2.orEmpty(),
                                    role = character.role.toAniRole(),
                                    roleNotes = voiceActor.roleNotes.orEmpty(),
                                ),
                            )
                        }
                    }
                }
            } else if (media?.type == MediaType.MANGA) {
                for (character in media.characters?.edges.orEmpty()) {
                    characterWithVoiceActors.add(
                        CharacterWithVoiceActor(
                            id = character?.node?.id ?: 0,
                            name = character?.node?.name?.native.orEmpty(),
                            coverImage = character?.node?.image?.large.orEmpty(),
                            role = character?.role.toAniRole(),
                        ),
                    )
                }
            }
            return characterWithVoiceActors
        }

        private fun parseStaffList(media: GetStaffInfoQuery.Media?): List<AniStaff> {
            val list = mutableListOf<AniStaff>()
            for (staff in media?.staff?.edges.orEmpty()) {
                list.add(
                    AniStaff(
                        id = staff?.node?.id.orMinusOne(),
                        name = staff?.node?.name?.userPreferred ?: "Unknown",
                        role = staff?.role ?: "Unknown",
                        coverImage = staff?.node?.image?.large ?: "Unknown",
                        hasNextPage = media?.staff?.pageInfo?.hasNextPage ?: false,
                    ),
                )
            }
            return list
        }
    }

fun ExternalLinkType?.toAni(): AniLinkType {
    return when (this) {
        ExternalLinkType.INFO -> AniLinkType.INFO
        ExternalLinkType.STREAMING -> AniLinkType.STREAMING
        ExternalLinkType.SOCIAL -> AniLinkType.SOCIAL
        ExternalLinkType.UNKNOWN__ -> AniLinkType.UNKNOWN
        null -> AniLinkType.UNKNOWN
    }
}

fun MediaStatus?.toAni(): AniMediaStatus {
    return when (this) {
        MediaStatus.FINISHED -> AniMediaStatus.FINISHED
        MediaStatus.RELEASING -> AniMediaStatus.RELEASING
        MediaStatus.NOT_YET_RELEASED -> AniMediaStatus.NOT_YET_RELEASED
        MediaStatus.CANCELLED -> AniMediaStatus.CANCELLED
        MediaStatus.HIATUS -> AniMediaStatus.HIATUS
        MediaStatus.UNKNOWN__ -> AniMediaStatus.UNKNOWN
        null -> AniMediaStatus.UNKNOWN
    }
}

fun getFuzzyDate(fuzzyDate: com.example.anilist.fragment.FuzzyDate?) =
    if (fuzzyDate?.year != null && fuzzyDate.month != null && fuzzyDate.day != null) {
        FuzzyDate(
            fuzzyDate.year,
            fuzzyDate.month,
            fuzzyDate.day,
        )
    } else {
        null
    }

fun parseMediaDetailFragment(data: MediaDetailFragment?): Media {
    val tags: MutableList<Tag> = mutableListOf()
    for (tag in data?.tags.orEmpty()) {
        if (tag != null) {
            tags.add(
                Tag(
                    tag.name,
                    tag.rank ?: 0,
                    tag.isMediaSpoiler ?: true,
                    description = tag.description.orEmpty(),
                ),
            )
        }
    }
    buildString {
        for (synonym in data?.synonyms.orEmpty()) {
            append(synonym)
            if (data?.synonyms?.last() != synonym) {
                append("\n")
            }
        }
    }
    val genres: MutableList<String> = mutableListOf()
    for (genre in data?.genres.orEmpty()) {
        if (genre != null) {
            genres.add(genre)
        }
    }
    val externalLinks: MutableList<AniLink> = mutableListOf()
    for (link in data?.externalLinks.orEmpty()) {
        if (link != null) {
            externalLinks.add(
                AniLink(
                    link.url.orEmpty(),
                    link.site,
                    link.language.orEmpty(),
                    link.color.orEmpty(),
                    link.icon.orEmpty(),
                    link.type.toAni(),
                ),
            )
        }
    }
    val relations: MutableList<AniMediaRelation> = mutableListOf()
    for (relation in data?.relations?.edges.orEmpty()) {
        relations.add(
            AniMediaRelation(
                id = relation?.node?.id ?: 0,
                coverImage = relation?.node?.coverImage?.extraLarge.orEmpty(),
                title = relation?.node?.title?.native.orEmpty(),
                relation = relation?.relationType?.toAniRelation() ?: AniMediaRelationTypes.UNKNOWN,
            ),
        )
    }
    return Media(
        id = data?.id.orMinusOne(),
        type =
            data?.type?.toAniHomeType()
                ?: com.example.anilist.data.models.AniMediaType.UNKNOWN,
        title = data?.title?.native ?: "Unknown",
        coverImage = data?.coverImage?.extraLarge.orEmpty(),
        format = data?.format?.toAni() ?: AniMediaFormat.UNKNOWN,
        season = data?.season?.toAniHomeSeason() ?: AniSeason.UNKNOWN,
        seasonYear = data?.seasonYear.orMinusOne(),
        episodeAmount = data?.episodes.orMinusOne(),
        averageScore = data?.averageScore ?: 0,
        genres = genres,
        description = data?.description ?: "No description",
        relations = relations,
        infoList =
            MediaDetailInfoList(
                format = data?.format?.name.orEmpty(),
                status = data?.status?.toAni() ?: AniMediaStatus.UNKNOWN,
                duration = data?.duration.orMinusOne(),
                country = data?.countryOfOrigin.toString(),
                source = data?.source?.name.orEmpty(),
                hashtag = data?.hashtag.orEmpty(),
                licensed = data?.isLicensed,
                updatedAt = data?.updatedAt.toString(),
                synonyms = data?.synonyms?.filterNotNull().orEmpty(),
                nsfw = data?.isAdult,
            ),
        tags = tags,
        trailerImage = data?.trailer?.thumbnail.orEmpty(),
        // todo add dailymotion
        trailerLink =
            when (data?.trailer?.site) {
                "youtube" -> {
                    "https://www.youtube.com/watch?v=${data.trailer.id}"
                }

                "dailymotion" -> {
                    "https://www.dailymotion.com/video/${data.trailer.id}"
                }

                else -> {
                    ""
                }
            },
        externalLinks = externalLinks,
        languages =
            data?.characters?.edges?.let {
                val resultList = mutableListOf<String>()
                it.forEach { edge ->
                    edge?.voiceActorRoles?.forEach { voiceActorRole ->
                        voiceActorRole?.voiceActor?.languageV2?.let { language ->
                            resultList.add(language)
                        }
                    }
                }
                resultList.distinct()
            }.orEmpty(),
        volumes = data?.volumes.orMinusOne(),
        chapters = data?.chapters.orMinusOne(),
        stats = parseStats(data),
        favourites = data?.favourites.orMinusOne(),
        isFavourite = data?.isFavourite ?: false,
        isFavouriteBlocked = data?.isFavouriteBlocked ?: false,
        studios =
            data?.studios?.nodes?.filterNotNull()
                ?.map { AniStudio(id = it.id, name = it.name) }.orEmpty(),
        mediaListEntry =
            if (data?.mediaListEntry?.id == null) {
                AniMediaListEntry()
            } else {
                parseMediaListEntry(
                    data.mediaListEntry,
                )
            },
        startDate = getFuzzyDate(data?.startDate?.fuzzyDate),
        endDate = getFuzzyDate(data?.endDate?.fuzzyDate),
        nextAiringEpisode =
            AniAiringSchedule(
                id = data?.nextAiringEpisode?.id ?: -1,
                airingAt = data?.nextAiringEpisode?.airingAt ?: -1,
                timeUntilAiring = data?.nextAiringEpisode?.timeUntilAiring ?: -1,
                episode = data?.nextAiringEpisode?.episode ?: -1,
                mediaId = data?.nextAiringEpisode?.mediaId ?: -1,
            ),
    )
}

fun parseStats(media: MediaDetailFragment?): AniStats {
    var highestRatedAllTime: Int = -1
    var highestRatedYearRank: Int = -1
    var highestRatedYearNumber: Int = -1
    var highestRatedSeasonRank: Int = -1
    var highestRatedSeasonSeason: AniSeason = AniSeason.UNKNOWN
    var highestRatedSeasonYear: Int = -1
    var mostPopularAllTime: Int = -1
    var mostPopularYearRank: Int = -1
    var mostPopularYearNumber: Int = -1
    var mostPopularSeasonRank: Int = -1
    var mostPopularSeasonSeason: AniSeason = AniSeason.UNKNOWN
    var mostPopularSeasonYear: Int = -1

    var ten = 0
    var twenty = 0
    var thirty = 0
    var forty = 0
    var fifty = 0
    var sixty = 0
    var seventy = 0
    var eighty = 0
    var ninety = 0
    var hundred = 0

    for (rank in media?.rankings.orEmpty()) {
        if (rank?.type == MediaRankType.RATED && rank.allTime == true) {
            highestRatedAllTime = rank.rank
        }
        if (rank?.type == MediaRankType.RATED && rank.year != null) {
            highestRatedYearRank = rank.rank
            highestRatedYearNumber = rank.year
        }
        if (rank?.type == MediaRankType.RATED && rank.season != null && rank.year != null) {
            highestRatedSeasonRank = rank.rank
            highestRatedSeasonSeason = rank.season.toAniHomeSeason()
            highestRatedSeasonYear = rank.year
        }

        if (rank?.type == MediaRankType.POPULAR && rank.allTime == true) {
            mostPopularAllTime = rank.rank
        }
        if (rank?.type == MediaRankType.POPULAR && rank.year != null) {
            mostPopularYearRank = rank.rank
            mostPopularYearNumber = rank.year
        }
        if (rank?.type == MediaRankType.POPULAR && rank.season != null && rank.year != null) {
            mostPopularSeasonRank = rank.rank
            mostPopularSeasonSeason = rank.season.toAniHomeSeason()
            mostPopularSeasonYear = rank.year
        }
    }
    for (stat in media?.stats?.scoreDistribution.orEmpty()) {
        if (stat?.score == 10) {
            ten = stat.amount ?: 0
        }
        if (stat?.score == 20) {
            twenty = stat.amount ?: 0
        }
        if (stat?.score == 30) {
            thirty = stat.amount ?: 0
        }
        if (stat?.score == 40) {
            forty = stat.amount ?: 0
        }
        if (stat?.score == 50) {
            fifty = stat.amount ?: 0
        }
        if (stat?.score == 60) {
            sixty = stat.amount ?: 0
        }
        if (stat?.score == 70) {
            seventy = stat.amount ?: 0
        }
        if (stat?.score == 80) {
            eighty = stat.amount ?: 0
        }
        if (stat?.score == 90) {
            ninety = stat.amount ?: 0
        }
        if (stat?.score == 100) {
            hundred = stat.amount ?: 0
        }
    }

    val statusDistribution =
        AniStatsStatusDistribution(
            current =
                media?.stats?.statusDistribution?.find { it?.status == MediaListStatus.CURRENT }?.amount
                    ?: 0,
            planning =
                media?.stats?.statusDistribution?.find { it?.status == MediaListStatus.PLANNING }?.amount
                    ?: 0,
            completed =
                media?.stats?.statusDistribution?.find { it?.status == MediaListStatus.COMPLETED }?.amount
                    ?: 0,
            dropped =
                media?.stats?.statusDistribution?.find { it?.status == MediaListStatus.DROPPED }?.amount
                    ?: 0,
            paused =
                media?.stats?.statusDistribution?.find { it?.status == MediaListStatus.PAUSED }?.amount
                    ?: 0,
        )
//    for (status in media?.stats?.statusDistribution.orEmpty()) {
//        statusDistribution[
//            when (status?.status) {
//                MediaListStatus.CURRENT -> Status.CURRENT
//                MediaListStatus.COMPLETED -> Status.COMPLETED
//                MediaListStatus.PAUSED -> Status.PAUSED
//                MediaListStatus.PLANNING -> Status.PLANNING
//                MediaListStatus.DROPPED -> Status.DROPPED
//                // the query does not return any data for repeating
//                MediaListStatus.REPEATING -> Status.UNKNOWN
//                MediaListStatus.UNKNOWN__ -> Status.UNKNOWN
//                null -> Status.UNKNOWN
//            },
//        ] = status?.amount ?: 0
//    }
    return AniStats(
        ranksIsNotEmpty = media?.rankings?.isNotEmpty() ?: true,
        highestRatedAllTime = highestRatedAllTime,
        highestRatedYearRank = highestRatedYearRank,
        highestRatedYearNumber = highestRatedYearNumber,
        highestRatedSeasonRank = highestRatedSeasonRank,
        highestRatedSeasonSeason = highestRatedSeasonSeason,
        highestRatedSeasonYear = highestRatedSeasonYear,
        mostPopularAllTime = mostPopularAllTime,
        mostPopularYearRank = mostPopularYearRank,
        mostPopularYearNumber = mostPopularYearNumber,
        mostPopularSeasonRank = mostPopularSeasonRank,
        mostPopularSeasonSeason = mostPopularSeasonSeason,
        mostPopularSeasonYear = mostPopularSeasonYear,
        scoreDistribution =
            AniScoreDistribution(
                ten,
                twenty,
                thirty,
                forty,
                fifty,
                sixty,
                seventy,
                eighty,
                ninety,
                hundred,
            ),
        statusDistribution = statusDistribution,
    )
}

private fun parseMediaListEntry(listEntry: MediaDetailFragment.MediaListEntry): AniMediaListEntry {
    return AniMediaListEntry(
        listEntryId = listEntry.id,
        userId = listEntry.userId,
        mediaId = listEntry.mediaId,
        status = listEntry.status?.toAniStatus() ?: AniPersonalMediaStatus.UNKNOWN,
        score = listEntry.score ?: -1.0,
        progress = listEntry.progress.orMinusOne(),
        progressVolumes = listEntry.progressVolumes.orMinusOne(),
        repeat = listEntry.repeat.orMinusOne(),
        private = listEntry.private ?: false,
        notes = listEntry.notes.orEmpty(),
        hiddenFromStatusLists = listEntry.hiddenFromStatusLists ?: false,
        customLists = listEntry.customLists.toString(),
        advancedScores = listEntry.advancedScores.toString(),
        startedAt = getFuzzyDate(listEntry.startedAt?.fuzzyDate),
        completedAt = getFuzzyDate(listEntry.completedAt?.fuzzyDate),
        updatedAt = listEntry.updatedAt?.toLong()?.let { Utils.convertEpochToFuzzyDate(it) },
    )
}
