package com.example.anilist.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.apollographql.apollo3.exception.ApolloException
import com.example.anilist.Apollo
import com.example.anilist.GetTrendingMediaQuery
import com.example.anilist.data.models.Media
import com.example.anilist.fragment.MediaTitleCover
import com.example.anilist.type.MediaSeason
import com.example.anilist.type.MediaType
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.datetime.Clock
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

enum class HomeTrendingTypes {
    POPULAR_THIS_SEASON,
    TRENDING_NOW,
    UPCOMING_NEXT_SEASON,
    ALL_TIME_POPULAR,
    TOP_100_ANIME
}

data class HomeMedia(
    val popularThisSeason: List<Media> = emptyList(),
    val trendingNow: List<Media> = emptyList(),
    val upcomingNextSeason: List<Media> = emptyList(),
    val allTimePopular: List<Media> = emptyList(),
    val top100Anime: List<Media> = emptyList()
)

@Singleton
class HomeRepository @Inject constructor() {
    val popularThisSeasonPage: Int = 1

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getHomeMedia(isAnime: Boolean): Result<HomeMedia> {
        try {
            val param = if (isAnime) MediaType.ANIME else MediaType.MANGA

            val instant = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val month = instant.month

            val season = getMediaSeasonFromMonth(month.number)
            // we add four because one season is four months long
            val nextSeason = getMediaSeasonFromMonth((month.number + 4) % 12)

            val result =
                Apollo.apolloClient.query(
                    GetTrendingMediaQuery(
                        type = param,
                        currentSeason = season,
                        nextSeason = nextSeason
                    )
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getMediaSeasonFromMonth(
        month: Int
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
            top100Anime
        )
    }

    private fun parseMediaTitleCover(media: MediaTitleCover): Media {
        return Media(
            id = media.id,
            title = media.title?.userPreferred ?: "",
            coverImage = media.coverImage?.extraLarge ?: ""
        )
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
