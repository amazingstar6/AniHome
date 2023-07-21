package com.example.anilist.data.repository

import javax.inject.Inject
import javax.inject.Singleton

enum class HomeTrendingTypes {
    POPULAR_THIS_SEASON,
    TRENDING_NOW,
    UPCOMING_NEXT_SEASON,
    ALL_TIME_POPULAR,
    TOP_100_ANIME
}

@Singleton
class HomeRepository @Inject constructor() {

//    fun getTrendingAnime() = flow {
//        try {
//            val result = Apollo.apolloClient.query(GetTrendingMediaQuery()).execute()
//            if (result.hasErrors()) {
//                // these errors are related to GraphQL errors
////                emit(ResultData(ResultStatus.ERROR, result.errors.toString()))
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