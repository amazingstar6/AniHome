package com.example.anilist.data.repository.homerepository

import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import com.example.anilist.GetAllTimePopularQuery
import com.example.anilist.GetGenresQuery
import com.example.anilist.GetPopularManhwaQuery
import com.example.anilist.GetPopularThisSeasonQuery
import com.example.anilist.GetTagsQuery
import com.example.anilist.GetTop100AnimeQuery
import com.example.anilist.GetTrendingNowQuery
import com.example.anilist.GetUpComingNextSeasonQuery
import com.example.anilist.SearchCharactersQuery
import com.example.anilist.SearchMediaQuery
import com.example.anilist.SearchStaffQuery
import com.example.anilist.SearchStudiosQuery
import com.example.anilist.SearchThreadsQuery
import com.example.anilist.SearchUsersQuery
import com.example.anilist.data.models.AniMediaFormat
import com.example.anilist.data.models.AniMediaStatus
import com.example.anilist.data.models.AniResult
import com.example.anilist.data.models.AniStudio
import com.example.anilist.data.models.AniTag
import com.example.anilist.data.models.AniThread
import com.example.anilist.data.models.AniUser
import com.example.anilist.data.models.CharacterDetail
import com.example.anilist.data.models.FuzzyDate
import com.example.anilist.data.models.HomeTrendingTypes
import com.example.anilist.data.models.Media
import com.example.anilist.data.models.Season
import com.example.anilist.data.models.StaffDetail
import com.example.anilist.data.repository.TrendingTogetherPagingSource
import com.example.anilist.data.toAniHomeSeason
import com.example.anilist.data.toAniHomeType
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
import com.example.anilist.ui.home.MediaSearchState
import com.example.anilist.ui.home.SearchFilter
import com.example.anilist.utils.Apollo
import kotlinx.datetime.Clock
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

data class HomeMedia(
    var popularThisSeason: List<Media> = emptyList(),
    val trendingNow: List<Media> = emptyList(),
    val upcomingNextSeason: List<Media> = emptyList(),
    val allTimePopular: List<Media> = emptyList(),
    val top100Anime: List<Media> = emptyList(),
)

@Singleton
class HomeRepositoryImpl @Inject constructor() : HomeRepository {

    fun trendingTogetherPagingSource(isAnime: Boolean) =
        TrendingTogetherPagingSource(this, isAnime = isAnime)

    override fun trendingNowPagingSource(isAnime: Boolean) =
        MediaPagingSource(this, HomeTrendingTypes.TRENDING_NOW, isAnime = isAnime)

    override fun popularThisSeasonPagingSource() =
        MediaPagingSource(this, HomeTrendingTypes.POPULAR_THIS_SEASON, isAnime = true)

    override fun upcomingNextSeasonPagingSource() =
        MediaPagingSource(this, HomeTrendingTypes.UPCOMING_NEXT_SEASON, isAnime = true)

    override fun allTimePopularPagingSource(isAnime: Boolean) =
        MediaPagingSource(this, HomeTrendingTypes.ALL_TIME_POPULAR, isAnime = isAnime)

    override fun top100AnimePagingSource(isAnime: Boolean) =
        MediaPagingSource(this, HomeTrendingTypes.TOP_100_ANIME, isAnime = isAnime)

    override fun popularManhwaPagingSource() =
        MediaPagingSource(this, HomeTrendingTypes.POPULAR_MANHWA, isAnime = false)

    override suspend fun getTrendingNow(isAnime: Boolean, page: Int, pageSize: Int): AniResult<List<Media>> {
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
                return AniResult.Failure(buildString {
                    result.errors?.forEach { appendLine(it.message) }
                })
            }
            val data = result.data
            return if (data != null) {
                AniResult.Success(
                    data.Page?.media?.filterNotNull()
                        ?.map { parseMediaTitleCover(it.mediaTitleCover) }.orEmpty()
                )
            } else {
                AniResult.Failure("Network error")
            }
        } catch (exception: ApolloException) {
            // handle exception here, these are mainly for network errors
            return AniResult.Failure(exception.localizedMessage ?: "No exception message given")
        }
    }

    override suspend fun getPopularThisSeason(page: Int, pageSize: Int): AniResult<List<Media>> {
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
                return AniResult.Failure(buildString {
                    result.errors?.forEach { appendLine(it.message) }
                })
            }
            val data = result.data
            return if (data != null) {
                AniResult.Success(
                    data.Page?.media?.filterNotNull()
                        ?.map { parseMediaTitleCover(it.mediaTitleCover) }.orEmpty()
                )
            } else {
                AniResult.Failure("Network error")
            }
        } catch (exception: ApolloException) {
            // handle exception here, these are mainly for network errors
            return AniResult.Failure(exception.localizedMessage ?: "No exception message given")
        }
    }

    override suspend fun getUpcomingNextSeason(page: Int, pageSize: Int): AniResult<List<Media>> {
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
                return AniResult.Failure(buildString {
                    result.errors?.forEach { appendLine(it.message) }
                })
            }
            val data = result.data
            return if (data != null) {
                AniResult.Success(
                    data.Page?.media?.filterNotNull()
                        ?.map { parseMediaTitleCover(it.mediaTitleCover) }.orEmpty()
                )
            } else {
                AniResult.Failure("Network error")
            }
        } catch (exception: ApolloException) {
            // handle exception here, these are mainly for network errors
            return AniResult.Failure(exception.localizedMessage ?: "No exception given")
        }
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

    override suspend fun getAllTimePopularMedia(
        isAnime: Boolean,
        page: Int,
        pageSize: Int
    ): AniResult<List<Media>> {
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
                return AniResult.Failure(buildString {
                    result.errors?.forEach { appendLine(it.message) }
                })
            }
            val data = result.data
            return if (data != null) {
                AniResult.Success(
                    data.Page?.media?.filterNotNull()
                        ?.map { parseMediaTitleCover(it.mediaTitleCover) }.orEmpty()
                )
            } else {
                AniResult.Failure("Network error")
            }
        } catch (exception: ApolloException) {
            // handle exception here, these are mainly for network errors
            return AniResult.Failure(exception.localizedMessage ?: "No exception message given")
        }
    }

    override suspend fun getTop100Anime(isAnime: Boolean, page: Int, pageSize: Int): AniResult<List<Media>> {
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
                return AniResult.Failure(buildString {
                    result.errors?.forEach { appendLine(it.message) }
                })
            }
            val data = result.data
            return if (data != null) {
                AniResult.Success(
                    data.Page?.media?.filterNotNull()
                        ?.map { parseMediaTitleCover(it.mediaTitleCover) }.orEmpty()
                )
            } else {
                AniResult.Failure("Network error")
            }
        } catch (exception: ApolloException) {
            // handle exception here, these are mainly for network errors
            return AniResult.Failure(exception.localizedMessage ?: "No exception message given")
        }
    }

    override suspend fun getPopularManhwa(page: Int, pageSize: Int): AniResult<List<Media>> {
        try {
            val result =
                Apollo.apolloClient.query(
                    GetPopularManhwaQuery(
                        page = page,
                        pageSize = pageSize,
                    ),
                ).execute()
            if (result.hasErrors()) {
                return AniResult.Failure(buildString {
                    result.errors?.forEach { appendLine(it.message) }
                })
            }
            val data = result.data
            return if (data != null) {
                AniResult.Success(
                    data.Page?.media?.filterNotNull()
                        ?.map { parseMediaTitleCover(it.mediaTitleCover) }.orEmpty()
                )
            } else {
                AniResult.Failure("Network error")
            }
        } catch (exception: ApolloException) {
            // handle exception here, these are mainly for network errors
            return AniResult.Failure(exception.localizedMessage ?: "No exception message given")
        }
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

    private fun parseMediaTitleCover(media: MediaTitleCover): Media {
        return Media(
            id = media.id,
            type = media.type?.toAniHomeType() ?: com.example.anilist.data.models.MediaType.UNKNOWN,
            title = media.title?.userPreferred ?: "",
            coverImage = media.coverImage?.extraLarge ?: "",
        )
    }

    private fun parseSearchMedia(media: SearchMediaQuery.Medium): Media {
        return Media(
            id = media.id,
            type = media.type?.toAniHomeType() ?: com.example.anilist.data.models.MediaType.UNKNOWN,
            title = media.title?.userPreferred ?: "",
            coverImage = media.coverImage?.extraLarge ?: "",
            format = media.format?.toAni() ?: AniMediaFormat.UNKNOWN,
            season = media.season?.toAniHomeSeason() ?: Season.UNKNOWN,
            seasonYear = media.seasonYear ?: -1,
            episodeAmount = media.episodes ?: -1,
            averageScore = media.averageScore ?: -1,
            volumes = media.volumes ?: -1,
            chapters = media.chapters ?: -1,
            startDate = if (media.startDate?.year != null && media.startDate.month != null && media.startDate.day != null) FuzzyDate(
                media.startDate.year,
                media.startDate.month,
                media.startDate.day
            ) else null,
            endDate = if (media.endDate?.year != null && media.endDate.month != null && media.endDate.day != null) FuzzyDate(
                media.endDate.year,
                media.endDate.month,
                media.endDate.day
            ) else null,
        )
    }

    override suspend fun searchMedia(
        page: Int,
        pageSize: Int,
        searchState: MediaSearchState
    ): AniResult<List<Media>> {
        try {
            when (searchState.searchType) {
                SearchFilter.MEDIA, SearchFilter.ANIME, SearchFilter.MANGA -> {
                    val query: SearchMediaQuery = when (searchState.searchType) {
                        SearchFilter.MEDIA, SearchFilter.ANIME, SearchFilter.MANGA -> SearchMediaQuery(
                            page = page,
                            pageSize = pageSize,
                            search = if (searchState.query.isNotBlank()) Optional.present(
                                searchState.query
                            ) else Optional.absent(),
                            sort = Optional.present(
                                listOf(MediaSort.fromAniMediaSort(searchState.mediaSort))
                            ),
                            type = when (searchState.searchType) {
                                SearchFilter.MEDIA -> Optional.absent()
                                SearchFilter.ANIME -> Optional.present(MediaType.ANIME)
                                SearchFilter.MANGA -> Optional.present(MediaType.MANGA)
                                else -> Optional.absent() // cannot happen
                            },
                            season =
                            if (searchState.currentSeason != Season.UNKNOWN && searchState.searchType != SearchFilter.MANGA) {
                                Optional.present(
                                    MediaSeason.fromAniSeason(searchState.currentSeason)
                                )
                            } else {
                                Optional.absent()
                            },
                            airingStatus = if (searchState.status != AniMediaStatus.UNKNOWN && searchState.searchType != SearchFilter.MANGA) {
                                Optional.present(
                                    MediaStatus.fromAniMediaStatus(searchState.status)
                                )
                            } else {
                                Optional.absent()
                            },
                            year = if (searchState.year != -1) Optional.present(searchState.year) else Optional.absent(),
                            genres = if (searchState.genres.isNotEmpty()) Optional.present(
                                searchState.genres
                            ) else Optional.absent(),
                            tags = if (searchState.tags.isNotEmpty()) Optional.present(searchState.tags) else Optional.absent(),
                            onList = Optional.present(searchState.onlyOnMyList)
                        )

                        else -> { // it should not get here
                            Timber.wtf("Error #1")
                            SearchMediaQuery(page, pageSize,if (searchState.query.isNotBlank()) Optional.present(
                                searchState.query
                            ) else Optional.absent())
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
                    return if (result.data != null) {
                        result.data?.Page?.media?.forEach {
                            if (it != null) {
                                resultList.add(parseSearchMedia(it))
                            }
                        }
                        AniResult.Success(resultList)
                    } else {
                        AniResult.Failure("Network error")
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

    override suspend fun searchCharacters(
        page: Int,
        pageSize: Int,
        text: String,
        sort: AniCharacterSort
    ): AniResult<List<CharacterDetail>> {
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
                return AniResult.Failure(buildString { result.errors?.forEach { appendLine(it.message) } })
            }

            val data = result.data

            return if (data != null) {
                val resultList = mutableListOf<CharacterDetail>()
                data.Page?.characters?.forEach {
                    if (it != null) {
                        resultList.add(parseSearchCharacters(it))
                    }
                }
                AniResult.Success(resultList)
            } else {
                AniResult.Failure("Network error")
            }
        } catch (exception: ApolloException) {
            // handle exception here, these are mainly for network errors
            return AniResult.Failure(exception.localizedMessage ?: "No exception message given")
        }
    }

    override suspend fun searchStaff(text: String, page: Int, pageSize: Int): AniResult<List<StaffDetail>> {
        try {
            val result =
                Apollo.apolloClient.query(
                    SearchStaffQuery(text, page, pageSize),
                )
                    .execute()
            if (result.hasErrors()) {
                return AniResult.Failure(buildString {
                    result.errors?.forEach { appendLine(it.message) }
                })
            }

            val data = result.data
            return if (data != null) {
                val resultList = mutableListOf<StaffDetail>()
                result.data?.Page?.staff?.forEach {
                    if (it != null) {
                        resultList.add(parseSearchStaff(it))
                    }
                }
                return AniResult.Success(resultList)
            } else {
                AniResult.Failure("Network error")
            }
        } catch (exception: ApolloException) {
            // handle exception here, these are mainly for network errors
            return AniResult.Failure(exception.localizedMessage ?: "No exception message given")
        }
    }

    override suspend fun searchStudio(text: String, page: Int, pageSize: Int): AniResult<List<AniStudio>> {
        try {
            val result =
                Apollo.apolloClient.query(
                    SearchStudiosQuery(text, page, pageSize),
                ).execute()
            if (result.hasErrors()) {
                return AniResult.Failure(buildString {
                    result.errors?.forEach { appendLine(it.message) }
                })
            }

            val data = result.data
            return if (data != null) {
                val resultList = mutableListOf<AniStudio>()
                result.data?.Page?.studios?.forEach {
                    if (it != null) {
                        resultList.add(parseSearchStudio(it))
                    }
                }
                AniResult.Success(resultList)
            } else {
                AniResult.Failure("Network error")
            }

        } catch (exception: ApolloException) {
            return AniResult.Failure(exception.localizedMessage ?: "No exception message given")
        }
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

    override suspend fun searchForum(text: String, page: Int, pageSize: Int): AniResult<List<AniThread>> {
        try {
            val result =
                Apollo.apolloClient.query(
                    SearchThreadsQuery(text, page, pageSize),
                )
                    .execute()
            if (result.hasErrors()) {
                return AniResult.Failure(buildString {
                    result.errors?.forEach { appendLine(it.message) }
                })
            }

            val data = result.data

            return if (data != null) {
                val resultList = mutableListOf<AniThread>()
                result.data?.Page?.threads?.forEach {
                    if (it != null) {
                        resultList.add(parseForumSearch(it))
                    }
                }
                AniResult.Success(resultList)
            } else {
                AniResult.Failure("Network error")
            }


        } catch (exception: ApolloException) {
            // handle exception here, these are mainly for network errors
            return AniResult.Failure(exception.message ?: "No exception message given")
        }
    }

    private fun parseForumSearch(thread: SearchThreadsQuery.Thread): AniThread {
        return AniThread(
            id = thread.id,
            title = thread.title ?: ""
        )
    }

    override suspend fun searchUser(text: String, page: Int, pageSize: Int): AniResult<List<AniUser>> {
        try {
            val result =
                Apollo.apolloClient.query(
                    SearchUsersQuery(text, page, pageSize),
                )
                    .execute()
            if (result.hasErrors()) {
                return AniResult.Failure(buildString {
                    result.errors?.forEach { appendLine(it.message) }
                })
            }

            val data = result.data

            return if (data != null) {
                val resultList = mutableListOf<AniUser>()
                result.data?.Page?.users?.forEach {
                    if (it != null) {
                        resultList.add(parseSearchUser(it))
                    }
                }
                AniResult.Success(resultList)
            } else {
                AniResult.Failure("Network error")
            }


        } catch (exception: ApolloException) {
            return AniResult.Failure(exception.localizedMessage ?: "No exception message given")
        }
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

    override suspend fun getTags(): AniResult<List<AniTag>> {
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

    override suspend fun getGenres(): AniResult<List<String>> {
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
}

fun MediaFormat?.toAni(): AniMediaFormat {
    return when (this) {
        MediaFormat.TV -> AniMediaFormat.TV
        MediaFormat.TV_SHORT -> AniMediaFormat.TV_SHORT
        MediaFormat.MOVIE -> AniMediaFormat.MOVIE
        MediaFormat.SPECIAL -> AniMediaFormat.SPECIAL
        MediaFormat.OVA -> AniMediaFormat.OVA
        MediaFormat.ONA -> AniMediaFormat.ONA
        MediaFormat.MUSIC -> AniMediaFormat.MUSIC
        MediaFormat.MANGA -> AniMediaFormat.MANGA
        MediaFormat.NOVEL -> AniMediaFormat.NOVEL
        MediaFormat.ONE_SHOT -> AniMediaFormat.ONE_SHOT
        MediaFormat.UNKNOWN__ -> AniMediaFormat.UNKNOWN
        null -> AniMediaFormat.UNKNOWN
    }
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
        AniCharacterSort.FAVOURITES -> CharacterSort.FAVOURITES
        AniCharacterSort.FAVOURITES_DESC -> CharacterSort.FAVOURITES_DESC
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


