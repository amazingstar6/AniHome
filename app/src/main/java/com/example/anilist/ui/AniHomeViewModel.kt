package com.example.anilist.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.api.http.HttpHeader
import com.example.anilist.GetAnimeInfoQuery
import com.example.anilist.GetMyAnimeQuery
import com.example.anilist.GetTrendsQuery
import com.example.anilist.data.Anime
import com.example.anilist.data.Character
import com.example.anilist.data.Link
import com.example.anilist.data.Relation
import com.example.anilist.data.Tag
import com.example.anilist.type.MediaSeason
import com.example.anilist.type.MediaSort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

private const val TAG = "AniHomeViewModel"

class AniHomeViewModel : ViewModel() {

    private val accessToken: String = "" //todo
    private val apolloClient =
        ApolloClient.Builder().serverUrl("https://graphql.anilist.co").build()
    private val _uiState = MutableStateFlow(AniHomeUiState())

    init {
        loadTrendingAnime()
        loadPopularAnime()
        loadUpcomingNextSeason()
        loadAllTimePopular()
        loadTop100Anime()
    }

    val uiState: StateFlow<AniHomeUiState> = _uiState.asStateFlow()

    fun loadTrendingAnime(increasePage: Boolean = false) {
        if (increasePage) {
            _uiState.update { currentState ->
                currentState.copy(trendingPage = currentState.trendingPage.inc())
            }
        }
        viewModelScope.launch {
            val response =
                apolloClient.query(
                    GetTrendsQuery(
                        Optional.Present(_uiState.value.trendingPage),
                        Optional.Present(
                            listOf(MediaSort.TRENDING_DESC)
                        )
                    )
                ).execute()
            val data = response.data?.trending?.media
//            if (data == null) Log.i(TAG, "Trending data list is empty!")
            _uiState.update { currentState ->
                currentState.copy(
                    trendingAnime = currentState.trendingAnime + parseMediaHome(
                        data?.filterNotNull()
                            .orEmpty()
                    )
                )
            }
        }
    }

    private fun parseMediaHome(medias: List<GetTrendsQuery.Medium>): List<Anime> {
        val mediaList: MutableList<Anime> = mutableListOf()
        for (media in medias) {
            if (media.title?.userPreferred != null && media.coverImage?.extraLarge != null) {
                mediaList.add(
                    Anime(
                        id = media.id,
                        title = media.title.userPreferred,
                        coverImage = media.coverImage?.extraLarge
                    )
                )
            }
        }
        return mediaList
    }

    fun loadPopularAnime(increasePage: Boolean = false) {
        if (increasePage) {
            _uiState.update { currentState ->
                currentState.copy(popularPage = currentState.popularPage.inc())
            }
        }
        val year = Calendar.getInstance().get(Calendar.YEAR)
        val month = Calendar.getInstance().get(Calendar.MONTH)
        val season: MediaSeason
        when (month) {
            Calendar.JANUARY -> season = MediaSeason.WINTER
            Calendar.FEBRUARY -> season = MediaSeason.WINTER
            Calendar.MARCH -> season = MediaSeason.WINTER

            Calendar.APRIL -> season = MediaSeason.SPRING
            Calendar.MAY -> season = MediaSeason.SPRING
            Calendar.JUNE -> season = MediaSeason.SPRING

            Calendar.JULY -> season = MediaSeason.SUMMER
            Calendar.AUGUST -> season = MediaSeason.SUMMER
            Calendar.SEPTEMBER -> season = MediaSeason.SUMMER

            Calendar.OCTOBER -> season = MediaSeason.FALL
            Calendar.NOVEMBER -> season = MediaSeason.FALL
            Calendar.DECEMBER -> season = MediaSeason.FALL

            else -> season = MediaSeason.UNKNOWN__
        }

        viewModelScope.launch {
            val response =
                apolloClient.query(
                    GetTrendsQuery(
                        Optional.Present(_uiState.value.popularPage),
                        Optional.Present(listOf(MediaSort.POPULARITY_DESC)),
                        Optional.Present(season),
                        Optional.Present(year)
                    )
                ).execute()
            _uiState.update { currentState ->
                currentState.copy(
                    popularAnime = currentState.popularAnime + parseMediaHome(
                        response.data?.trending?.media?.filterNotNull()
                            .orEmpty()
                    )
                )
            }
        }
    }

    fun loadUpcomingNextSeason(increasePage: Boolean = false) {
        if (increasePage) {
            _uiState.update { currentState ->
                currentState.copy(upcomingNextSeasonPage = currentState.upcomingNextSeasonPage.inc())
            }
        }
        val month = Calendar.getInstance().get(Calendar.MONTH)
        val season: MediaSeason
        // using the next season, since we're loading the upcoming season
        when (month) {
            Calendar.JANUARY -> season = MediaSeason.SPRING
            Calendar.FEBRUARY -> season = MediaSeason.SPRING
            Calendar.MARCH -> season = MediaSeason.SPRING

            Calendar.APRIL -> season = MediaSeason.SUMMER
            Calendar.MAY -> season = MediaSeason.SUMMER
            Calendar.JUNE -> season = MediaSeason.SUMMER

            Calendar.JULY -> season = MediaSeason.FALL
            Calendar.AUGUST -> season = MediaSeason.FALL
            Calendar.SEPTEMBER -> season = MediaSeason.FALL

            Calendar.OCTOBER -> season = MediaSeason.WINTER
            Calendar.NOVEMBER -> season = MediaSeason.WINTER
            Calendar.DECEMBER -> season = MediaSeason.WINTER

            else -> season = MediaSeason.UNKNOWN__
        }
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val year = if (season == MediaSeason.WINTER) currentYear + 1 else currentYear

        viewModelScope.launch {
            val response =
                apolloClient.query(
                    GetTrendsQuery(
                        Optional.Present(_uiState.value.upcomingNextSeasonPage),
                        Optional.Present(listOf(MediaSort.POPULARITY_DESC)),
                        Optional.Present(season),
                        Optional.Present(year)
                    )
                ).execute()
            _uiState.update { currentState ->
                currentState.copy(
                    upcomingNextSeason = currentState.upcomingNextSeason + parseMediaHome(
                        response.data?.trending?.media?.filterNotNull()
                            .orEmpty()
                    )
                )
            }
        }
    }

    fun loadAllTimePopular(increasePage: Boolean = true) {
        if (increasePage) {
            _uiState.update { currentState ->
                currentState.copy(allTimePopularPage = currentState.allTimePopularPage.inc())
            }
        }
        viewModelScope.launch {
            val response =
                apolloClient.query(
                    GetTrendsQuery(
                        Optional.Present(_uiState.value.allTimePopularPage),
                        Optional.Present(listOf(MediaSort.POPULARITY_DESC)),
                    )
                ).execute()
            _uiState.update { currentState ->
                currentState.copy(
                    allTimePopular = currentState.allTimePopular + parseMediaHome(
                        response.data?.trending?.media?.filterNotNull()
                            .orEmpty()
                    )
                )
            }
        }
    }

    fun loadTop100Anime(increasePage: Boolean = false) {
        if (increasePage) {
            _uiState.update { currentState ->
                currentState.copy(top100AnimePage = currentState.top100AnimePage.inc())
            }
        }
        viewModelScope.launch {
            val response =
                apolloClient.query(
                    GetTrendsQuery(
                        Optional.Present(_uiState.value.top100AnimePage),
                        Optional.Present(listOf(MediaSort.SCORE_DESC)),
                    )
                ).execute()
            _uiState.update { currentState ->
                currentState.copy(
                    top100Anime = currentState.top100Anime + parseMediaHome(
                        response.data?.trending?.media?.filterNotNull()
                            .orEmpty()
                    )
                )

            }
        }
    }

    fun getAnimeDetails(id: Int) {
        viewModelScope.launch {
            val response =
                apolloClient.query(GetAnimeInfoQuery(id)).execute()
            _uiState.update { currentState ->
                currentState.copy(
                    currentDetailAnime = parseMedia(response.data?.Media),
                    currentDetailCharacters = parseCharacters(response.data?.Media)
                )
            }
        }
    }

    private fun parseCharacters(anime: GetAnimeInfoQuery.Media?): List<Character> {
        val languages: MutableList<String> = mutableListOf()
        val characters: MutableList<Character> = mutableListOf()
        for (character in anime?.characters?.edges.orEmpty()) {
            for (voiceActor in character?.voiceActors.orEmpty()) {
                if (languages.contains(voiceActor?.languageV2) && voiceActor?.languageV2 != null) {
                    languages.add(voiceActor.languageV2)
                }
                if (character != null && voiceActor != null) {
                    characters.add(
                        Character(
                            id = character.node?.id ?: 0,
                            name = character.node?.name?.native ?: "",
                            coverImage = character.node?.image?.large ?: "",
                            voiceActorName = voiceActor.name?.native ?: "",
                            voiceActorCoverImage = voiceActor.image?.large ?: "",
                            voiceActorLanguage = voiceActor.languageV2 ?: ""
                        )
                    )
                }
            }
        }
        return characters
    }

    private fun parseMedia(anime: GetAnimeInfoQuery.Media?): Anime {
        val tags: MutableList<Tag> = mutableListOf()
        for (tag in anime?.tags.orEmpty()) {
            if (tag != null) {
                tags.add(Tag(tag.name, tag.rank ?: 0, tag.isMediaSpoiler ?: true))
            }
        }
        val synonyms = buildString {
            for (synonym in anime?.synonyms.orEmpty()) {
                append(synonym)
                if (anime?.synonyms?.last() != synonym) {
                    append("\n")
                }
            }
        }
        val genres: MutableList<String> = mutableListOf()
        for (genre in anime?.genres.orEmpty()) {
            if (genre != null) {
                genres.add(genre)
            }
        }
        val externalLinks: MutableList<Link> = mutableListOf()
        for (link in anime?.externalLinks.orEmpty()) {
            if (link != null) {
                externalLinks.add(
                    Link(
                        link.url ?: "",
                        link.site,
                        link.language ?: "",
                        link.color ?: "",
                        link.icon ?: ""
                    )
                )
            }
        }
        val relations: MutableList<Relation> = mutableListOf()
        for (relation in anime?.relations?.edges.orEmpty()) {
            relations.add(
                Relation(
                    id = relation?.node?.id ?: 0,
                    coverImage = relation?.node?.coverImage?.extraLarge ?: "",
                    title = relation?.node?.title?.native ?: "",
                    relation = relation?.relationType?.rawValue ?: ""
                )
            )
        }
        return Anime(title = anime?.title?.native ?: "Unknown",
            coverImage = anime?.coverImage?.extraLarge ?: "",
            format = anime?.format?.name ?: "Unknown",
            seasonYear = anime?.seasonYear.toString(),
            episodeAmount = anime?.episodes ?: 0,
            averageScore = anime?.averageScore ?: 0,
            tags = tags,
            description = anime?.description ?: "No description found",
            relations = relations,
            infoList = mapOf("format" to anime?.format?.name.orEmpty(),
                "status" to anime?.status?.name?.lowercase()
                    ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    .orEmpty(),
                "startDate" to if (anime?.startDate != null) "${anime.startDate.day}-${anime.startDate.month}-${anime.startDate.year}" else "Unknown",
                "endDate" to if (anime?.endDate?.year != null && anime.endDate.month != null && anime.endDate.day != null) "${anime.endDate.day}-${anime.endDate.month}-${anime.endDate.year}" else "Unknown",
                "duration" to anime?.duration.toString(),
                "country" to anime?.countryOfOrigin.toString(),
                "source" to (anime?.source?.rawValue ?: "Unknown"),
                "hashtag" to (anime?.hashtag ?: "Unknown"),
                "licensed" to anime?.isLicensed.toString(),
                "updatedAt" to anime?.updatedAt.toString(),
                "synonyms" to synonyms,
                "nsfw" to anime?.isAdult.toString()),
            genres = genres,
            trailerImage = anime?.trailer?.thumbnail ?: "",
            // todo add dailymotion
            trailerLink = if (anime?.trailer?.site == "youtube") "https://www.youtube.com/watch?v=${anime.trailer.id}" else if (anime?.trailer?.site == "dailymotion") "" else "",
            externalLinks = externalLinks)
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(TAG, "AniHomeViewModel was just cleared!")
    }

    fun loadMyAnime() {
        viewModelScope.launch {
            val response =
                apolloClient.query(
                    GetMyAnimeQuery(
                    )
                ).httpHeaders(listOf(HttpHeader("Authorization", "Bearer $accessToken"))).execute()
            _uiState.update { currentState ->
                currentState.copy(
                    personalAnimeList = emptyList() //todo
                )
            }
        }
    }
}