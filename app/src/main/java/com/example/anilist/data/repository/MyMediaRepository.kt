package com.example.anilist.data.repository

import android.util.Log
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import com.example.anilist.Apollo
import com.example.anilist.GetMyMediaQuery
import com.example.anilist.IncreaseEpisodeProgressMutation
import com.example.anilist.data.models.Media
import com.example.anilist.type.MediaType
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private const val TAG = "MyMediaRepository"

@Singleton
class MyMediaRepository @Inject constructor() {
    fun getMyMedia(isAnime: Boolean): Flow<List<Media>> = flow {
        Log.i(TAG, "My anime is being loaded!")

        try {
            val param = if (isAnime) MediaType.ANIME else MediaType.MANGA
            val result =
                Apollo.apolloClient.query(GetMyMediaQuery(Optional.present(param)))
                    .execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
//                emit(ResultData(ResultStatus.ERROR, result.errors.toString()))
            }
            val data = result.data
            if (data != null) {
                emit(parseMedia(data))
            }
        } catch (exception: ApolloException) {
            // handle exception here,, these are mainly for network errors
//            emit(ResultData(ResultStatus.ERROR, exception.message ?: "No error message"))
        }
    }

    private fun parseMedia(data: GetMyMediaQuery.Data): List<Media> {
        val list: List<GetMyMediaQuery.List> =
            data.MediaListCollection?.lists?.filterNotNull().orEmpty()
        return list[0].entries?.map {
            Media(
                id = it?.media?.id ?: -1,
                title = it?.media?.title?.userPreferred ?: "?",
                coverImage = it?.media?.coverImage?.extraLarge ?: "",
                format = it?.media?.format?.name ?: "",
                episodeAmount = it?.media?.episodes ?: -1,
                personalRating = it?.score ?: (-1).toDouble(),
                personalProgress = it?.progress ?: -1,
                personalVolumeProgress = it?.progressVolumes ?: -1,
                rewatches = it?.repeat ?: -1,
                chapters = it?.media?.chapters ?: -1,
                volumes = it?.media?.volumes ?: -1,
                note = it?.notes ?: ""
            )
        } ?: emptyList()
    }

    fun updateStatus() {
        TODO()
    }

    suspend fun increaseEpisodeProgress(mediaId: Int, newProgress: Int): Int {
        Log.d(TAG, "Episode progress is being increased")
        Log.i(TAG, "Episode progress is being increased")
        try {
            val result =
                Apollo.apolloClient.mutation(
                    IncreaseEpisodeProgressMutation(
                        mediaId,
                        newProgress
                    )
                ).execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
                Log.d(TAG, result.errors.toString())
            }
            return result.data?.SaveMediaListEntry?.progress ?: -1
        } catch (exception: ApolloException) {
            // handle exception here,, these are mainly for network errors
            Log.d(TAG, exception.message ?: "No exception message")
            return -1
        }
    }

    fun increaseVolumeProgress(mediaId: Int) {
        TODO()
    }

    fun changeStatus() {
        TODO()
    }

    suspend fun reloadMedia(isAnime: Boolean): List<Media> {
        try {
            val param = if (isAnime) MediaType.ANIME else MediaType.MANGA
            val result =
                Apollo.apolloClient.query(GetMyMediaQuery(Optional.present(param)))
                    .execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
//                emit(ResultData(ResultStatus.ERROR, result.errors.toString()))
            }
            val data = result.data
            if (data != null) {
                return parseMedia(data)
            }
        } catch (exception: ApolloException) {
            // handle exception here,, these are mainly for network errors
//            emit(ResultData(ResultStatus.ERROR, exception.message ?: "No error message"))
        }
        return emptyList()
    }
//
//    fun changeEpisodeProgress(newProgress: Int) {
//        TODO()
//    }
//
//    fun changeTotalRewatches(newAmount: Int) {
//        TODO()
//    }
//
//    fun changeScore(newScore: Int) {
//        TODO()
//    }
//
//    fun changeStartDate(newDate: Int) {
//        TODO()
//    }
//
//    fun changeFinishDate(newDate: Int) {
//        TODO()
//    }
//
//    fun addNote(note: String) {
//        TODO()
//    }
}
