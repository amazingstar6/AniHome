package com.example.anilist.data.repository

import com.apollographql.apollo3.exception.ApolloException
import com.example.anilist.GetCharacterDetailQuery
import com.example.anilist.data.models.AniResult
import com.example.anilist.data.models.CharacterDetail
import com.example.anilist.data.models.CharacterMediaConnection
import com.example.anilist.data.models.StaffDetail
import com.example.anilist.utils.Apollo
import javax.inject.Inject

class CharacterDetailRepository @Inject constructor() {
    suspend fun fetchCharacter(characterId: Int): AniResult<CharacterDetail> {
        try {
            val result =
                Apollo.apolloClient.query(
                    GetCharacterDetailQuery(characterId),
                )
                    .execute()
            if (result.hasErrors()) {
                return AniResult.Failure(buildString {
                    result.errors?.forEach { appendLine(it.message) }
                })
            }
            val data = result.data?.Character
            if (data != null) {
                return AniResult.Success(parseCharacter(data))
            } else {
                return AniResult.Failure("Network error")
            }
        } catch (exception: ApolloException) {
            return AniResult.Failure(exception.localizedMessage ?: "No exception message given")
        }
    }

    private fun parseCharacter(data: GetCharacterDetailQuery.Character): CharacterDetail {
        val regex = Regex("<span class='markdown_spoiler'>(.*?)</span>")
        val matches = regex.findAll(data.description ?: "")


//        Html.fromHtml(data.description, Html.FROM_HTML_MODE_COMPACT, null, )
        val description = buildString {
            if (!(data.dateOfBirth?.fuzzyDate?.year == null && data.dateOfBirth?.fuzzyDate?.month == null && data.dateOfBirth?.fuzzyDate?.day == null)) {
                append(
                    "<div><strong>Birthday:</strong>${data.dateOfBirth.fuzzyDate.year ?: "?"}-${data.dateOfBirth.fuzzyDate.month ?: "?"}-${data.dateOfBirth.fuzzyDate.day ?: "?"}</div>",
                )
            }
            if (data.age != null) append("<div><strong>Age:</strong>${data.age}</div>")
            if (data.gender != null) append("<div><strong>Gender:</strong>${data.gender}</div>")
            if (data.bloodType != null) append("<div><strong>Blood type:</strong>${data.bloodType}</div>")
            if (data.description != null) {
                append(
                    data.description
                        .substringAfter("<p>")
                        .substringBeforeLast("</p>"),
                )
            }
        }
        return CharacterDetail(
            id = data.id,
            userPreferredName = data.name?.userPreferred ?: "",
            firstName = data.name?.first ?: "",
            middleName = data.name?.middle ?: "",
            lastName = data.name?.last ?: "",
            fullName = data.name?.full ?: "",
            nativeName = data.name?.native ?: "",
            coverImage = data.image?.large ?: "",
            // we take the outer p element out of the description, because otherwise there will be a margin between the blood type and description
            description = description,
            isFavourite = data.isFavourite,
            isFavoriteBlocked = data.isFavouriteBlocked,
            favorites = data.favourites ?: -1,
            voiceActors = parseVoiceActorsForCharacter(data.media),
            relatedMedia = parseMediaCharacter(data.media),
            alternativeNames = data.name?.alternative?.filterNotNull().orEmpty(),
            alternativeSpoilerNames = data.name?.alternativeSpoiler?.filterNotNull().orEmpty(),
        )
    }

    private fun parseVoiceActorsForCharacter(mediaList: GetCharacterDetailQuery.Media?): List<StaffDetail> {
        val result = mutableListOf<StaffDetail>()
        for (media in mediaList?.edges.orEmpty()) {
            for (voiceActor in media?.voiceActorRoles.orEmpty()) {
                result.add(
                    StaffDetail(
                        id = voiceActor?.voiceActor?.id ?: -1,
                        userPreferredName = voiceActor?.voiceActor?.name?.userPreferred ?: "",
                        coverImage = voiceActor?.voiceActor?.image?.large ?: "",
                        language = voiceActor?.voiceActor?.languageV2 ?: "",
                    ),
                )
            }
        }
        return result.distinctBy { it.id }
    }

    private fun parseMediaCharacter(mediaList: GetCharacterDetailQuery.Media?): List<CharacterMediaConnection> {
        val result = mutableListOf<CharacterMediaConnection>()
        for (media in mediaList?.edges.orEmpty()) {
            result.add(
                CharacterMediaConnection(
                    id = media?.node?.id ?: -1,
                    title = media?.node?.title?.userPreferred ?: "",
                    coverImage = media?.node?.coverImage?.extraLarge ?: "",
                    characterRole = media?.characterRole?.name ?: "",
                ),
            )
        }
        return result
    }
}