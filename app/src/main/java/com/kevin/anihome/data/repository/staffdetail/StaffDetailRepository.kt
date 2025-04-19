package com.kevin.anihome.data.repository.staffdetail

import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import com.kevin.anihome.GetMediaOfStaffQuery
import com.kevin.anihome.GetStaffDetailQuery
import com.kevin.anihome.ToggleFavoriteCharacterMutation
import com.kevin.anihome.data.models.AniCharacterMediaConnection
import com.kevin.anihome.data.models.AniCharacterRole
import com.kevin.anihome.data.models.AniMediaType
import com.kevin.anihome.data.models.AniResult
import com.kevin.anihome.data.models.AniStaffDetail
import com.kevin.anihome.data.models.CharacterWithVoiceActor
import com.kevin.anihome.data.toAniHomeType
import com.kevin.anihome.data.toAniRole
import com.kevin.anihome.fragment.StaffMedia
import com.kevin.anihome.utils.Apollo
import javax.inject.Inject

class StaffDetailRepository
    @Inject
    constructor() {
        suspend fun fetchStaffDetail(id: Int): AniResult<AniStaffDetail> {
            try {
                val result =
                    Apollo.apolloClient.query(
                        GetStaffDetailQuery(id),
                    )
                        .execute()
                if (result.hasErrors()) {
                    return AniResult.Failure(
                        buildString {
                            result.errors?.forEach { appendLine(it.message) }
                        },
                    )
                }
                val data = result.data?.Staff
                return if (data != null) {
                    AniResult.Success(parseStaff(data))
                } else {
                    AniResult.Failure("Network error")
                }
            } catch (exception: ApolloException) {
                return AniResult.Failure(exception.localizedMessage ?: "No exception message given")
            }
        }

        suspend fun toggleFavourite(id: Int): AniResult<Boolean> {
            try {
                val result =
                    Apollo.apolloClient.mutation(
                        ToggleFavoriteCharacterMutation(staffId = Optional.present(id)),
                    )
                        .execute()
                if (result.hasErrors()) {
                    return AniResult.Failure(
                        buildString {
                            result.errors?.forEach { appendLine(it.message) }
                        },
                    )
                }
                // the result is a list of all the things you've already liked of the same type
                val isFavourite =
                    result.data?.ToggleFavourite?.staff?.nodes?.any { it?.id == id }
                return if (isFavourite != null) {
                    AniResult.Success(isFavourite)
                } else {
                    AniResult.Failure("Network error")
                }
            } catch (exception: ApolloException) {
                return AniResult.Failure(exception.localizedMessage ?: "No exception message given")
            }
        }

        private fun parseStaff(staff: GetStaffDetailQuery.Staff): AniStaffDetail {
            // excluding user preferred name
            val namesList = mutableListOf<String>()
            staff.name?.full?.let {
                namesList.add(it)
            }
            staff.name?.native?.let {
                namesList.add(it)
            }
            namesList.addAll(staff.name?.alternative?.filterNotNull().orEmpty())
            staff.name?.userPreferred?.let {
                namesList.remove(it)
            }
            return AniStaffDetail(
                id = staff.id,
                coverImage = staff.image?.large ?: "",
                userPreferredName = staff.name?.userPreferred ?: "",
                alternativeNames = namesList.distinct(),
                favourites = staff.favourites ?: -1,
                language = staff.languageV2 ?: "",
                description = staff.description ?: "",
                isFavourite = staff.isFavourite,
                isFavouriteBlocked = staff.isFavouriteBlocked,
                voicedCharacters = parseVoicedCharactersForStaff(staff.characters),
                // todo remove these; unused
                animeStaffRole = parseMediaForStaff(staff.anime?.staffMedia),
                mangaStaffRole = parseMediaForStaff(staff.manga?.staffMedia),
            )
        }

        /**
         * We need coverImage, name, role and id in Character object
         */
        private fun parseVoicedCharactersForStaff(characters: GetStaffDetailQuery.Characters?): List<CharacterWithVoiceActor> {
            val result = mutableListOf<CharacterWithVoiceActor>()
            for (character in characters?.edges.orEmpty()) {
                result.add(
                    CharacterWithVoiceActor(
                        id = character?.node?.id ?: -1,
                        name = character?.node?.name?.userPreferred ?: "",
                        role = character?.role?.toAniRole() ?: AniCharacterRole.UNKNOWN,
                        coverImage = character?.node?.image?.large ?: "",
                    ),
                )
            }
            return result
        }

        suspend fun fetchStaffMedia(
            staffId: Int,
            page: Int,
            pageSize: Int,
        ): AniResult<List<AniCharacterMediaConnection>> {
            try {
                val result =
                    Apollo.apolloClient.query(
                        GetMediaOfStaffQuery(staffId = staffId, page = page, pageSize = pageSize),
                    )
                        .execute()
                if (result.hasErrors()) {
                    return AniResult.Failure(
                        buildString {
                            result.errors?.forEach { appendLine(it.message) }
                        },
                    )
                }
                val data = result.data?.Staff
                return if (data != null) {
                    AniResult.Success(parseMediaForStaff(data.staffMedia?.staffMedia))
                } else {
                    AniResult.Failure("Network error")
                }
            } catch (exception: ApolloException) {
                return AniResult.Failure(exception.localizedMessage ?: "No exception message given")
            }
        }

        /**
         * We need to extract coverImage, title, characterRole and id into the object
         */
        private fun parseMediaForStaff(staffMedia: StaffMedia?): List<AniCharacterMediaConnection> {
            val result = mutableListOf<AniCharacterMediaConnection>()
            for (media in staffMedia?.edges.orEmpty()) {
                if (media != null) {
                    result.add(
                        AniCharacterMediaConnection(
                            id = media.node?.id ?: -1,
                            title = media.node?.title?.userPreferred ?: "",
                            characterRole = media.staffRole ?: "",
                            coverImage = media.node?.coverImage?.extraLarge ?: "",
                            type = media.node?.type?.toAniHomeType() ?: AniMediaType.UNKNOWN,
                        ),
                    )
                }
            }
            return result
        }
    }
