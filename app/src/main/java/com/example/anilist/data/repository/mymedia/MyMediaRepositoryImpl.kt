package com.example.anilist.data.repository.mymedia

import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.cache.normalized.isFromCache
import com.apollographql.apollo3.exception.ApolloException
import com.example.anilist.DeleteEntryMutation
import com.example.anilist.GetMyMediaQuery
import com.example.anilist.MainActivity
import com.example.anilist.UpdateStatusMutation
import com.example.anilist.data.models.AniMediaFormat
import com.example.anilist.data.models.AniMediaListEntry
import com.example.anilist.data.models.AniResult
import com.example.anilist.data.models.Media
import com.example.anilist.data.models.AniPersonalMediaStatus
import com.example.anilist.data.models.StatusUpdate
import com.example.anilist.data.repository.mediadetail.getFuzzyDate
import com.example.anilist.data.repository.homerepository.toAni
import com.example.anilist.fragment.MyMedia
import com.example.anilist.type.FuzzyDateInput
import com.example.anilist.type.MediaListStatus
import com.example.anilist.type.MediaType
import com.example.anilist.utils.Apollo
import com.example.anilist.utils.Utils
import com.example.anilist.utils.Utils.Companion.orMinusOne
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyMediaRepositoryImpl @Inject constructor() : MyMediaRepository {
    override suspend fun getMyMedia(
        isAnime: Boolean,
        useNetworkFirst: Boolean
    ): AniResult<Pair<Map<AniPersonalMediaStatus, List<Media>>, Boolean>> {
        try {
            val param = if (isAnime) MediaType.ANIME else MediaType.MANGA
            val client =
                if (useNetworkFirst) Apollo.apolloClient.newBuilder()
                    .fetchPolicy(FetchPolicy.NetworkFirst)
                    .build() else Apollo.apolloClient
            val result =
                client.query(GetMyMediaQuery(param, MainActivity.userId))
                    .execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
                return AniResult.Failure(buildString { result.errors?.forEach { append(it.message + "\n") } })
            }
            val data = result.data
            return if (data != null) {
                val resultMap = mutableMapOf<AniPersonalMediaStatus, List<Media>>()
                for (statusList in data.MediaListCollection?.lists.orEmpty()) {
                    val list = mutableListOf<Media>()
                    for (entries in statusList?.entries.orEmpty()) {
                        if (entries?.myMedia != null) {
                            list.add(parseMyMediaFragment(entries.myMedia))
                        }
                    }
                    resultMap[statusList?.status?.toAniStatus() ?: AniPersonalMediaStatus.UNKNOWN] =
                        list
                }
                AniResult.Success(resultMap to result.isFromCache)
            } else {
                AniResult.Failure("Data received was empty")
            }
        } catch (exception: ApolloException) {
            // handle exception here,, these are mainly for network errors
            return AniResult.Failure(exception.message ?: "No exception message given")
        }
    }

    override suspend fun updateProgress(
        statusUpdate: StatusUpdate,
    ): AniResult<Media> {
        Timber.d("changing status of entry list id ${statusUpdate.entryListId} and with media id ${statusUpdate.mediaId}")
        try {
            val status = when (statusUpdate.status) {
                AniPersonalMediaStatus.CURRENT -> Optional.present(MediaListStatus.CURRENT)
                AniPersonalMediaStatus.PLANNING -> Optional.present(MediaListStatus.PLANNING)
                AniPersonalMediaStatus.COMPLETED -> Optional.present(MediaListStatus.COMPLETED)
                AniPersonalMediaStatus.DROPPED -> Optional.present(MediaListStatus.DROPPED)
                AniPersonalMediaStatus.PAUSED -> Optional.present(MediaListStatus.PAUSED)
                AniPersonalMediaStatus.REPEATING -> Optional.present(MediaListStatus.REPEATING)
                AniPersonalMediaStatus.UNKNOWN -> Optional.present(MediaListStatus.UNKNOWN__)
                null -> Optional.Absent
            }

            val result =
                Apollo.apolloClient.mutation(
                    UpdateStatusMutation(
                        id = if (statusUpdate.entryListId != -1) Optional.present(statusUpdate.entryListId) else Optional.absent(),
                        mediaId = if (statusUpdate.mediaId != -1) Optional.present(statusUpdate.mediaId) else Optional.absent(),
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

            if (result.isFromCache) {
                Timber.d("Result update media status is from cache ???")
            }

            return if (result.data?.SaveMediaListEntry?.myMedia != null) {
                AniResult.Success(
                    parseMyMediaFragment(result.data?.SaveMediaListEntry?.myMedia!!)
                )
            } else {
                Timber.d(statusUpdate.toString())
                Timber.d(result.exception?.message ?: "No message")
                AniResult.Failure("Failed updating progress, please try again")
            }
        } catch (exception: ApolloException) {
            // handle exception here, these are mainly for network errors
            return AniResult.Failure(exception.message ?: "No exception message given")
        }
    }

    /**
     * Delete a media list entry
     * @param entryListId id of the list entry (not the media id!)
     * @return whether the deletion was successful
     */
    override suspend fun deleteEntry(entryListId: Int): Boolean {
        try {
            val result =
                Apollo.apolloClient.mutation(
                    DeleteEntryMutation(
                        entryListId
                    ),
                ).execute()
            if (result.hasErrors()) {
                // these errors are related to GraphQL errors
                Timber.d(result.errors.toString())
            }
            return result.data?.DeleteMediaListEntry?.deleted ?: false
        } catch (exception: ApolloException) {
            // handle exception here,, these are mainly for network errors
            Timber.d(exception.message ?: "No exception message")
            return false
        }
    }

    private fun parseMyMediaFragment(data: MyMedia): Media {
        return Media(
            id = data.media?.id ?: -1,
            mediaListEntry = AniMediaListEntry(
                listEntryId = data.id,
                userId = data.userId,
                mediaId = data.mediaId,
                status = data.status?.toAniStatus() ?: AniPersonalMediaStatus.UNKNOWN,
                score = data.score ?: -1.0,
                progress = data.progress.orMinusOne(),
                progressVolumes = data.progressVolumes.orMinusOne(),
                repeat = data.repeat.orMinusOne(),
                private = data.private ?: false,
                notes = data.notes.orEmpty(),
                hiddenFromStatusLists = data.hiddenFromStatusLists ?: false,
                customLists = data.customLists.toString(),
                advancedScores = data.advancedScores.toString(),
                startedAt = getFuzzyDate(data.startedAt?.fuzzyDate),
                completedAt = getFuzzyDate(data.completedAt?.fuzzyDate),
                updatedAt = data.updatedAt?.toLong()?.let { Utils.convertEpochToFuzzyDate(it) }
            ),
//            listEntryId = data?.id ?: -1,
            title = data.media?.title?.userPreferred ?: "?",
            coverImage = data.media?.coverImage?.extraLarge ?: "",
            format = data.media?.format?.toAni() ?: AniMediaFormat.UNKNOWN,
            episodeAmount = data.media?.episodes ?: -1,
//            personalRating = data?.score ?: (-1).toDouble(),
//            personalProgress = data?.progress ?: -1,
//            isPrivate = data?.private ?: false,
//            note = data?.notes ?: "",
//            rewatches = data?.repeat ?: -1,
            volumes = data.media?.volumes ?: -1,
//            personalVolumeProgress = data?.progressVolumes ?: -1,
            chapters = data.media?.chapters ?: -1,
//            startedAt = if (data?.startedAt?.year != null && data.startedAt.month != null && data.startedAt.day != null) {
//                FuzzyDate(
//                    data.startedAt.year,
//                    data.startedAt.month,
//                    data.startedAt.day,
//                )
//            } else {
//                null
//            },
//            completedAt = if (data?.completedAt?.year != null && data.completedAt.month != null && data.completedAt.day != null) {
//                FuzzyDate(
//                    data.completedAt.year,
//                    data.completedAt.month,
//                    data.completedAt.day,
//                )
//            } else {
//                null
//            },
//            createdAt = data?.createdAt?.toLong()?.let { Utils.convertEpochToFuzzyDate(it) },
//            personalStatus = data?.status?.toAniStatus() ?: PersonalMediaStatus.UNKNOWN,
//            rawScore = data?.score ?: -1.0,
//            updatedAt = data?.updatedAt ?: -1,
            priority = data.priority ?: -1,
        )
    }
}

fun MediaListStatus?.toAniStatus(): AniPersonalMediaStatus {
    return when (this) {
        MediaListStatus.CURRENT -> AniPersonalMediaStatus.CURRENT
        MediaListStatus.PLANNING -> AniPersonalMediaStatus.PLANNING
        MediaListStatus.COMPLETED -> AniPersonalMediaStatus.COMPLETED
        MediaListStatus.DROPPED -> AniPersonalMediaStatus.DROPPED
        MediaListStatus.PAUSED -> AniPersonalMediaStatus.PAUSED
        MediaListStatus.REPEATING -> AniPersonalMediaStatus.REPEATING
        MediaListStatus.UNKNOWN__, null -> AniPersonalMediaStatus.UNKNOWN
    }
}
