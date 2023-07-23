package com.example.anilist.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import com.example.anilist.Apollo
import com.example.anilist.GetMediaDetailQuery
import com.example.anilist.GetReviewDetailQuery
import com.example.anilist.GetReviewsOfMediaQuery
import com.example.anilist.GetStaffInfoQuery
import com.example.anilist.GetStatsQuery
import com.example.anilist.data.models.Character
import com.example.anilist.data.models.Link
import com.example.anilist.data.models.Media
import com.example.anilist.data.models.Relation
import com.example.anilist.data.models.Review
import com.example.anilist.data.models.ReviewRatingStatus
import com.example.anilist.data.models.ScoreDistribution
import com.example.anilist.data.models.Season
import com.example.anilist.data.models.Staff
import com.example.anilist.data.models.Stats
import com.example.anilist.data.models.Status
import com.example.anilist.data.models.Tag
import com.example.anilist.type.MediaRankType
import com.example.anilist.type.MediaSeason
import com.example.anilist.type.ReviewRating
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class MediaDetailsRepository @Inject constructor() {

    suspend fun fetchMedia(mediaId: Int): Media {
        try {
            val result =
                Apollo.apolloClient.query(
                    GetMediaDetailQuery(mediaId)
                )
                    .execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
            }
            val data = result.data?.Media
            if (data != null) {
                return parseMedia(data)
            }
        } catch (exception: ApolloException) {
            // handle exception here,, these are mainly for network errors
        }
        return Media()
    }

    suspend fun fetchCharacters(mediaId: Int): List<Character> {
        try {
            val result =
                Apollo.apolloClient.query(
                    GetMediaDetailQuery(mediaId)
                )
                    .execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
            }
            val data = result.data?.Media
            if (data != null) {
                return parseCharacters(data)
            }
        } catch (exception: ApolloException) {
            // handle exception here,, these are mainly for network errors
        }
        return emptyList()
    }

    suspend fun fetchStaff(mediaId: Int, page: Int): List<Staff> {
        try {
            val result =
                Apollo.apolloClient.query(
                    GetStaffInfoQuery(mediaId, Optional.present(page))
                )
                    .execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
            }
            val data = result.data?.Media
            if (data != null) {
                return parseStaff(data)
            }
        } catch (exception: ApolloException) {
            // handle exception here,, these are mainly for network errors
        }
        return emptyList()
    }

    suspend fun fetchReviews(mediaId: Int): List<Review> {
        try {
            val result =
                Apollo.apolloClient.query(
                    GetReviewsOfMediaQuery(mediaId)
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

    suspend fun fetchStats(mediaId: Int): Stats? {
        try {
            val result =
                Apollo.apolloClient.query(
                    GetStatsQuery(mediaId)
                )
                    .execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
            }
            val data = result.data?.Media
            if (data != null) {
                return parseStats(data)
            }
        } catch (exception: ApolloException) {
            // handle exception here,, these are mainly for network errors
        }
        return Stats()
    }

    private fun parseStats(media: GetStatsQuery.Media): Stats {
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
        val scoreDistribution: ScoreDistribution = ScoreDistribution()
        val statusDistribution: Map<Status, Int> = mapOf(
            Status.CURRENT to 0,
            Status.PLANNING to 0,
            Status.COMPLETED to 0,
            Status.DROPPED to 0,
            Status.PAUSED to 0
        )
        for (rank in media.rankings.orEmpty()) {
            if (rank?.type == MediaRankType.RATED && rank?.allTime == true) {
                highestRatedAllTime = rank.rank
            }
            if (rank?.type == MediaRankType.RATED && rank.year != null) {
                highestRatedYearRank = rank.rank
                highestRatedYearNumber = rank.year
            }
            if (rank?.type == MediaRankType.RATED && rank.season != null && rank.year != null) {
                highestRatedSeasonRank = rank.rank
                highestRatedSeasonSeason = when (rank.season) {
                    MediaSeason.SPRING -> Season.SPRING
                    MediaSeason.SUMMER -> Season.SUMMER
                    MediaSeason.FALL -> Season.FALL
                    MediaSeason.WINTER -> Season.WINTER
                    MediaSeason.UNKNOWN__ -> Season.UNKNOWN
                }
                highestRatedSeasonYear = rank.year
            }

            if (rank?.type == MediaRankType.POPULAR && rank?.allTime == true) {
                mostPopularAllTime = rank.rank
            }
            if (rank?.type == MediaRankType.POPULAR && rank.year != null) {
                mostPopularYearRank = rank.rank
                mostPopularYearNumber = rank.year
            }
            if (rank?.type == MediaRankType.POPULAR && rank.season != null && rank.year != null) {
                mostPopularSeasonRank = rank.rank
                mostPopularSeasonSeason = when (rank.season) {
                    MediaSeason.SPRING -> Season.SPRING
                    MediaSeason.SUMMER -> Season.SUMMER
                    MediaSeason.FALL -> Season.FALL
                    MediaSeason.WINTER -> Season.WINTER
                    MediaSeason.UNKNOWN__ -> Season.UNKNOWN
                }
                mostPopularSeasonYear = rank.year
            }
        }
        return Stats(
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
            scoreDistribution = scoreDistribution,
            statusDistribution = statusDistribution
        )
    }

    suspend fun fetchReview(reviewId: Int): Review {
        try {
            val result =
                Apollo.apolloClient.query(
                    GetReviewDetailQuery(reviewId)
                )
                    .execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
            }
            val review = result.data?.Review
            if (review != null) {
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
                    userAvatar = review?.user?.avatar?.large ?: ""
                )
            }
        } catch (exception: ApolloException) {
            // handle exception here,, these are mainly for network errors
        }
        return Review()
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
                    userAvatar = review?.user?.avatar?.large ?: ""
                )
            )
        }
        return list
    }

    private fun parseMedia(anime: GetMediaDetailQuery.Media?): Media {
        val tags: MutableList<Tag> = mutableListOf()
        for (tag in anime?.tags.orEmpty()) {
            if (tag != null) {
                tags.add(Tag(tag.name, tag.rank ?: 0, tag.isMediaSpoiler ?: true))
            }
        }
        val synonyms = buildString {
            for (synonym in anime?.synonyms.orEmpty()) {
                append(synonym)
                if (anime?.synonyms?.last() != synonym) {
                    append("\n")
                }
            }
        }
        val genres: MutableList<String> = mutableListOf()
        for (genre in anime?.genres.orEmpty()) {
            if (genre != null) {
                genres.add(genre)
            }
        }
        val externalLinks: MutableList<Link> = mutableListOf()
        for (link in anime?.externalLinks.orEmpty()) {
            if (link != null) {
                externalLinks.add(
                    Link(
                        link.url ?: "",
                        link.site,
                        link.language ?: "",
                        link.color ?: "",
                        link.icon ?: ""
                    )
                )
            }
        }
        val relations: MutableList<Relation> = mutableListOf()
        for (relation in anime?.relations?.edges.orEmpty()) {
            relations.add(
                Relation(
                    id = relation?.node?.id ?: 0,
                    coverImage = relation?.node?.coverImage?.extraLarge ?: "",
                    title = relation?.node?.title?.native ?: "",
                    relation = relation?.relationType?.rawValue ?: ""
                )
            )
        }
        val media = Media(
            title = anime?.title?.native ?: "Unknown",
            coverImage = anime?.coverImage?.extraLarge ?: "",
            format = anime?.format?.name ?: "Unknown",
            seasonYear = anime?.seasonYear.toString(),
            episodeAmount = anime?.episodes ?: 0,
            averageScore = anime?.averageScore ?: 0,
            genres = genres,
            description = anime?.description ?: "No description found",
            relations = relations,
            infoList = mapOf(
                "format" to anime?.format?.name.orEmpty(),
                "status" to anime?.status?.name?.lowercase()
                    ?.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                    }
                    .orEmpty(),
                "startDate" to if (anime?.startDate != null) "${anime.startDate.day}-${anime.startDate.month}-${anime.startDate.year}" else "Unknown",
                "endDate" to if (anime?.endDate?.year != null && anime.endDate.month != null && anime.endDate.day != null) "${anime.endDate.day}-${anime.endDate.month}-${anime.endDate.year}" else "Unknown",
                "duration" to anime?.duration.toString(),
                "country" to anime?.countryOfOrigin.toString(),
                "source" to (anime?.source?.rawValue ?: "Unknown"),
                "hashtag" to (anime?.hashtag ?: "Unknown"),
                "licensed" to anime?.isLicensed.toString(),
                "updatedAt" to anime?.updatedAt.toString(),
                "synonyms" to synonyms,
                "nsfw" to anime?.isAdult.toString()
            ),
            tags = tags,
            trailerImage = anime?.trailer?.thumbnail ?: "",
            // todo add dailymotion
            trailerLink = if (anime?.trailer?.site == "youtube") "https://www.youtube.com/watch?v=${anime.trailer.id}" else if (anime?.trailer?.site == "dailymotion") "" else "",
            externalLinks = externalLinks,
            note = ""
        )
        return media
    }

    private fun parseCharacters(anime: GetMediaDetailQuery.Media?): List<Character> {
        val languages: MutableList<String> = mutableListOf()
        val characters: MutableList<Character> = mutableListOf()
        for (character in anime?.characters?.edges.orEmpty()) {
            for (voiceActor in character?.voiceActors.orEmpty()) {
                if (languages.contains(voiceActor?.languageV2) && voiceActor?.languageV2 != null) {
                    languages.add(voiceActor.languageV2)
                }
                if (character != null && voiceActor != null) {
                    characters.add(
                        Character(
                            id = character.node?.id ?: 0,
                            name = character.node?.name?.native ?: "",
                            coverImage = character.node?.image?.large ?: "",
                            voiceActorName = voiceActor.name?.native ?: "",
                            voiceActorCoverImage = voiceActor.image?.large ?: "",
                            voiceActorLanguage = voiceActor.languageV2 ?: ""
                        )
                    )
                }
            }
        }
        return characters
    }

    private fun parseStaff(media: GetStaffInfoQuery.Media?): List<Staff> {
        val list = mutableListOf<Staff>()
        for (staff in media?.staff?.edges.orEmpty()) {
            list.add(
                Staff(
                    staff?.node?.name?.userPreferred ?: "Unknown",
                    staff?.role ?: "Unkown",
                    staff?.node?.image?.large ?: "Unknown",
                    media?.staff?.pageInfo?.hasNextPage ?: false
                )
            )
        }
        return list
    }

    fun pagingRepository(): StaffPagingSource {
        return StaffPagingSource()
    }
}

class StaffPagingSource : PagingSource<Int, Staff>() {
    private val STARTING_KEY = 0

    /**
     * Provide a [Key] used for the initial [load] for the next [PagingSource] due to invalidation
     * of this [PagingSource]. The [Key] is provided to [load] via [LoadParams.key].
     *
     * The [Key] returned by this method should cause [load] to load enough items to
     * fill the viewport around the last accessed position, allowing the next generation to
     * transparently animate in. The last accessed position can be retrieved via
     * [state.anchorPosition][PagingState.anchorPosition], which is typically
     * the top-most or bottom-most item in the viewport due to access being triggered by binding
     * items as they scroll into view.
     *
     * For example, if items are loaded based on integer position keys, you can return
     * [state.anchorPosition][PagingState.anchorPosition].
     *
     * Alternately, if items contain a key used to load, get the key from the item in the page at
     * index [state.anchorPosition][PagingState.anchorPosition].
     *
     * @param state [PagingState] of the currently fetched data, which includes the most recently
     * accessed position in the list via [PagingState.anchorPosition].
     *
     * @return [Key] passed to [load] after invalidation used for initial load of the next
     * generation. The [Key] returned by [getRefreshKey] should load pages centered around
     * user's current viewport. If the correct [Key] cannot be determined, `null` can be returned
     * to allow [load] decide what default key to use.
     */
    override fun getRefreshKey(state: PagingState<Int, Staff>): Int? {
        TODO("Not yet implemented")
    }

    /**
     * Loading API for [PagingSource].
     *
     * Implement this method to trigger your async load (e.g. from database or network).
     */
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Staff> {
        val start = params.key ?: STARTING_KEY
        val range = start.until(start + params.loadSize)
        return LoadResult.Page(
            data = emptyList(),
            // Make sure we don't try to load items behind the STARTING_KEY
            prevKey = when (start) {
                STARTING_KEY -> null
                else -> ensureValidKey(key = range.first - params.loadSize)
            },
            nextKey = range.last + 1
        )
    }

    /**
     * Makes sure the paging key is never less than [STARTING_KEY]
     */
    private fun ensureValidKey(key: Int) = max(STARTING_KEY, key)
}
