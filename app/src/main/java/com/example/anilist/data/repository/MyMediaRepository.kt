package com.example.anilist.data.repository

import android.util.Log
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import com.example.anilist.DeleteEntryMutation
import com.example.anilist.GetMyMediaQuery
import com.example.anilist.IncreaseEpisodeProgressMutation
import com.example.anilist.MainActivity
import com.example.anilist.UpdateStatusMutation
import com.example.anilist.data.models.FuzzyDate
import com.example.anilist.data.models.Media
import com.example.anilist.data.models.StatusUpdate
import com.example.anilist.fragment.MyMedia
import com.example.anilist.type.FuzzyDateInput
import com.example.anilist.type.MediaListStatus
import com.example.anilist.type.MediaType
import com.example.anilist.ui.mymedia.PersonalMediaStatus
import com.example.anilist.utils.Apollo
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "MyMediaRepository"

@Singleton
class MyMediaRepository @Inject constructor() {
    suspend fun getMyMedia(isAnime: Boolean): Map<PersonalMediaStatus, List<Media>> {
        try {
//            val currentUser = ApolloClient.Builder().httpHeaders(
//                listOf(
//                    HttpHeader(
//                        "Authorization",
//                        "Bearer " + "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6IjI0MzFmYjM2MjMwNWFiM2Y2NzI5MmNlMGY4M2RmOGM0MmM1NGI0NmEyYmI5NjNlN2ZiODA1YzI1YTU0ZGU4ZjJmZTk5NWUxMDEwNGRiNWUxIn0.eyJhdWQiOiIxMzYxNiIsImp0aSI6IjI0MzFmYjM2MjMwNWFiM2Y2NzI5MmNlMGY4M2RmOGM0MmM1NGI0NmEyYmI5NjNlN2ZiODA1YzI1YTU0ZGU4ZjJmZTk5NWUxMDEwNGRiNWUxIiwiaWF0IjoxNjg5NzYwNTQ3LCJuYmYiOjE2ODk3NjA1NDcsImV4cCI6MTcyMTM4Mjk0Nywic3ViIjoiODA4NjI2Iiwic2NvcGVzIjpbXX0.RD0LdCD8AmzNlJM_OIWbrPz-Ec9RBKZrtGE0vWhvM7G9cs5vWf4WF3QGrd0P5k5-YZJB_Cr7YJCe8mV_n2B0yHm3Ia0kde7gdRx1V9aaXDRNH-MNidYjVq-RuVLfkI-bgw82vGXQ42Y_dhFZypJiYdh2SYIY09OWgNqwvxLu-D-EYVJMBEdsWbd6RRJdKzyCQ0EMsUxgmBCgHuMt2KghA5FMhTj_eWzT30rEs1ziREGTpIz_aJS-pHed8husWF-WhwC0YY0r0NXbuge--tpGvGd8ShJb2AQ0lDvQ7JomvFlkqEUXZf7jC_rQqeLApKqnx-iwCTy-0JMDLuRGHkvXtn6pGCgdFwySTLfoMYInXX3vuYMxlfuheINkkx2qL5n51PlMRXRaADfH_C2jmFFU5fNLHFSmTE_tvVMMdflEmQWwt1htz1g-EZp9wGMC88j56fXpoNeI4htppkOWn5mLioyZFoX5hqD7zPu3yQd6A9cistkQWvz0VBIOGQH0d-5eKlVkHIQpP289cx3ho2abxBmilDQsF0RhOueLaSPHtsuyuvOie-gcvmQUPYuMEkwUpEvyxrXqg976YwTGCcN1Rl6BG4RhCgj5ngF3HhPTj4HSO1X5-gynKLcL3S61yjD6je3WecFrYqj8xStZekffuYcD0TKyAOYE0BQndC7xQTg"
//                    )
//                )
//            ).serverUrl("https://graphql.anilist.co").build().query(GetCurrentUserQuery()).execute()
//            val currentUser = Apollo.apolloClient.query(GetCurrentUserQuery()).execute()
//            val userId = currentUser.data?.Viewer?.id ?: -1
            Log.d(TAG, "Found user id ${MainActivity.userId}")

            val param = if (isAnime) MediaType.ANIME else MediaType.MANGA
            val result =
                Apollo.apolloClient.query(GetMyMediaQuery(param, MainActivity.userId))
                    .execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
                Log.d(TAG, result.errors.toString())
            }
            val data = result.data
            if (data != null) {
                val resultMap = mutableMapOf<PersonalMediaStatus, List<Media>>()
                for (statusList in data.MediaListCollection?.lists.orEmpty()) {
                    val list = mutableListOf<Media>()
                    for (entries in statusList?.entries.orEmpty()) {
                        list.add(parseMedia(entries?.myMedia, statusList?.status?.toAniStatus()))
                    }
                    resultMap[statusList?.status?.toAniStatus() ?: PersonalMediaStatus.UNKNOWN] = list
                }
                return resultMap
            }
        } catch (exception: ApolloException) {
            // handle exception here,, these are mainly for network errors
            Log.d(TAG, exception.message ?: "No error message given")
        }
        return emptyMap()
    }

    suspend fun updateProgress(
        statusUpdate: StatusUpdate,
    ): Media {
        Log.d(TAG, "changing status of entry list id ${statusUpdate.entryListId}")
        try {
            val status = when (statusUpdate.status) {
                PersonalMediaStatus.CURRENT -> Optional.present(MediaListStatus.CURRENT)
                PersonalMediaStatus.PLANNING -> Optional.present(MediaListStatus.PLANNING)
                PersonalMediaStatus.COMPLETED -> Optional.present(MediaListStatus.COMPLETED)
                PersonalMediaStatus.DROPPED -> Optional.present(MediaListStatus.DROPPED)
                PersonalMediaStatus.PAUSED -> Optional.present(MediaListStatus.PAUSED)
                PersonalMediaStatus.REPEATING -> Optional.present(MediaListStatus.REPEATING)
                PersonalMediaStatus.UNKNOWN -> Optional.present(MediaListStatus.UNKNOWN__)
                null -> Optional.Absent
            }

            val result =
                Apollo.apolloClient.mutation(
                    UpdateStatusMutation(
                        id = statusUpdate.entryListId,
                        status = status,
                        scoreRaw = if (statusUpdate.scoreRaw == null) {
                            Optional.Absent
                        } else {
                            Optional.present(
                                statusUpdate.scoreRaw,
                            )
                        },
                        progress = if (statusUpdate.progress == null) {
                            Optional.Absent
                        } else {
                            Optional.present(
                                statusUpdate.progress,
                            )
                        },
                        progressVolumes = if (statusUpdate.progressVolumes == null) {
                            Optional.Absent
                        } else {
                            Optional.present(
                                statusUpdate.progressVolumes,
                            )
                        },
                        repeat = if (statusUpdate.repeat == null) {
                            Optional.Absent
                        } else {
                            Optional.present(
                                statusUpdate.repeat,
                            )
                        },
                        priority = if (statusUpdate.priority == null) {
                            Optional.Absent
                        } else {
                            Optional.present(
                                statusUpdate.priority,
                            )
                        },
                        private = if (statusUpdate.privateToUser == null) {
                            Optional.Absent
                        } else {
                            Optional.present(
                                statusUpdate.privateToUser,
                            )
                        },
                        notes = if (statusUpdate.notes == null) {
                            Optional.Absent
                        } else {
                            Optional.present(
                                statusUpdate.notes,
                            )
                        },
                        hiddenFromStatusLists = if (statusUpdate.hiddenFromStatusList == null) {
                            Optional.Absent
                        } else {
                            Optional.present(
                                statusUpdate.hiddenFromStatusList,
                            )
                        },
                        customLists = if (statusUpdate.customLists == null) {
                            Optional.Absent
                        } else {
                            Optional.present(
                                statusUpdate.customLists,
                            )
                        },
                        advancedScores = if (statusUpdate.advancedScores == null) {
                            Optional.Absent
                        } else {
                            Optional.present(
                                statusUpdate.advancedScores,
                            )
                        },
                        // dates are null when they're empty, they can be null unlike other fields
                        startedAt = if (statusUpdate.startedAt == null) {
                            Optional.present(null)
                        } else {
                            Optional.present(
                                FuzzyDateInput(
                                    Optional.present(statusUpdate.startedAt.year),
                                    Optional.present(statusUpdate.startedAt.month),
                                    Optional.present(statusUpdate.startedAt.day),
                                ),
                            )
                        },
                        completedAt = if (statusUpdate.completedAt == null) {
                            Optional.present(null)
                        } else {
                            Optional.present(
                                FuzzyDateInput(
                                    Optional.present(statusUpdate.completedAt.year),
                                    Optional.present(statusUpdate.completedAt.month),
                                    Optional.present(statusUpdate.completedAt.day),
                                ),
                            )
                        },
                    ),
                ).execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
                Log.d(TAG, result.errors.toString())
            }
            return parseMedia(
                result.data?.SaveMediaListEntry?.myMedia,
                statusUpdate.status //fixme
            )
        } catch (exception: ApolloException) {
            // handle exception here,, these are mainly for network errors
            Log.d(TAG, exception.message ?: "No exception message")
            return Media()
        }
    }

    private fun parseMedia(data: MyMedia?, status: PersonalMediaStatus?): Media {
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
            note = data?.notes ?: "",
            isPrivate = data?.private ?: false,
            startedAt = if (data?.startedAt?.year != null && data.startedAt.month != null && data.startedAt.day != null) {
                FuzzyDate(
                    data.startedAt.year,
                    data.startedAt.month,
                    data.startedAt.day,
                )
            } else {
                null
            },
            completedAt = if (data?.completedAt?.year != null && data.completedAt.month != null && data.completedAt.day != null) {
                FuzzyDate(
                    data.completedAt.year,
                    data.completedAt.month,
                    data.completedAt.day,
                )
            } else {
                null
            },
            rawScore = data?.score ?: -1.0,
            personalStatus = status ?: PersonalMediaStatus.UNKNOWN
        )
    }

    suspend fun increaseEpisodeProgress(mediaId: Int, newProgress: Int): Int {
        Log.i(TAG, "Episode progress is being increased")
        try {
            val result =
                Apollo.apolloClient.mutation(
                    IncreaseEpisodeProgressMutation(
                        mediaId,
                        newProgress,
                    ),
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

    /**
     * Delete a media list entry
     * @param id id of the list entry (not the media id!)
     * @return whether the deletion was successful
     */
    suspend fun deleteEntry(id: Int): Boolean {
        try {
            val result =
                Apollo.apolloClient.mutation(
                    DeleteEntryMutation(
                        id
                    ),
                ).execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
                Log.d(TAG, result.errors.toString())
            }
            return result.data?.DeleteMediaListEntry?.deleted ?: false
        } catch (exception: ApolloException) {
            // handle exception here,, these are mainly for network errors
            Log.d(TAG, exception.message ?: "No exception message")
            return false
        }
    }
}

// suspend fun reloadMedia(isAnime: Boolean): List<Media> {
//    try {
//        val param = if (isAnime) MediaType.ANIME else MediaType.MANGA
//        val result =
//            Apollo.apolloClient.query(GetMyMediaQuery(Optional.present(param)))
//                .execute()
//        if (result.hasErrors()) {
//            // these errors are related to GraphQL errors
// //                emit(ResultData(ResultStatus.ERROR, result.errors.toString()))
//        }
//        val data = result.data
//        if (data != null) {
//            return parseMedia(data)
//        }
//    } catch (exception: ApolloException) {
//        // handle exception here,, these are mainly for network errors
// //            emit(ResultData(ResultStatus.ERROR, exception.message ?: "No error message"))
//    }
//    return emptyList()
// }

fun MediaListStatus.toAniStatus(): PersonalMediaStatus {
    return when (this) {
        MediaListStatus.CURRENT -> PersonalMediaStatus.CURRENT
        MediaListStatus.PLANNING -> PersonalMediaStatus.PLANNING
        MediaListStatus.COMPLETED -> PersonalMediaStatus.COMPLETED
        MediaListStatus.DROPPED -> PersonalMediaStatus.DROPPED
        MediaListStatus.PAUSED -> PersonalMediaStatus.PAUSED
        MediaListStatus.REPEATING -> PersonalMediaStatus.REPEATING
        MediaListStatus.UNKNOWN__ -> PersonalMediaStatus.UNKNOWN
    }
}
