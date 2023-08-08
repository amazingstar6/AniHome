package com.example.anilist.data.repository

import android.util.Log
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import com.example.anilist.DeleteEntryMutation
import com.example.anilist.GetMyMediaQuery
import com.example.anilist.MainActivity
import com.example.anilist.UpdateStatusMutation
import com.example.anilist.data.models.AniResult
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
    suspend fun getMyMedia(isAnime: Boolean): AniResult<Map<PersonalMediaStatus, List<Media>>> {
        try {
            val param = if (isAnime) MediaType.ANIME else MediaType.MANGA
            val result =
                Apollo.apolloClient.query(GetMyMediaQuery(param, MainActivity.userId))
                    .execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
                return AniResult.Failure(buildString { result.errors?.forEach { append(it.message + "\n") } })
            }
            val data = result.data
            return if (data != null) {
                val resultMap = mutableMapOf<PersonalMediaStatus, List<Media>>()
                for (statusList in data.MediaListCollection?.lists.orEmpty()) {
                    val list = mutableListOf<Media>()
                    for (entries in statusList?.entries.orEmpty()) {
                        list.add(parseMedia(entries?.myMedia))
                    }
                    resultMap[statusList?.status?.toAniStatus() ?: PersonalMediaStatus.UNKNOWN] =
                        list
                }
                AniResult.Success(resultMap)
            } else {
                AniResult.Failure("Data received was empty")
            }
        } catch (exception: ApolloException) {
            // handle exception here,, these are mainly for network errors
            return AniResult.Failure(exception.message ?: "No exception message given")
        }
    }

    suspend fun updateProgress(
        statusUpdate: StatusUpdate,
    ): AniResult<Media> {
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
                return AniResult.Failure(buildString { result.errors?.forEach { appendLine(it.message) } })
            }

            return if (result.data != null) {
                AniResult.Success(
                    parseMedia(result.data?.SaveMediaListEntry?.myMedia)
                )
            } else {
                Log.d(TAG, statusUpdate.toString())
                Log.d(TAG, result.exception?.message ?: "No message")
                AniResult.Failure("Failed updating progress, please try again")
            }
        } catch (exception: ApolloException) {
            // handle exception here,, these are mainly for network errors
            return AniResult.Failure(exception.message ?: "No exception message given")
        }
    }

    /**
     * Delete a media list entry
     * @param entryListId id of the list entry (not the media id!)
     * @return whether the deletion was successful
     */
    suspend fun deleteEntry(entryListId: Int): Boolean {
        try {
            val result =
                Apollo.apolloClient.mutation(
                    DeleteEntryMutation(
                        entryListId
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
            personalStatus = data?.status?.toAniStatus() ?: PersonalMediaStatus.UNKNOWN,
            updatedAt = data?.updatedAt ?: -1
        )
    }
}

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
