package com.example.anilist.data.repository

import android.content.Context
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import com.example.anilist.GetAllTimePopularQuery
import com.example.anilist.GetGenresQuery
import com.example.anilist.GetPopularManhwaQuery
import com.example.anilist.GetPopularThisSeasonQuery
import com.example.anilist.GetTagsQuery
import com.example.anilist.GetTop100AnimeQuery
import com.example.anilist.GetTrendingMediaQuery
import com.example.anilist.GetTrendingNowQuery
import com.example.anilist.GetUpComingNextSeasonQuery
import com.example.anilist.R
import com.example.anilist.SearchCharactersQuery
import com.example.anilist.SearchMediaQuery
import com.example.anilist.SearchStaffQuery
import com.example.anilist.SearchStudiosQuery
import com.example.anilist.SearchThreadsQuery
import com.example.anilist.SearchUsersQuery
import com.example.anilist.data.models.AniMediaStatus
import com.example.anilist.data.models.AniResult
import com.example.anilist.data.models.AniStudio
import com.example.anilist.data.models.AniTag
import com.example.anilist.data.models.AniThread
import com.example.anilist.data.models.AniUser
import com.example.anilist.data.models.CharacterDetail
import com.example.anilist.data.models.FuzzyDate
import com.example.anilist.data.models.Media
import com.example.anilist.data.models.Season
import com.example.anilist.data.models.StaffDetail
import com.example.anilist.fragment.MediaTitleCover
import com.example.anilist.type.CharacterSort
import com.example.anilist.type.MediaFormat
import com.example.anilist.type.MediaSeason
import com.example.anilist.type.MediaSort
import com.example.anilist.type.MediaStatus
import com.example.anilist.type.MediaType
import com.example.anilist.ui.home.AniCharacterSort
import com.example.anilist.ui.home.AniMediaSort
import com.example.anilist.ui.home.MediaPagingSource
import com.example.anilist.ui.home.SearchFilter
import com.example.anilist.utils.Apollo
import kotlinx.datetime.Clock
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

enum class HomeTrendingTypes {
    TRENDING_NOW,
    POPULAR_THIS_SEASON,
    UPCOMING_NEXT_SEASON,
    ALL_TIME_POPULAR,
    TOP_100_ANIME,
    POPULAR_MANHWA;

    fun toString(context: Context): String {
        return when (this) {
            TRENDING_NOW -> context.getString(R.string.trending_now)
            POPULAR_THIS_SEASON -> context.getString(R.string.popular_this_season)
            UPCOMING_NEXT_SEASON -> context.getString(R.string.upcoming_next_season)
            ALL_TIME_POPULAR -> context.getString(R.string.all_time_popular)
            TOP_100_ANIME -> context.getString(R.string.top_100_anime)
            POPULAR_MANHWA -> context.getString(R.string.popular_manhwa)
        }
    }
}

data class HomeMedia(
    var popularThisSeason: List<Media> = emptyList(),
    val trendingNow: List<Media> = emptyList(),
    val upcomingNextSeason: List<Media> = emptyList(),
    val allTimePopular: List<Media> = emptyList(),
    val top100Anime: List<Media> = emptyList(),
)

private const val TAG = "HomeRepository"

@Singleton
class HomeRepository @Inject constructor() {

    fun trendingTogetherPagingSource(isAnime: Boolean) =
        TrendingTogetherPagingSource(this, isAnime = isAnime)

    fun trendingNowPagingSource(isAnime: Boolean) =
        MediaPagingSource(this, HomeTrendingTypes.TRENDING_NOW, isAnime = isAnime)

    fun popularThisSeasonPagingSource() =
        MediaPagingSource(this, HomeTrendingTypes.POPULAR_THIS_SEASON, isAnime = true)

    fun upcomingNextSeasonPagingSource() =
        MediaPagingSource(this, HomeTrendingTypes.UPCOMING_NEXT_SEASON, isAnime = true)

    fun allTimePopularPagingSource(isAnime: Boolean) =
        MediaPagingSource(this, HomeTrendingTypes.ALL_TIME_POPULAR, isAnime = isAnime)

    fun top100AnimePagingSource(isAnime: Boolean) =
        MediaPagingSource(this, HomeTrendingTypes.TOP_100_ANIME, isAnime = isAnime)

    fun popularManhwaPagingSource() =
        MediaPagingSource(this, HomeTrendingTypes.POPULAR_MANHWA, isAnime = false)

    suspend fun getTrendingNow(isAnime: Boolean, page: Int, pageSize: Int): List<Media> {
        try {
            val result =
                Apollo.apolloClient.query(
                    GetTrendingNowQuery(
                        mediaType = if (isAnime) MediaType.ANIME else MediaType.MANGA,
                        page = page,
                        pageSize = pageSize
                    ),
                )
                    .execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
            }
            val data = result.data
            if (data != null) {
                return data.Page?.media?.filterNotNull()
                    ?.map { parseMediaTitleCover(it.mediaTitleCover) }.orEmpty()
            }
        } catch (exception: ApolloException) {
            // handle exception here, these are mainly for network errors
            return emptyList()
        }
        return emptyList()
    }

    suspend fun getPopularThisSeason(page: Int, pageSize: Int): List<Media> {
        try {
            val result =
                Apollo.apolloClient.query(
                    GetPopularThisSeasonQuery(
                        page = page,
                        pageSize = pageSize,
                        currentSeason = getCurrentSeason(),
                        currentYear = getCurrentYear()
                    ),
                )
                    .execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
            }
            val data = result.data
            if (data != null) {
                return data.Page?.media?.filterNotNull()
                    ?.map { parseMediaTitleCover(it.mediaTitleCover) }.orEmpty()
            }
        } catch (exception: ApolloException) {
            // handle exception here, these are mainly for network errors
            return emptyList()
        }
        return emptyList()
    }

    suspend fun getUpcomingNextSeason(page: Int, pageSize: Int): List<Media> {
        try {
//            Log.d(TAG, "Parameters are: $page, $pageSize, ${getNextSeason()}, ${getNextYear()}")
            val result =
                Apollo.apolloClient.query(
                    GetUpComingNextSeasonQuery(
                        page = page,
                        pageSize = pageSize,
                        nextSeason = getNextSeason(),
                        nextYear = getNextYear()
                    ),
                )
                    .execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
            }
            val data = result.data
//            Log.d(TAG, "Data for upcoming this season received is ${data?.Page?.media}")
            if (data != null) {
                return data.Page?.media?.filterNotNull()
                    ?.map { parseMediaTitleCover(it.mediaTitleCover) }.orEmpty()
            }
        } catch (exception: ApolloException) {
            // handle exception here, these are mainly for network errors
            return emptyList()
        }
        return emptyList()
    }

    private fun getNextYear(): Int {
        val month = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).month
        val nextSeason = getMediaSeasonFromMonth((month.number + 4) % 12)
        val year = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
        return if (nextSeason == MediaSeason.SPRING) year + 1 else year
    }

    private fun getNextSeason(): MediaSeason {
        // plus four months equals the next season
        return getMediaSeasonFromMonth(
            (Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).monthNumber + 4 % 12)
        )
    }

    suspend fun getAllTimePopularMedia(isAnime: Boolean, page: Int, pageSize: Int): List<Media> {
        try {
            val result =
                Apollo.apolloClient.query(
                    GetAllTimePopularQuery(
                        mediaType = if (isAnime) MediaType.ANIME else MediaType.MANGA,
                        page = page,
                        pageSize = pageSize
                    ),
                )
                    .execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
            }
            val data = result.data
            if (data != null) {
                return data.Page?.media?.filterNotNull()
                    ?.map { parseMediaTitleCover(it.mediaTitleCover) }.orEmpty()
            }
        } catch (exception: ApolloException) {
            // handle exception here, these are mainly for network errors
            return emptyList()
        }
        return emptyList()
    }

    suspend fun getTop100Anime(isAnime: Boolean, page: Int, pageSize: Int): List<Media> {
        try {
            val result =
                Apollo.apolloClient.query(
                    GetTop100AnimeQuery(
                        mediaType = if (isAnime) MediaType.ANIME else MediaType.MANGA,
                        page = page,
                        pageSize = pageSize,
                    ),
                ).execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
            }
            val data = result.data
            if (data != null) {
                return data.Page?.media?.filterNotNull()
                    ?.map { parseMediaTitleCover(it.mediaTitleCover) }.orEmpty()
            }
        } catch (exception: ApolloException) {
            // handle exception here, these are mainly for network errors
            return emptyList()
        }
        return emptyList()
    }

    suspend fun getPopularManhwa(page: Int, pageSize: Int): List<Media> {
        try {
            val result =
                Apollo.apolloClient.query(
                    GetPopularManhwaQuery(
                        page = page,
                        pageSize = pageSize,
                    ),
                ).execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
            }
            val data = result.data
            if (data != null) {
                return data.Page?.media?.filterNotNull()
                    ?.map { parseMediaTitleCover(it.mediaTitleCover) }.orEmpty()
            }
        } catch (exception: ApolloException) {
            // handle exception here, these are mainly for network errors
            return emptyList()
        }
        return emptyList()
    }

    suspend fun getHomeMedia(
        isAnime: Boolean,
        page: Int,
        pageSize: Int,
        includeTrendingNow: Boolean = false,
        includePopularThisSeason: Boolean = true,
        includeUpcomingNextSeason: Boolean = true,
        includeAllTimePopular: Boolean = true,
        includeTop100Anime: Boolean = true,
    ): Result<HomeMedia> {
        try {
            val param = if (isAnime) MediaType.ANIME else MediaType.MANGA

            val instant = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val month = instant.month

            val season = getMediaSeasonFromMonth(month.number)
            // we add four because one season is four months long
            val nextSeason = getMediaSeasonFromMonth((month.number + 4) % 12)

            val year = instant.year
            val nextYear = if (nextSeason == MediaSeason.SPRING) year + 1 else year

            val result =
                Apollo.apolloClient.query(
                    GetTrendingMediaQuery(
                        page = page,
                        pageSize = pageSize,
                        type = param,
                        currentSeason = season,
                        nextSeason = nextSeason,
                        currentYear = year,
                        nextYear = nextYear,
                        skipPopularThisSeason = Optional.present(includePopularThisSeason),
                        skipTrendingNow = Optional.present(includeTrendingNow),
                        skipUpcomingNextSeason = Optional.present(includeUpcomingNextSeason),
                        skipAllTimePopular = Optional.present(includeAllTimePopular),
                        skipTop100Anime = Optional.present(includeTop100Anime),
                    ),
                )
                    .execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
                return Result.success(HomeMedia())
            }
            val data = result.data
            if (data != null) {
                return Result.success(parseHomeMedia(data))
            }
        } catch (exception: ApolloException) {
            // handle exception here, these are mainly for network errors
            return Result.failure(exception)
        }
        return Result.success(HomeMedia())
    }

    private fun getMediaSeasonFromMonth(
        month: Int,
    ): MediaSeason {
        return when (month) {
            Month.JANUARY.number -> MediaSeason.WINTER
            Month.FEBRUARY.number -> MediaSeason.WINTER
            Month.MARCH.number -> MediaSeason.WINTER

            Month.APRIL.number -> MediaSeason.SPRING
            Month.MAY.number -> MediaSeason.SPRING
            Month.JUNE.number -> MediaSeason.SPRING

            Month.JULY.number -> MediaSeason.SUMMER
            Month.AUGUST.number -> MediaSeason.SUMMER
            Month.SEPTEMBER.number -> MediaSeason.SUMMER

            Month.OCTOBER.number -> MediaSeason.FALL
            Month.NOVEMBER.number -> MediaSeason.FALL
            Month.DECEMBER.number -> MediaSeason.FALL

            else -> MediaSeason.UNKNOWN__
        }
    }

    private fun parseHomeMedia(data: GetTrendingMediaQuery.Data): HomeMedia {
        val popularThisSeason: MutableList<Media> = mutableListOf()
        val trendingNow: MutableList<Media> = mutableListOf()
        val upcomingNextSeason: MutableList<Media> = mutableListOf()
        val allTimePopular: MutableList<Media> = mutableListOf()
        val top100Anime: MutableList<Media> = mutableListOf()

        for (media in data.popularThisSeason?.media.orEmpty()) {
            if (media != null) {
                popularThisSeason.add(parseMediaTitleCover(media.mediaTitleCover))
            }
        }

        for (media in data.trendingNow?.media.orEmpty()) {
            if (media != null) {
                trendingNow.add(parseMediaTitleCover(media.mediaTitleCover))
            }
        }

        for (media in data.upcomingNextSeason?.media.orEmpty()) {
            if (media != null) {
                upcomingNextSeason.add(parseMediaTitleCover(media.mediaTitleCover))
            }
        }

        for (media in data.allTimePopular?.media.orEmpty()) {
            if (media != null) {
                allTimePopular.add(parseMediaTitleCover(media.mediaTitleCover))
            }
        }

        for (media in data.top100Anime?.media.orEmpty()) {
            if (media != null) {
                top100Anime.add(parseMediaTitleCover(media.mediaTitleCover))
            }
        }

        return HomeMedia(
            popularThisSeason,
            trendingNow,
            upcomingNextSeason,
            allTimePopular,
            top100Anime,
        )
    }

    private fun parseMediaTitleCover(media: MediaTitleCover): Media {
        return Media(
            id = media.id,
            title = media.title?.userPreferred ?: "",
            coverImage = media.coverImage?.extraLarge ?: "",
            type = media.type?.toAniHomeType() ?: com.example.anilist.data.models.MediaType.UNKNOWN
        )
    }

    private fun parseSearchMedia(media: SearchMediaQuery.Medium): Media {
        return Media(
            id = media.id,
            title = media.title?.userPreferred ?: "",
            coverImage = media.coverImage?.extraLarge ?: "",
            type = media.type?.toAniHomeType() ?: com.example.anilist.data.models.MediaType.UNKNOWN,
            format = media.format?.toCapitalizedString() ?: "",
            episodeAmount = media.episodes ?: -1,
            chapters = media.chapters ?: -1,
            volumes = media.volumes ?: -1,
            averageScore = media.averageScore ?: -1,
            season = media.season?.toAniHomeSeason() ?: Season.UNKNOWN,
            seasonYear = media.seasonYear ?: -1,
            startDate = if (media.startDate?.year != null && media.startDate.month != null && media.startDate.day != null) FuzzyDate(
                media.startDate.year,
                media.startDate.month,
                media.startDate.day
            ) else null,
            endDate = if (media.endDate?.year != null && media.endDate.month != null && media.endDate.day != null) FuzzyDate(
                media.endDate.year,
                media.endDate.month,
                media.endDate.day
            ) else null
        )
    }

    suspend fun searchMedia(
        page: Int,
        pageSize: Int,
        text: String,
        type: SearchFilter,
        sort: AniMediaSort,
        season: Season,
        status: AniMediaStatus,
        year: Int,
        genres: List<String>,
        tags: List<String>
    ): AniResult<List<Media>> {
        try {
            when (type) {
                SearchFilter.MEDIA, SearchFilter.ANIME, SearchFilter.MANGA -> {
                    Timber.d("Season is $season")
                    val query: SearchMediaQuery = when (type) {
                        SearchFilter.MEDIA, SearchFilter.ANIME, SearchFilter.MANGA -> SearchMediaQuery(
                            page = page,
                            pageSize = pageSize,
                            text,
                            sort = Optional.present(
                                listOf(MediaSort.fromAniMediaSort(sort))
                            ),
                            type = when (type) {
                                SearchFilter.MEDIA -> Optional.absent()
                                SearchFilter.ANIME -> Optional.present(MediaType.ANIME)
                                SearchFilter.MANGA -> Optional.present(MediaType.MANGA)
                                else -> Optional.absent() // cannot happen
                            },
                            season =
                            if (season != Season.UNKNOWN && type != SearchFilter.MANGA) {
                                Optional.present(
                                    MediaSeason.fromAniSeason(season)
                                )
                            } else {
                                Optional.absent()
                            },
                            airingStatus = if (status != AniMediaStatus.UNKNOWN && type != SearchFilter.MANGA) {
                                Optional.present(
                                    MediaStatus.fromAniMediaStatus(status)
                                )
                            } else {
                                Optional.absent()
                            },
                            year = if (year != -1) Optional.present(year) else Optional.absent(),
                            genres = if (genres.isNotEmpty()) Optional.present(genres) else Optional.absent(),
                            tags = if (tags.isNotEmpty()) Optional.present(tags) else Optional.absent()
                        )

//                        SearchFilter.ANIME -> SearchMediaQuery(
//                            page,
//                            pageSize,
//                            text,
//                            type = Optional.present(MediaType.ANIME),
//                            sort = Optional.present(listOf(MediaSort.fromAniCharacterSort(sort)))
//                        )
//
//                        SearchFilter.MANGA -> SearchMediaQuery(
//                            page,
//                            pageSize,
//                            text,
//                            type = Optional.present(MediaType.MANGA),
//                            sort = Optional.present(listOf(MediaSort.fromAniCharacterSort(sort)))
//                        )

                        else -> {
                            SearchMediaQuery(page, pageSize, text)
                        }
                    }
                    val result =
                        Apollo.apolloClient.query(
                            query,
                        )
                            .execute()
                    if (result.hasErrors()) {
                        // these errors are related to GraphQL errors
                        return AniResult.Failure(buildString {
                            result.errors?.forEach { appendLine(it.message) }
                        })
                    }

                    val resultList = mutableListOf<Media>()
                    if (result.data != null) {
                        result.data?.Page?.media?.forEach {
                            if (it != null) {
                                resultList.add(parseSearchMedia(it))
                            }
                        }
                        return AniResult.Success(resultList)
                    } else {
                        return AniResult.Failure("Network error")
                    }
                }

                else -> return AniResult.Failure("Wrong search type!")
            }

        } catch (exception: ApolloException) {
            // handle exception here, these are mainly for network errors
            return AniResult.Failure(
                exception.localizedMessage ?: "No message given from exception"
            )
        }
    }

    private fun parseSearchCharacters(character: SearchCharactersQuery.Character): CharacterDetail {
        return CharacterDetail(
            id = character.id,
            userPreferredName = character.name?.userPreferred ?: "",
            coverImage = character.image?.large ?: "",
            favorites = character.favourites ?: -1,
            isFavourite = character.isFavourite,
            isFavoriteBlocked = character.isFavouriteBlocked
        )
    }

    suspend fun searchCharacters(
        page: Int,
        pageSize: Int,
        text: String,
        sort: AniCharacterSort
    ): List<CharacterDetail> {
        try {
            val result =
                Apollo.apolloClient.query(
                    SearchCharactersQuery(
                        page,
                        pageSize,
                        text,
                        CharacterSort.fromAniCharacterSort(sort)
                    ),
                )
                    .execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
                return emptyList()
            }

            val resultList = mutableListOf<CharacterDetail>()
            result.data?.Page?.characters?.forEach {
                if (it != null) {
                    resultList.add(parseSearchCharacters(it))
                }
            }
            return resultList
        } catch (exception: ApolloException) {
            // handle exception here, these are mainly for network errors
        }
        return emptyList()
    }

    suspend fun searchStaff(text: String, page: Int, pageSize: Int): List<StaffDetail> {
        try {
            val result =
                Apollo.apolloClient.query(
                    SearchStaffQuery(text, page, pageSize),
                )
                    .execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
                return emptyList()
            }

            val resultList = mutableListOf<StaffDetail>()
            result.data?.Page?.staff?.forEach {
                if (it != null) {
                    resultList.add(parseSearchStaff(it))
                }
            }
            return resultList
        } catch (exception: ApolloException) {
            // handle exception here, these are mainly for network errors
        }
        return emptyList()
    }

    suspend fun searchStudio(text: String, page: Int, pageSize: Int): List<AniStudio> {
        try {
            val result =
                Apollo.apolloClient.query(
                    SearchStudiosQuery(text, page, pageSize),
                )
                    .execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
                return emptyList()
            }

            val resultList = mutableListOf<AniStudio>()
            result.data?.Page?.studios?.forEach {
                if (it != null) {
                    resultList.add(parseSearchStudio(it))
                }
            }
            return resultList
        } catch (exception: ApolloException) {
            // handle exception here, these are mainly for network errors
        }
        return emptyList()
    }

    private fun parseSearchStudio(studio: SearchStudiosQuery.Studio): AniStudio {
        return AniStudio(
            id = studio.id,
            name = studio.name,
            favourites = studio.favourites ?: -1,
            isFavourite = studio.isFavourite
        )
    }

    private fun parseSearchStaff(staff: SearchStaffQuery.Staff): StaffDetail {
        return StaffDetail(
            id = staff.id,
            userPreferredName = staff.name?.userPreferred ?: "",
            coverImage = staff.image?.large ?: "",
            favourites = staff.favourites ?: -1,
            isFavourite = staff.isFavourite,
            isFavouriteBlocked = staff.isFavouriteBlocked
        )
    }

    suspend fun searchForum(text: String, page: Int, pageSize: Int): List<AniThread> {
        try {
            val result =
                Apollo.apolloClient.query(
                    SearchThreadsQuery(text, page, pageSize),
                )
                    .execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
                return emptyList()
            }

            val resultList = mutableListOf<AniThread>()
            result.data?.Page?.threads?.forEach {
                if (it != null) {
                    resultList.add(parseForumSearch(it))
                }
            }
            return resultList
        } catch (exception: ApolloException) {
            // handle exception here, these are mainly for network errors
        }
        return emptyList()
    }

    private fun parseForumSearch(thread: SearchThreadsQuery.Thread): AniThread {
        return AniThread(
            id = thread.id,
            title = thread.title ?: ""
        )
    }

    suspend fun searchUser(text: String, page: Int, pageSize: Int): List<AniUser> {
        try {
            val result =
                Apollo.apolloClient.query(
                    SearchUsersQuery(text, page, pageSize),
                )
                    .execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
                return emptyList()
            }

            val resultList = mutableListOf<AniUser>()
            result.data?.Page?.users?.forEach {
                if (it != null) {
                    resultList.add(parseSearchUser(it))
                }
            }
            return resultList
        } catch (exception: ApolloException) {
            // handle exception here, these are mainly for network errors
        }
        return emptyList()
    }

    private fun parseSearchUser(user: SearchUsersQuery.User): AniUser {
        return AniUser(
            id = user.id,
            name = user.name
        )
    }

    private fun getCurrentSeason(): MediaSeason {
        return getMediaSeasonFromMonth(
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).monthNumber
        )
    }

    private fun getCurrentYear(): Int {
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
    }

    suspend fun getTags(): AniResult<List<AniTag>> {
        try {
            val result = Apollo.apolloClient.query(GetTagsQuery()).execute()
            if (result.hasErrors()) {
                return AniResult.Failure(buildString { result.errors?.forEach { appendLine(it.message) } })
            }
            val data = result.data
            return if (data == null) {
                AniResult.Failure("Network error")
            } else {
                AniResult.Success(data.MediaTagCollection?.filterNotNull()?.map {
                    AniTag(
                        id = it.id,
                        name = it.name,
                        category = it.category ?: "",
                        description = it.description ?: "",
                        isAdult = it.isAdult ?: false
                    )
                }.orEmpty())
            }
        } catch (e: ApolloException) {
            return AniResult.Failure(e.localizedMessage ?: "No exception message given")
        }
    }

    suspend fun getGenres(): AniResult<List<String>> {
        try {
            val result = Apollo.apolloClient.query(GetGenresQuery()).execute()
            if (result.hasErrors()) {
                return AniResult.Failure(buildString { result.errors?.forEach { appendLine(it.message) } })
            }
            val data = result.data
            return if (data == null) {
                AniResult.Failure("Network error")
            } else {
                AniResult.Success(data.GenreCollection?.filterNotNull().orEmpty())
            }
        } catch (e: ApolloException) {
            return AniResult.Failure(e.localizedMessage ?: "No exception message given")
        }
    }


//    fun getTrendingAnime() = flow {
//        try {
//            val result = Apollo.apolloClient.query(GetTrendingMediaQuery()).execute()
//            if (result.hasErrors()) {
//                // these errors are related to GraphQL errors
// //                emit(ResultData(ResultStatus.ERROR, result.errors.toString()))
//            }
//            val data = result.data
//            if (data != null) {
//                emit(
//                    mapOf(HomeTrendingTypes.POPULAR_THIS_SEASON to parseMedia(data.trending?.popularThisSeason))
//                )
//            }
//        } catch (exception: ApolloException) {
//            // handle exception here,, these are mainly for network errors
//            emit(ResultData(ResultStatus.ERROR, exception.message ?: "No error message"))
//        }
//    }
//
//    private fun parseMedia(medias: List<GetTrendingMediaQuery.PopularThisSeason?>?): List<Anime> {
//        val mediaList: MutableList<Anime> = mutableListOf()
//        for (media in medias.orEmpty()) {
//            if (media.title?.userPreferred != null && media.coverImage?.extraLarge != null) {
//                mediaList.add(
//                    Anime(
//                        id = media.id,
//                        title = media.title.userPreferred,
//                        coverImage = media.coverImage?.extraLarge
//                    )
//                )
//            }
//        }
//        return mediaList
//    }
}

private fun MediaStatus.Companion.fromAniMediaStatus(status: AniMediaStatus): MediaStatus {
    return when (status) {
        AniMediaStatus.FINISHED -> MediaStatus.FINISHED
        AniMediaStatus.RELEASING -> MediaStatus.RELEASING
        AniMediaStatus.NOT_YET_RELEASED -> MediaStatus.NOT_YET_RELEASED
        AniMediaStatus.CANCELLED -> MediaStatus.CANCELLED
        AniMediaStatus.HIATUS -> MediaStatus.HIATUS
        AniMediaStatus.UNKNOWN -> MediaStatus.UNKNOWN__
    }
}

private fun MediaSeason.Companion.fromAniSeason(season: Season): MediaSeason {
    return when (season) {
        Season.UNKNOWN -> MediaSeason.UNKNOWN__
        Season.SPRING -> MediaSeason.SPRING
        Season.SUMMER -> MediaSeason.SUMMER
        Season.FALL -> MediaSeason.FALL
        Season.WINTER -> MediaSeason.WINTER
    }
}

private fun CharacterSort.Companion.fromAniCharacterSort(sort: AniCharacterSort): CharacterSort {
    return when (sort) {
        AniCharacterSort.DEFAULT -> CharacterSort.SEARCH_MATCH
//        AniCharacterSort.RELEVANCE -> CharacterSort.RELEVANCE
//        AniCharacterSort.ROLE -> CharacterSort.ROLE
//        AniCharacterSort.ROLE_DESC -> CharacterSort.ROLE_DESC
        AniCharacterSort.FAVOURITES -> CharacterSort.FAVOURITES
        AniCharacterSort.FAVOURITES_DESC -> CharacterSort.FAVOURITES_DESC
    }
}

fun MediaFormat?.toCapitalizedString(): String {
    return if (this?.rawValue != "TV") {
        this?.rawValue?.lowercase()
            ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            ?: "Unknown"
    } else {
        this.rawValue
    }
}

fun MediaSort.Companion.fromAniMediaSort(it: AniMediaSort): MediaSort {
    return when (it) {
        AniMediaSort.SEARCH_MATCH -> MediaSort.SEARCH_MATCH
        AniMediaSort.START_DATE -> MediaSort.START_DATE_DESC
        AniMediaSort.END_DATE -> MediaSort.END_DATE_DESC
        AniMediaSort.SCORE -> MediaSort.SCORE_DESC
        AniMediaSort.POPULARITY -> MediaSort.POPULARITY_DESC
        AniMediaSort.TRENDING -> MediaSort.TRENDING_DESC
        AniMediaSort.EPISODES -> MediaSort.EPISODES_DESC
        AniMediaSort.DURATION -> MediaSort.DURATION_DESC
        AniMediaSort.CHAPTERS -> MediaSort.CHAPTERS_DESC
        AniMediaSort.VOLUMES -> MediaSort.VOLUMES_DESC
        AniMediaSort.FAVOURITES -> MediaSort.FAVOURITES_DESC
    }
}


