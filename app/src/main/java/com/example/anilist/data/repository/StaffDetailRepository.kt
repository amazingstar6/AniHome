package com.example.anilist.data.repository

import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import com.example.anilist.GetStaffDetailQuery
import com.example.anilist.ToggleFavoriteCharacterMutation
import com.example.anilist.data.models.AniCharacterRole
import com.example.anilist.data.models.AniResult
import com.example.anilist.data.models.CharacterMediaConnection
import com.example.anilist.data.models.CharacterWithVoiceActor
import com.example.anilist.data.models.StaffDetail
import com.example.anilist.data.toAniRole
import com.example.anilist.fragment.StaffMedia
import com.example.anilist.utils.Apollo
import javax.inject.Inject

class StaffDetailRepository @Inject constructor() {
    suspend fun fetchStaffDetail(id: Int): AniResult<StaffDetail> {
        try {
            val result =
                Apollo.apolloClient.query(
                    GetStaffDetailQuery(id),
                )
                    .execute()
            if (result.hasErrors()) {
                return AniResult.Failure(buildString {
                    result.errors?.forEach { appendLine(it.message) }
                })
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
                return AniResult.Failure(buildString {
                    result.errors?.forEach { appendLine(it.message) }
                })
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

    private fun parseStaff(staff: GetStaffDetailQuery.Staff): StaffDetail {
        return StaffDetail(
            id = staff.id,
            coverImage = staff.image?.large ?: "",
            userPreferredName = staff.name?.userPreferred ?: "",
            alternativeNames = staff.name?.alternative?.filterNotNull().orEmpty(),
            favourites = staff.favourites ?: -1,
            language = staff.languageV2 ?: "",
            description = staff.description ?: "",
            isFavourite = staff.isFavourite,
            isFavouriteBlocked = staff.isFavouriteBlocked,
            voicedCharacters = parseVoicedCharactersForStaff(staff.characters),
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

    /**
     * We need to extract coverImage, title, characterRole and id into the object
     */
    private fun parseMediaForStaff(staffMedia: StaffMedia?): List<CharacterMediaConnection> {
        val result = mutableListOf<CharacterMediaConnection>()
        for (media in staffMedia?.edges.orEmpty()) {
            if (media != null) {
                result.add(
                    CharacterMediaConnection(
                        id = media.node?.id ?: -1,
                        title = media.node?.title?.userPreferred ?: "",
                        characterRole = media.staffRole ?: "",
                        coverImage = media.node?.coverImage?.extraLarge ?: "",
                    ),
                )
            }
        }
        return result
    }
}