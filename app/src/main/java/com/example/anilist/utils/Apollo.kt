package com.example.anilist.utils

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.http.HttpRequest
import com.apollographql.apollo3.api.http.HttpResponse
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.cache.normalized.normalizedCache
import com.apollographql.apollo3.cache.normalized.sql.SqlNormalizedCacheFactory
import com.apollographql.apollo3.cache.normalized.writeToCacheAsynchronously
import com.apollographql.apollo3.network.http.HttpInterceptor
import com.apollographql.apollo3.network.http.HttpInterceptorChain
import com.example.anilist.MainActivity
import com.example.anilist.data.models.AniNotification
import timber.log.Timber

private const val TAG = "Apollo"

class Apollo {
    companion object Client {
        private var accessCode: String = MainActivity.accessCode

        // Android specific
        private val sqlNormalizedCacheFactory = SqlNormalizedCacheFactory("apollo.db")

        // Creates a 10MB MemoryCacheFactory
        private const val ONE_HOUR_IN_MILLI_SECONDS: Long = 3600000L
        private val cacheFactory = MemoryCacheFactory(
            maxSizeBytes = 10 * 1024 * 1024,
            ONE_HOUR_IN_MILLI_SECONDS,
        )

//        private var headers: List<HttpHeader> = if (accessCode != "") listOf(
//            HttpHeader(
//                "Authorization",
//                "Bearer $accessCode"
//            )
//        ) else emptyList()

        /**
         * Uses cache first
         */
        val apolloClient =
            ApolloClient.Builder()
                .normalizedCache(cacheFactory.chain(sqlNormalizedCacheFactory))
                .serverUrl("https://graphql.anilist.co")
                .fetchPolicy(FetchPolicy.CacheFirst)
                .writeToCacheAsynchronously(true)
                .addHttpInterceptor(AuthorizationInterceptor(accessCode))
                .build()

//        val apolloClientNetworkFirst = ApolloClient.Builder()
//            .normalizedCache(cacheFactory.chain(sqlNormalizedCacheFactory))
//            .serverUrl("https://graphql.anilist.co")
//            .addHttpInterceptor(AuthorizationInterceptor(accessCode))
//            .fetchPolicy(FetchPolicy.NetworkFirst)
//            .build()

        // todo function does not work, we take it directly from mainActivity in the above snippet anyway, that's why it sitll works
        fun setAccessCode(newAccessCode: String) {
//            Log.d(TAG, "new access code is $newAccessCode")
//            accessCode = newAccessCode
//            headers = listOf(
//                HttpHeader(
//                    "Authorization",
//                    "Bearer $accessCode"
//                )
//            )
//            Log.d(TAG, "Done")
        }
    }
}

class AuthorizationInterceptor(val token: String) : HttpInterceptor {
    override suspend fun intercept(
        request: HttpRequest,
        chain: HttpInterceptorChain
    ): HttpResponse {
        Timber.d("WERE INTERCEPTING with token $token")
        return if (token != "") {
            chain.proceed(
                request.newBuilder().addHeader("Authorization", "Bearer $token").build()
            )
        } else {
            chain.proceed(request)
        }
    }
}


enum class ResultStatus {
    SUCCESSFUL,
    NO_DATA,
    ERROR,
}

/**
 * Data is never null when status is successful
 */
data class ResultData(
    val status: ResultStatus,
    val errorMessage: String = "",
    val data: List<AniNotification>? = null,
)
