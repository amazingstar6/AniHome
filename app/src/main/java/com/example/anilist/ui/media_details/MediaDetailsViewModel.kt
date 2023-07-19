package com.example.anilist.ui.media_details

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo3.cache.normalized.normalizedCache
import com.example.anilist.GetAnimeInfoQuery
import com.example.anilist.data.repository.MediaRepository
import com.example.anilist.data.models.Anime
import com.example.anilist.data.models.Link
import com.example.anilist.data.models.Relation
import com.example.anilist.data.models.Tag
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

//data class MediaDetailsUiState(
//    val Anime: Anime? = null,
//)

private const val TAG = "MediaDetailsViewModel"

class MediaDetailsViewModel(
//    private val mediaRepository: MediaRepository
) : ViewModel() {

    // Creates a 10MB MemoryCacheFactory
    val cacheFactory = MemoryCacheFactory(maxSizeBytes = 10 * 1024 * 1024)
    // Build the ApolloClient
    val apolloClient = ApolloClient.Builder()
        .serverUrl("https://graphql.anilist.co")
        // normalizedCache() is an extension function on ApolloClient.Builder
        .normalizedCache(cacheFactory)
        .build()

    private val _mediaId = MutableLiveData<Int>()

    private val _media: MutableLiveData<Anime> by lazy {
        MutableLiveData<Anime>().apply {
//            if (cacheList.any { it.id == _mediaId.value }) {
//                _media.value = cacheList.find { it.id == _mediaId.value }
//                Log.i(TAG, "We just used some cache! #4")
//            } else {
                viewModelScope.launch {
                    val response =
                        apolloClient.query(GetAnimeInfoQuery(_mediaId.value ?: 0)).execute()
//                    val media = Anime(
//                        id = _mediaId.value ?: 0,
//                        title = response.data?.Media?.title?.native ?: ""
//                    )
                    val media = parseMedia(response.data?.Media)
//                    cacheList.add(media)
                    _media.value = media
                    Log.i(
                        TAG,
                        "We did not use cache for media with id: ${_mediaId.value} #5; $cacheList"
                    )
//                }
            }
        }
    }

    private val cacheList: MutableList<Anime> = mutableListOf()

//
//    private val _media: MutableLiveData<Anime> = _mediaId.switchMap {
//        it ->
//
//    }

    val media: LiveData<Anime> = _media

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    fun start(mediaId: Int) {
        // If we're already loading or already loaded, return (might be a config change)
        if (_dataLoading.value == true || mediaId == _mediaId.value) {
            return
        }
        // Trigger the load
        _mediaId.value = mediaId
        viewModelScope.launch {
            val response =
                apolloClient.query(GetAnimeInfoQuery(_mediaId.value ?: 0)).execute()
            _media.value = parseMedia(response.data?.Media)
        }
    }

    fun refresh() {
        _media.value?.let {
            _dataLoading.value = true
            viewModelScope.launch {
                val response =
                    ApolloClient.Builder().serverUrl("https://graphql.anilist.co").build()
                        .query(GetAnimeInfoQuery(_mediaId.value ?: 0)).execute()
                _media.value = Anime(title = response.data?.Media?.title?.native ?: "")
                _dataLoading.value = false
            }
        }
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
}