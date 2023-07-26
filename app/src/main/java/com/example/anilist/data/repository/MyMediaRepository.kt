package com.example.anilist.data.repository

import android.util.Log
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import com.example.anilist.Apollo
import com.example.anilist.GetMyMediaQuery
import com.example.anilist.IncreaseEpisodeProgressMutation
import com.example.anilist.UpdateStatusMutation
import com.example.anilist.data.models.Media
import com.example.anilist.data.models.StatusUpdate
import com.example.anilist.fragment.MyMedia
import com.example.anilist.type.MediaListStatus
import com.example.anilist.type.MediaType
import com.example.anilist.ui.my_media.MediaStatus
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "MyMediaRepository"

@Singleton
class MyMediaRepository @Inject constructor() {
    suspend fun getMyMedia(isAnime: Boolean): Map<MediaStatus, List<Media>> {
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
                val resultMap = mutableMapOf<MediaStatus, List<Media>>()
                for (statusList in data.MediaListCollection?.lists.orEmpty()) {
                    val list = mutableListOf<Media>()
                    for (entries in statusList?.entries.orEmpty()) {
                        list.add(parseMedia(entries?.myMedia))
                    }
                    resultMap[statusList?.status?.toAniStatus() ?: MediaStatus.UNKNOWN] = list
                }
                return resultMap
            }
        } catch (exception: ApolloException) {
            // handle exception here,, these are mainly for network errors
//            emit(ResultData(ResultStatus.ERROR, exception.message ?: "No error message"))
        }
        return emptyMap()
    }

    suspend fun updateProgress(
        statusUpdate: StatusUpdate
    ): Media {
        try {
            val status = when (statusUpdate.status) {
                MediaStatus.CURRENT -> Optional.present(MediaListStatus.CURRENT)
                MediaStatus.PLANNING -> Optional.present(MediaListStatus.PLANNING)
                MediaStatus.COMPLETED -> Optional.present(MediaListStatus.COMPLETED)
                MediaStatus.DROPPED -> Optional.present(MediaListStatus.DROPPED)
                MediaStatus.PAUSED -> Optional.present(MediaListStatus.PAUSED)
                MediaStatus.REPEATING -> Optional.present(MediaListStatus.REPEATING)
                MediaStatus.UNKNOWN -> Optional.present(MediaListStatus.UNKNOWN__)
                null -> Optional.Absent
            }
            val result =
                Apollo.apolloClient.mutation(
                    UpdateStatusMutation(
                        id = statusUpdate.id,
                        status = status,
                        scoreRaw = if (statusUpdate.scoreRaw == null) Optional.Absent else Optional.present(
                            statusUpdate.scoreRaw
                        ),
                        progress = if (statusUpdate.progress == null) Optional.Absent else Optional.present(
                            statusUpdate.progress
                        ),
                        progressVolumes = if (statusUpdate.progressVolumes == null) Optional.Absent else Optional.present(
                            statusUpdate.progressVolumes
                        ),
                        repeat = if (statusUpdate.repeat == null) Optional.Absent else Optional.present(
                            statusUpdate.repeat
                        ),
                        priority = if (statusUpdate.priority == null) Optional.Absent else Optional.present(
                            statusUpdate.priority
                        ),
                        private = if (statusUpdate.privateToUser == null) Optional.Absent else Optional.present(
                            statusUpdate.privateToUser
                        ),
                        notes = if (statusUpdate.notes == null) Optional.Absent else Optional.present(
                            statusUpdate.notes
                        ),
                        hiddenFromStatusLists = if (statusUpdate.hiddenFromStatusList == null) Optional.Absent else Optional.present(
                            statusUpdate.hiddenFromStatusList
                        ),
                        customLists = if (statusUpdate.customLists == null) Optional.Absent else Optional.present(
                            statusUpdate.customLists
                        ),
                        advancedScores = if (statusUpdate.advancedScores == null) Optional.Absent else Optional.present(
                            statusUpdate.advancedScores
                        ),
                        startedAt = Optional.Absent, /*if (statusUpdate.startedAt == null) Optional.Absent else Optional.present(statusUpdate.startedAt)*/
                        completedAt = Optional.Absent, /*if (statusUpdate.completedAt == null) Optional.Absent else Optional.present(statusUpdate.completedAt)*/
                    )
                ).execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
                Log.d(TAG, result.errors.toString())
            }
            return parseMedia(result.data?.SaveMediaListEntry?.myMedia)
        } catch (exception: ApolloException) {
            // handle exception here,, these are mainly for network errors
            Log.d(TAG, exception.message ?: "No exception message")
            return Media()
        }
    }

    private fun parseMedia(data: MyMedia?): Media {
        return Media(
            id = data?.media?.id ?: -1,
            listEntryId = data?.id ?: -1,
            title = data?.media?.title?.userPreferred ?: "?",
            coverImage = data?.media?.coverImage?.extraLarge ?: "",
            format = data?.media?.format?.name ?: "",
            episodeAmount = data?.media?.episodes ?: -1,
            personalRating = data?.score ?: (-1).toDouble(),
            personalProgress = data?.progress ?: -1,
            personalVolumeProgress = data?.progressVolumes ?: -1,
            rewatches = data?.repeat ?: -1,
            chapters = data?.media?.chapters ?: -1,
            volumes = data?.media?.volumes ?: -1,
            note = data?.notes ?: ""
        )
    }

    suspend fun increaseEpisodeProgress(mediaId: Int, newProgress: Int): Int {
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

}






//suspend fun reloadMedia(isAnime: Boolean): List<Media> {
//    try {
//        val param = if (isAnime) MediaType.ANIME else MediaType.MANGA
//        val result =
//            Apollo.apolloClient.query(GetMyMediaQuery(Optional.present(param)))
//                .execute()
//        if (result.hasErrors()) {
//            // these errors are related to GraphQL errors
////                emit(ResultData(ResultStatus.ERROR, result.errors.toString()))
//        }
//        val data = result.data
//        if (data != null) {
//            return parseMedia(data)
//        }
//    } catch (exception: ApolloException) {
//        // handle exception here,, these are mainly for network errors
////            emit(ResultData(ResultStatus.ERROR, exception.message ?: "No error message"))
//    }
//    return emptyList()
//}

fun MediaListStatus.toAniStatus(): MediaStatus {
    return when (this) {
        MediaListStatus.CURRENT -> MediaStatus.CURRENT
        MediaListStatus.PLANNING -> MediaStatus.PLANNING
        MediaListStatus.COMPLETED -> MediaStatus.COMPLETED
        MediaListStatus.DROPPED -> MediaStatus.DROPPED
        MediaListStatus.PAUSED -> MediaStatus.PAUSED
        MediaListStatus.REPEATING -> MediaStatus.REPEATING
        MediaListStatus.UNKNOWN__ -> MediaStatus.UNKNOWN
    }
}
