package com.example.anilist.data.repository

import android.util.Log
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import com.example.anilist.GetCharacterDetailQuery
import com.example.anilist.GetMediaDetailQuery
import com.example.anilist.GetReviewDetailQuery
import com.example.anilist.GetReviewsOfMediaQuery
import com.example.anilist.GetStaffDetailQuery
import com.example.anilist.GetStaffInfoQuery
import com.example.anilist.MainActivity
import com.example.anilist.RateReviewMutation
import com.example.anilist.ToggleFavoriteCharacterMutation
import com.example.anilist.data.models.AniCharacterRole
import com.example.anilist.data.models.AniMediaFormat
import com.example.anilist.data.models.AniStudio
import com.example.anilist.data.models.CharacterDetail
import com.example.anilist.data.models.CharacterMediaConnection
import com.example.anilist.data.models.CharacterWithVoiceActor
import com.example.anilist.data.models.FuzzyDate
import com.example.anilist.data.models.Link
import com.example.anilist.data.models.Media
import com.example.anilist.data.models.MediaDetailInfoList
import com.example.anilist.data.models.Relation
import com.example.anilist.data.models.RelationTypes
import com.example.anilist.data.models.Review
import com.example.anilist.data.models.ReviewRatingStatus
import com.example.anilist.data.models.ScoreDistribution
import com.example.anilist.data.models.Season
import com.example.anilist.data.models.Staff
import com.example.anilist.data.models.StaffDetail
import com.example.anilist.data.models.Stats
import com.example.anilist.data.models.Status
import com.example.anilist.data.models.Tag
import com.example.anilist.data.repository.mymedia.toAniStatus
import com.example.anilist.fragment.StaffMedia
import com.example.anilist.type.CharacterRole
import com.example.anilist.type.MediaListStatus
import com.example.anilist.type.MediaRankType
import com.example.anilist.type.MediaRelation
import com.example.anilist.type.MediaSeason
import com.example.anilist.type.MediaType
import com.example.anilist.type.ReviewRating
import com.example.anilist.data.models.PersonalMediaStatus
import com.example.anilist.utils.Apollo
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "MediaDetailRepository"

@Singleton
class MediaDetailsRepository @Inject constructor() {

    suspend fun fetchMedia(
        mediaId: Int
    ): Result<Media> {
        try {
            Timber.d("user id found in main activity is " + MainActivity.userId)
            Timber.d("media id being used is $mediaId")
            val result =
                Apollo.apolloClient.query(
                    GetMediaDetailQuery(mediaId, MainActivity.userId),
                )
                    .execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
            }
            val data = result.dataOrThrow()
            return Result.success(parseMedia(data))
        } catch (exception: ApolloException) {
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
                    // these errors are related to GraphQL errors
                }
                val data = result.dataOrThrow()
                return Result.success(parseMedia(data))
            } catch (e: ApolloException) {
                Log.d(TAG, "Exception: ${exception.message}\n$exception.")
                return Result.failure(exception)
            }
        }
    }

//    suspend fun fetchCharacters(mediaId: Int): List<CharacterWithVoiceActor> {
//        try {
//            val result =
//                Apollo.apolloClient.query(
//                    GetMediaDetailQuery(mediaId),
//                )
//                    .execute()
//            if (result.hasErrors()) {
//                // these errors are related to GraphQL errors
//            }
//            val data = result.data?.Media
//            if (data != null) {
//                return parseCharacters(data)
//            }
//        } catch (exception: ApolloException) {
//            // handle exception here,, these are mainly for network errors
//        }
//        return emptyList()
//    }

    suspend fun fetchStaffList(mediaId: Int, page: Int, pageSize: Int): List<Staff> {
        try {
            val result =
                Apollo.apolloClient.query(
                    GetStaffInfoQuery(id = mediaId, page = page, perPage = pageSize),
                )
                    .execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
            }
            val data = result.data?.Media
            if (data != null) {
                return parseStaffList(data)
            }
        } catch (exception: ApolloException) {
            // handle exception here,, these are mainly for network errors
        }
        return emptyList()
    }

    suspend fun fetchStaffDetail(id: Int): StaffDetail {
        try {
            val result =
                Apollo.apolloClient.query(
                    GetStaffDetailQuery(id),
                )
                    .execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
            }
            val data = result.data?.Staff
            if (data != null) {
                return parseStaff(data)
            }
        } catch (exception: ApolloException) {
            // handle exception here,, these are mainly for network errors
        }
        return StaffDetail()
    }

    private fun parseStaff(staff: GetStaffDetailQuery.Staff): StaffDetail {
        return StaffDetail(
            id = staff.id,
            coverImage = staff.image?.large ?: "",
            userPreferredName = staff.name?.userPreferred ?: "",
            alternativeNames = staff.name?.alternative?.filterNotNull().orEmpty(),
            favourites = staff.favourites ?: -1,
            language = staff.languageV2 ?: "",
            description = staff.description ?: "",
            isFavourite = staff.isFavourite,
            isFavouriteBlocked = staff.isFavouriteBlocked,
            voicedCharacters = parseVoicedCharactersForStaff(staff.characters),
            animeStaffRole = parseMediaForStaff(staff.anime?.staffMedia),
            mangaStaffRole = parseMediaForStaff(staff.manga?.staffMedia),
        )
    }

    /**
     * We need to extract coverImage, title, characterRole and id into the object
     */
    private fun parseMediaForStaff(staffMedia: StaffMedia?): List<CharacterMediaConnection> {
        val result = mutableListOf<CharacterMediaConnection>()
        for (media in staffMedia?.edges.orEmpty()) {
            if (media != null) {
                result.add(
                    CharacterMediaConnection(
                        id = media.node?.id ?: -1,
                        title = media.node?.title?.userPreferred ?: "",
                        characterRole = media.staffRole ?: "",
                        coverImage = media.node?.coverImage?.extraLarge ?: "",
                    ),
                )
            }
        }
        return result
    }

    /**
     * We need coverImage, name, role and id in Character object
     */
    private fun parseVoicedCharactersForStaff(characters: GetStaffDetailQuery.Characters?): List<CharacterWithVoiceActor> {
        val result = mutableListOf<CharacterWithVoiceActor>()
        for (character in characters?.edges.orEmpty()) {
            result.add(
                CharacterWithVoiceActor(
                    id = character?.node?.id ?: -1,
                    name = character?.node?.name?.userPreferred ?: "",
                    role = character?.role?.toAniRole() ?: AniCharacterRole.UNKNOWN,
                    coverImage = character?.node?.image?.large ?: "",
                ),
            )
        }
        return result
    }

    suspend fun fetchReviews(mediaId: Int, page: Int, pageSize: Int): List<Review> {
        try {
            val result =
                Apollo.apolloClient.query(
                    GetReviewsOfMediaQuery(mediaId, page, pageSize),
                )
                    .execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
            }
            val data = result.data?.Media?.reviews
            if (data != null) {
                return parseReview(data)
            }
        } catch (exception: ApolloException) {
            // handle exception here,, these are mainly for network errors
        }
        return emptyList()
    }

//    suspend fun fetchStats(mediaId: Int): Stats {
//        try {
//            val result =
//                Apollo.apolloClient.query(
//                    GetStatsQuery(mediaId)
//                )
//                    .execute()
//            if (result.hasErrors()) {
//                // these errors are related to GraphQL errors
//            }
//            val data = result.data?.Media
//            if (data != null) {
//                return parseStats(data)
//            }
//        } catch (exception: ApolloException) {
//            // handle exception here,, these are mainly for network errors
//        }
//        return Stats()
//    }

    suspend fun fetchCharacter(characterId: Int): CharacterDetail {
        try {
            val result =
                Apollo.apolloClient.query(
                    GetCharacterDetailQuery(characterId),
                )
                    .execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
            }
            val data = result.data?.Character
            if (data != null) {
                return parseCharacter(data)
            }
        } catch (exception: ApolloException) {
            // handle exception here,, these are mainly for network errors
        }
        return CharacterDetail()
    }

    private fun parseMediaCharacter(mediaList: GetCharacterDetailQuery.Media?): List<CharacterMediaConnection> {
        val result = mutableListOf<CharacterMediaConnection>()
        for (media in mediaList?.edges.orEmpty()) {
            result.add(
                CharacterMediaConnection(
                    id = media?.node?.id ?: -1,
                    title = media?.node?.title?.userPreferred ?: "",
                    coverImage = media?.node?.coverImage?.extraLarge ?: "",
                    characterRole = media?.characterRole?.name ?: "",
                ),
            )
        }
        return result
    }

    private fun parseVoiceActorsForCharacter(mediaList: GetCharacterDetailQuery.Media?): List<StaffDetail> {
        val result = mutableListOf<StaffDetail>()
        for (media in mediaList?.edges.orEmpty()) {
            for (voiceActor in media?.voiceActorRoles.orEmpty()) {
                result.add(
                    StaffDetail(
                        id = voiceActor?.voiceActor?.id ?: -1,
                        userPreferredName = voiceActor?.voiceActor?.name?.userPreferred ?: "",
                        coverImage = voiceActor?.voiceActor?.image?.large ?: "",
                        language = voiceActor?.voiceActor?.languageV2 ?: "",
                    ),
                )
            }
        }
        return result.distinctBy { it.id }
    }

    private fun parseCharacter(data: GetCharacterDetailQuery.Character): CharacterDetail {
        val regex = Regex("<span class='markdown_spoiler'>(.*?)</span>")
        val matches = regex.findAll(data.description ?: "")


//        Html.fromHtml(data.description, Html.FROM_HTML_MODE_COMPACT, null, )
        val description = buildString {
            if (!(data.dateOfBirth?.fuzzyDate?.year == null && data.dateOfBirth?.fuzzyDate?.month == null && data.dateOfBirth?.fuzzyDate?.day == null)) {
                append(
                    "<div><strong>Birthday:</strong>${data.dateOfBirth.fuzzyDate.year ?: "?"}-${data.dateOfBirth.fuzzyDate.month ?: "?"}-${data.dateOfBirth.fuzzyDate.day ?: "?"}</div>",
                )
            }
            if (data.age != null) append("<div><strong>Age:</strong>${data.age}</div>")
            if (data.gender != null) append("<div><strong>Gender:</strong>${data.gender}</div>")
            if (data.bloodType != null) append("<div><strong>Blood type:</strong>${data.bloodType}</div>")
            if (data.description != null) {
                append(
                    data.description
                        .substringAfter("<p>")
                        .substringBeforeLast("</p>"),
                )
            }
        }
        return CharacterDetail(
            id = data.id,
            userPreferredName = data.name?.userPreferred ?: "",
            firstName = data.name?.first ?: "",
            middleName = data.name?.middle ?: "",
            lastName = data.name?.last ?: "",
            fullName = data.name?.full ?: "",
            nativeName = data.name?.native ?: "",
            coverImage = data.image?.large ?: "",
            // we take the outer p element out of the description, because otherwise there will be a margin between the blood type and description
            description = description,
            isFavourite = data.isFavourite,
            isFavoriteBlocked = data.isFavouriteBlocked,
            favorites = data.favourites ?: -1,
            voiceActors = parseVoiceActorsForCharacter(data.media),
            relatedMedia = parseMediaCharacter(data.media),
            alternativeNames = data.name?.alternative?.filterNotNull().orEmpty(),
            alternativeSpoilerNames = data.name?.alternativeSpoiler?.filterNotNull().orEmpty(),
        )
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

    suspend fun fetchReview(reviewId: Int): Review {
        try {
            val result =
                Apollo.apolloClient.query(
                    GetReviewDetailQuery(reviewId),
                )
                    .execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
            }
            val review = result.data?.Review
            if (review != null) {
                return parseReview(review)
            }
        } catch (exception: ApolloException) {
            // handle exception here,, these are mainly for network errors
        }
        return Review()
    }

    private fun parseReview(review: GetReviewDetailQuery.Review?): Review {
        return Review(
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
        )
    }

    enum class LikeAbleType {
        CHARACTER,
        STAFF,
        ANIME,
        MANGA,
        STUDIO,
    }

    suspend fun toggleFavourite(type: LikeAbleType, id: Int): Boolean {
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
                // these errors are related to GraphQL errors
            }
            // the result is a list of all the things you've already liked of the same type
            val isFavourite = when (type) {
                LikeAbleType.CHARACTER -> result.data?.ToggleFavourite?.characters?.nodes?.any { it?.id == id }
                LikeAbleType.STAFF -> result.data?.ToggleFavourite?.staff?.nodes?.any { it?.id == id }
                LikeAbleType.ANIME -> result.data?.ToggleFavourite?.anime?.nodes?.any { it?.id == id }
                LikeAbleType.MANGA -> result.data?.ToggleFavourite?.manga?.nodes?.any { it?.id == id }
                LikeAbleType.STUDIO -> result.data?.ToggleFavourite?.studios?.nodes?.any { it?.id == id }
            }
            return isFavourite ?: false
        } catch (exception: ApolloException) {
            // handle exception here,, these are mainly for network errors
        }
        return false
    }

    suspend fun rateReview(id: Int, rating: ReviewRatingStatus): Boolean {
        try {
            val apiRating = when (rating) {
                ReviewRatingStatus.NO_VOTE -> ReviewRating.NO_VOTE
                ReviewRatingStatus.UP_VOTE -> ReviewRating.UP_VOTE
                ReviewRatingStatus.DOWN_VOTE -> ReviewRating.DOWN_VOTE
            }
            val result =
                Apollo.apolloClient.mutation(
                    RateReviewMutation(
                        rating = apiRating,
                        id = id
                    ),
                )
                    .execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
            }
            // the result is a list of all the things you've already liked of the same type
            return result.data?.RateReview?.userRating == apiRating
        } catch (exception: ApolloException) {
            // handle exception here,, these are mainly for network errors
        }
        return false
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
        val synonyms = buildString {
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
            title = media?.title?.native ?: "Unknown",
            type = media?.type?.toAniHomeType()
                ?: com.example.anilist.data.models.MediaType.UNKNOWN,
            coverImage = media?.coverImage?.extraLarge ?: "",
            format = media?.format?.toAni() ?: AniMediaFormat.UNKNOWN,
            season = media?.season?.toAniHomeSeason() ?: Season.UNKNOWN,
            seasonYear = media?.seasonYear ?: -1,
            episodeAmount = media?.episodes ?: -1,
            volumes = media?.volumes ?: -1,
            chapters = media?.chapters ?: -1,
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
            )
//            mapOf(
//                "format" to media?.format?.name.orEmpty(),
//                "status" to media?.status?.name?.lowercase()
//                    ?.replaceFirstChar {
//                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
//                    }
//                    .orEmpty(),
//                "startDate" to if (media?.startDate != null) "${media.startDate.day}-${media.startDate.month}-${media.startDate.year}" else "Unknown",
//                "endDate" to if (media?.endDate?.year != null && media.endDate.month != null && media.endDate.day != null) "${media.endDate.day}-${media.endDate.month}-${media.endDate.year}" else "Unknown",
//                "duration" to if (media?.duration == null) "Unknown" else media.duration.toString(),
//                "country" to media?.countryOfOrigin.toString(),
//                "source" to (media?.source?.rawValue ?: "Unknown"),
//                "hashtag" to (media?.hashtag ?: "Unknown"),
//                "licensed" to media?.isLicensed.toString(),
//                "updatedAt" to media?.updatedAt.toString(),
//                "synonyms" to synonyms,
//                "nsfw" to media?.isAdult.toString(),
//            )
            ,
            tags = tags,
            trailerImage = media?.trailer?.thumbnail ?: "",
            // todo add dailymotion
            trailerLink =
            if (media?.trailer?.site == "youtube") "https://www.youtube.com/watch?v=${media.trailer.id}" else if (media?.trailer?.site == "dailymotion") "" else "",
            externalLinks = externalLinks,
            stats = parseStats(media),
            characterWithVoiceActors = parseCharacters(media),
            favourites = media?.favourites ?: -1,
            isFavourite = media?.isFavourite ?: false,
            isFavouriteBlocked = media?.isFavouriteBlocked ?: false,
            personalStatus = data?.MediaList?.status?.toAniStatus() ?: PersonalMediaStatus.UNKNOWN,
            studios = data?.Media?.studios?.nodes?.filterNotNull()
                ?.map { AniStudio(id = it.id, name = it.name) }.orEmpty(),
            personalProgress = data?.MediaList?.progress ?: -1,
            personalVolumeProgress = data?.MediaList?.progressVolumes ?: -1,
            rewatches = data?.MediaList?.repeat ?: -1,
            rawScore = data?.MediaList?.score ?: -1.0,
            startedAt = getFuzzyDate(data?.MediaList?.startedAt?.fuzzyDate),
            completedAt = getFuzzyDate(data?.MediaList?.completedAt?.fuzzyDate),
            isPrivate = data?.MediaList?.private ?: false,
            note = data?.MediaList?.notes ?: "",
            listEntryId = data?.MediaList?.id ?: -1
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

    private fun parseCharacters(anime: GetMediaDetailQuery.Media?): List<CharacterWithVoiceActor> {
        val characterWithVoiceActors: MutableList<CharacterWithVoiceActor> = mutableListOf()
        if (anime?.type == MediaType.ANIME) {
            val languages: MutableList<String> = mutableListOf()
            for (character in anime?.characters?.edges.orEmpty()) {
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
                                role = character.role.toAniRole()
                            ),
                        )
                    }
                }
            }
        } else if (anime?.type == MediaType.MANGA) {
            for (character in anime.characters?.edges.orEmpty()) {
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

private fun CharacterRole?.toAniRole(): AniCharacterRole {
    return when (this) {
        CharacterRole.MAIN -> AniCharacterRole.MAIN
        CharacterRole.SUPPORTING -> AniCharacterRole.SUPPORTING
        CharacterRole.BACKGROUND -> AniCharacterRole.BACKGROUND
        CharacterRole.UNKNOWN__ -> AniCharacterRole.UNKNOWN
        null -> AniCharacterRole.UNKNOWN
    }
}

private fun MediaRelation?.toAniRelation(): RelationTypes {
    return when (this) {
        MediaRelation.ADAPTATION -> RelationTypes.ADAPTION
        MediaRelation.PREQUEL -> RelationTypes.PREQUEL
        MediaRelation.SEQUEL -> RelationTypes.SEQUEL
        MediaRelation.PARENT -> RelationTypes.PARENT
        MediaRelation.SIDE_STORY -> RelationTypes.SIDE_STORY
        MediaRelation.CHARACTER -> RelationTypes.CHARACTER
        MediaRelation.SUMMARY -> RelationTypes.SUMMARY
        MediaRelation.ALTERNATIVE -> RelationTypes.ALTERNATIVE
        MediaRelation.SPIN_OFF -> RelationTypes.SPIN_OFF
        MediaRelation.OTHER -> RelationTypes.OTHER
        MediaRelation.SOURCE -> RelationTypes.SOURCE
        MediaRelation.COMPILATION -> RelationTypes.COMPILATION
        MediaRelation.CONTAINS -> RelationTypes.CONTAINS
        MediaRelation.UNKNOWN__ -> RelationTypes.UNKNOWN
        null -> RelationTypes.UNKNOWN
    }
}

fun MediaSeason.toAniHomeSeason(): Season {
    return when (this) {
        MediaSeason.SPRING -> Season.SPRING
        MediaSeason.SUMMER -> Season.SUMMER
        MediaSeason.FALL -> Season.FALL
        MediaSeason.WINTER -> Season.WINTER
        MediaSeason.UNKNOWN__ -> Season.UNKNOWN
    }
}

fun MediaType.toAniHomeType(): com.example.anilist.data.models.MediaType {
    return when (this) {
        MediaType.MANGA -> com.example.anilist.data.models.MediaType.MANGA
        MediaType.ANIME -> com.example.anilist.data.models.MediaType.ANIME
        MediaType.UNKNOWN__ -> com.example.anilist.data.models.MediaType.UNKNOWN
    }
}