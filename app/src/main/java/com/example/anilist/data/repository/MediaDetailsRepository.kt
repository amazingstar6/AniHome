package com.example.anilist.data.repository

import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import com.example.anilist.GetCharactersOfMediaQuery
import com.example.anilist.GetMediaDetailQuery
import com.example.anilist.GetReviewsOfMediaQuery
import com.example.anilist.GetStaffInfoQuery
import com.example.anilist.MainActivity
import com.example.anilist.ToggleFavoriteCharacterMutation
import com.example.anilist.data.models.AniMediaFormat
import com.example.anilist.data.models.AniResult
import com.example.anilist.data.models.AniStudio
import com.example.anilist.data.models.CharacterWithVoiceActor
import com.example.anilist.data.models.FuzzyDate
import com.example.anilist.data.models.Link
import com.example.anilist.data.models.Media
import com.example.anilist.data.models.MediaDetailInfoList
import com.example.anilist.data.models.PersonalMediaStatus
import com.example.anilist.data.models.Relation
import com.example.anilist.data.models.RelationTypes
import com.example.anilist.data.models.Review
import com.example.anilist.data.models.ReviewRatingStatus
import com.example.anilist.data.models.ScoreDistribution
import com.example.anilist.data.models.Season
import com.example.anilist.data.models.Staff
import com.example.anilist.data.models.Stats
import com.example.anilist.data.models.Status
import com.example.anilist.data.models.Tag
import com.example.anilist.data.repository.homerepository.toAni
import com.example.anilist.data.repository.mymedia.toAniStatus
import com.example.anilist.data.toAniHomeSeason
import com.example.anilist.data.toAniHomeType
import com.example.anilist.data.toAniRelation
import com.example.anilist.data.toAniRole
import com.example.anilist.type.MediaListStatus
import com.example.anilist.type.MediaRankType
import com.example.anilist.type.MediaType
import com.example.anilist.type.ReviewRating
import com.example.anilist.utils.Apollo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaDetailsRepository @Inject constructor() {

    suspend fun fetchMedia(
        mediaId: Int
    ): AniResult<Media> {
        try {
            val result =
                Apollo.apolloClient.query(
                    GetMediaDetailQuery(mediaId, MainActivity.userId),
                )
                    .execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
                return AniResult.Failure(buildString {
                    result.errors?.forEach { appendLine(it.message) }
                })
            }
            val data = result.dataOrThrow()
            return AniResult.Success(parseMedia(data))
        } catch (exception: ApolloException) {
            // fixme
            // an 404 not found gets thrown when a media does not have a media list entry
            // for the user, so we try the query again skipping the media list entry info
            // we could also convert them into two separate queries, but this one we'll have the chance
            // of doing only one query instead of always doing two
            try {
                val result =
                    Apollo.apolloClient.query(
                        GetMediaDetailQuery(
                            id = mediaId,
                            userId = MainActivity.userId,
                            skipMediaList = Optional.present(true)
                        ),
                    )
                        .execute()
                if (result.hasErrors()) {
                    return AniResult.Failure(buildString {
                        result.errors?.forEach { appendLine(it.message) }
                    })
                }
                val data = result.dataOrThrow()
                return AniResult.Success(parseMedia(data))
            } catch (e: ApolloException) {
                return AniResult.Failure(e.localizedMessage ?: "No exception message given")
            }
        }
    }

    suspend fun fetchStaffList(mediaId: Int, page: Int, pageSize: Int): AniResult<List<Staff>> {
        try {
            val result =
                Apollo.apolloClient.query(
                    GetStaffInfoQuery(id = mediaId, page = page, perPage = pageSize),
                )
                    .execute()
            if (result.hasErrors()) {
                return AniResult.Failure(buildString {
                    result.errors?.forEach { appendLine(it.message) }
                })
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

    suspend fun fetchCharacterList(
        mediaId: Int,
        page: Int,
        pageSize: Int
    ): AniResult<List<CharacterWithVoiceActor>> {
        try {
            val result =
                Apollo.apolloClient.query(
                    GetCharactersOfMediaQuery(id = mediaId, page = page, perPage = pageSize),
                )
                    .execute()
            if (result.hasErrors()) {
                return AniResult.Failure(buildString {
                    result.errors?.forEach { appendLine(it.message) }
                })
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


    suspend fun fetchReviews(mediaId: Int, page: Int, pageSize: Int): AniResult<List<Review>> {
        try {
            val result =
                Apollo.apolloClient.query(
                    GetReviewsOfMediaQuery(mediaId, page, pageSize),
                )
                    .execute()
            if (result.hasErrors()) {
                return AniResult.Failure(buildString {
                    result.errors?.forEach { appendLine(it.message) }
                })
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


    suspend fun toggleFavourite(type: LikeAbleType, id: Int): AniResult<Boolean> {
        try {
            val mutation: ToggleFavoriteCharacterMutation = when (type) {
                LikeAbleType.CHARACTER -> ToggleFavoriteCharacterMutation(
                    characterId = Optional.present(
                        id,
                    ),
                )

                LikeAbleType.STAFF -> ToggleFavoriteCharacterMutation(staffId = Optional.present(id))
                LikeAbleType.ANIME -> ToggleFavoriteCharacterMutation(animeId = Optional.present(id))
                LikeAbleType.MANGA -> ToggleFavoriteCharacterMutation(mangaId = Optional.present(id))
                LikeAbleType.STUDIO -> ToggleFavoriteCharacterMutation(
                    studioId = Optional.present(
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
                return AniResult.Failure(buildString {
                    result.errors?.forEach { appendLine(it.message) }
                })
            }
            // the result is a list of all the things you've already liked of the same type
            val isFavourite = when (type) {
                LikeAbleType.CHARACTER -> result.data?.ToggleFavourite?.characters?.nodes?.any { it?.id == id }
                LikeAbleType.STAFF -> result.data?.ToggleFavourite?.staff?.nodes?.any { it?.id == id }
                LikeAbleType.ANIME -> result.data?.ToggleFavourite?.anime?.nodes?.any { it?.id == id }
                LikeAbleType.MANGA -> result.data?.ToggleFavourite?.manga?.nodes?.any { it?.id == id }
                LikeAbleType.STUDIO -> result.data?.ToggleFavourite?.studios?.nodes?.any { it?.id == id }
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


    private fun parseStats(media: GetMediaDetailQuery.Media?): Stats {
        var highestRatedAllTime: Int = -1
        var highestRatedYearRank: Int = -1
        var highestRatedYearNumber: Int = -1
        var highestRatedSeasonRank: Int = -1
        var highestRatedSeasonSeason: Season = Season.UNKNOWN
        var highestRatedSeasonYear: Int = -1
        var mostPopularAllTime: Int = -1
        var mostPopularYearRank: Int = -1
        var mostPopularYearNumber: Int = -1
        var mostPopularSeasonRank: Int = -1
        var mostPopularSeasonSeason: Season = Season.UNKNOWN
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

        val statusDistribution: MutableMap<Status, Int> = mutableMapOf(
            Status.CURRENT to 0,
            Status.PLANNING to 0,
            Status.COMPLETED to 0,
            Status.DROPPED to 0,
            Status.PAUSED to 0,
        )
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

        for (status in media?.stats?.statusDistribution.orEmpty()) {
            statusDistribution[
                when (status?.status) {
                    MediaListStatus.CURRENT -> Status.CURRENT
                    MediaListStatus.COMPLETED -> Status.COMPLETED
                    MediaListStatus.PAUSED -> Status.PAUSED
                    MediaListStatus.PLANNING -> Status.PLANNING
                    MediaListStatus.DROPPED -> Status.DROPPED
                    // the query does not return any data for repeating
                    MediaListStatus.REPEATING -> Status.UNKNOWN
                    MediaListStatus.UNKNOWN__ -> Status.UNKNOWN
                    null -> Status.UNKNOWN
                },
            ] = status?.amount ?: 0
        }
        return Stats(
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
            scoreDistribution = ScoreDistribution(
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


    enum class LikeAbleType {
        CHARACTER,
        STAFF,
        ANIME,
        MANGA,
        STUDIO,
    }

    private fun parseReview(reviews: GetReviewsOfMediaQuery.Reviews?): List<Review> {
        val list = mutableListOf<Review>()
        for (review in reviews?.nodes.orEmpty()) {
            list.add(
                Review(
                    id = review?.id ?: -1,
                    title = review?.summary ?: "",
                    userName = review?.user?.name ?: "",
                    createdAt = review?.createdAt ?: -1,
                    body = review?.body ?: "",
                    score = review?.score ?: -1,
                    upvotes = review?.rating ?: -1,
                    totalVotes = review?.ratingAmount ?: -1,
                    userRating = when (review?.userRating) {
                        ReviewRating.NO_VOTE -> ReviewRatingStatus.NO_VOTE
                        ReviewRating.UP_VOTE -> ReviewRatingStatus.UP_VOTE
                        ReviewRating.DOWN_VOTE -> ReviewRatingStatus.DOWN_VOTE
                        else -> ReviewRatingStatus.NO_VOTE
                    },
                    userAvatar = review?.user?.avatar?.large ?: "",
                ),
            )
        }
        return list
    }

    private fun parseMedia(data: GetMediaDetailQuery.Data?): Media {
        val media = data?.Media
        val tags: MutableList<Tag> = mutableListOf()
        for (tag in media?.tags.orEmpty()) {
            if (tag != null) {
                tags.add(Tag(tag.name, tag.rank ?: 0, tag.isMediaSpoiler ?: true))
            }
        }
        buildString {
            for (synonym in media?.synonyms.orEmpty()) {
                append(synonym)
                if (media?.synonyms?.last() != synonym) {
                    append("\n")
                }
            }
        }
        val genres: MutableList<String> = mutableListOf()
        for (genre in media?.genres.orEmpty()) {
            if (genre != null) {
                genres.add(genre)
            }
        }
        val externalLinks: MutableList<Link> = mutableListOf()
        for (link in media?.externalLinks.orEmpty()) {
            if (link != null) {
                externalLinks.add(
                    Link(
                        link.url ?: "",
                        link.site,
                        link.language ?: "",
                        link.color ?: "",
                        link.icon ?: "",
                    ),
                )
            }
        }
        val relations: MutableList<Relation> = mutableListOf()
        for (relation in media?.relations?.edges.orEmpty()) {
            relations.add(
                Relation(
                    id = relation?.node?.id ?: 0,
                    coverImage = relation?.node?.coverImage?.extraLarge ?: "",
                    title = relation?.node?.title?.native ?: "",
                    relation = relation?.relationType?.toAniRelation() ?: RelationTypes.UNKNOWN,
                ),
            )
        }
        return Media(
            id = media?.id ?: -1,
            listEntryId = data?.MediaList?.id ?: -1,
            type = media?.type?.toAniHomeType()
                ?: com.example.anilist.data.models.MediaType.UNKNOWN,
            title = media?.title?.native ?: "Unknown",
            coverImage = media?.coverImage?.extraLarge ?: "",
            format = media?.format?.toAni() ?: AniMediaFormat.UNKNOWN,
            season = media?.season?.toAniHomeSeason() ?: Season.UNKNOWN,
            seasonYear = media?.seasonYear ?: -1,
            episodeAmount = media?.episodes ?: -1,
            averageScore = media?.averageScore ?: 0,
            genres = genres,
            description = media?.description ?: "No description",
            relations = relations,
            infoList = MediaDetailInfoList(
                format = media?.format?.name.orEmpty(),
                status = media?.status?.name.orEmpty(),
                startDate = if (media?.startDate != null) "${
                    media.startDate.day?.toString()?.padStart(2, '0') ?: "?"
                }-${
                    media.startDate.month?.toString()?.padStart(2, '0') ?: "?"
                }-${media.startDate.year?.toString()?.padStart(2, '0') ?: "?"}"
                else "Unknown",
                endDate = if (media?.endDate != null) "${
                    media.endDate.day?.toString()?.padStart(2, '0') ?: "?"
                }-${
                    media.endDate.month?.toString()?.padStart(2, '0') ?: "?"
                }-${media.endDate.year?.toString()?.padStart(2, '0') ?: "?"}"
                else "Unknown",
                duration = media?.duration ?: -1,
                country = media?.countryOfOrigin.toString(),
                source = media?.source?.name.orEmpty(),
                hashtag = media?.hashtag.orEmpty(),
                licensed = media?.isLicensed,
                updatedAt = media?.updatedAt.toString(),
                synonyms = media?.synonyms?.filterNotNull().orEmpty(),
                nsfw = media?.isAdult
            ),
            tags = tags,
            trailerImage = media?.trailer?.thumbnail ?: "",
            trailerLink =
            if (media?.trailer?.site == "youtube") "https://www.youtube.com/watch?v=${media.trailer.id}" else if (media?.trailer?.site == "dailymotion") "" else "",
            // todo add dailymotion
            externalLinks = externalLinks,
            personalProgress = data?.MediaList?.progress ?: -1,
            isPrivate = data?.MediaList?.private ?: false,
//            characterWithVoiceActors = parseCharacters(media),
            languages = data?.Media?.characters?.edges?.let {
                val resultList = mutableListOf<String>()
                it.forEach {edge ->
                    edge?.voiceActorRoles?.forEach {voiceActorRole ->
                        voiceActorRole?.voiceActor?.languageV2?.let {language ->
                            resultList.add(language)
                        }
                    }
                }
                resultList.distinct()
            }.orEmpty(),
            note = data?.MediaList?.notes ?: "",
            rewatches = data?.MediaList?.repeat ?: -1,
            volumes = media?.volumes ?: -1,
            personalVolumeProgress = data?.MediaList?.progressVolumes ?: -1,
            chapters = media?.chapters ?: -1,
            stats = parseStats(media),
            favourites = media?.favourites ?: -1,
            isFavourite = media?.isFavourite ?: false,
            isFavouriteBlocked = media?.isFavouriteBlocked ?: false,
            startedAt = getFuzzyDate(data?.MediaList?.startedAt?.fuzzyDate),
            completedAt = getFuzzyDate(data?.MediaList?.completedAt?.fuzzyDate),
            personalStatus = data?.MediaList?.status?.toAniStatus() ?: PersonalMediaStatus.UNKNOWN,
            rawScore = data?.MediaList?.score ?: -1.0,
            studios = data?.Media?.studios?.nodes?.filterNotNull()
                ?.map { AniStudio(id = it.id, name = it.name) }.orEmpty(),
        )
    }

    private fun getFuzzyDate(
        fuzzyDate: com.example.anilist.fragment.FuzzyDate?
    ) =
        if (fuzzyDate?.year != null && fuzzyDate.month != null && fuzzyDate.day != null) {
            FuzzyDate(
                fuzzyDate.year,
                fuzzyDate.month,
                fuzzyDate.day,
            )
        } else {
            null
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
                                voiceActorId = voiceActor.voiceActor?.id ?: -1,
                                name = character.node?.name?.native ?: "",
                                coverImage = character.node?.image?.large ?: "",
                                voiceActorName = voiceActor.voiceActor?.name?.userPreferred ?: "",
                                voiceActorCoverImage = voiceActor.voiceActor?.image?.large ?: "",
                                voiceActorLanguage = voiceActor.voiceActor?.languageV2 ?: "",
                                role = character.role.toAniRole(),
                                roleNotes = voiceActor.roleNotes.orEmpty()
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
                        name = character?.node?.name?.native ?: "",
                        coverImage = character?.node?.image?.large ?: "",
                        role = character?.role.toAniRole()
                    )
                )
            }
        }
        return characterWithVoiceActors
    }

    private fun parseStaffList(media: GetStaffInfoQuery.Media?): List<Staff> {
        val list = mutableListOf<Staff>()
        for (staff in media?.staff?.edges.orEmpty()) {
            list.add(
                Staff(
                    id = staff?.node?.id ?: -1,
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