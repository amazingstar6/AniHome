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
        private val myAccessCode =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6IjI0MzFmYjM2MjMwNWFiM2Y2NzI5MmNlMGY4M2RmOGM0MmM1NGI0NmEyYmI5NjNlN2ZiODA1YzI1YTU0ZGU4ZjJmZTk5NWUxMDEwNGRiNWUxIn0.eyJhdWQiOiIxMzYxNiIsImp0aSI6IjI0MzFmYjM2MjMwNWFiM2Y2NzI5MmNlMGY4M2RmOGM0MmM1NGI0NmEyYmI5NjNlN2ZiODA1YzI1YTU0ZGU4ZjJmZTk5NWUxMDEwNGRiNWUxIiwiaWF0IjoxNjg5NzYwNTQ3LCJuYmYiOjE2ODk3NjA1NDcsImV4cCI6MTcyMTM4Mjk0Nywic3ViIjoiODA4NjI2Iiwic2NvcGVzIjpbXX0.RD0LdCD8AmzNlJM_OIWbrPz-Ec9RBKZrtGE0vWhvM7G9cs5vWf4WF3QGrd0P5k5-YZJB_Cr7YJCe8mV_n2B0yHm3Ia0kde7gdRx1V9aaXDRNH-MNidYjVq-RuVLfkI-bgw82vGXQ42Y_dhFZypJiYdh2SYIY09OWgNqwvxLu-D-EYVJMBEdsWbd6RRJdKzyCQ0EMsUxgmBCgHuMt2KghA5FMhTj_eWzT30rEs1ziREGTpIz_aJS-pHed8husWF-WhwC0YY0r0NXbuge--tpGvGd8ShJb2AQ0lDvQ7JomvFlkqEUXZf7jC_rQqeLApKqnx-iwCTy-0JMDLuRGHkvXtn6pGCgdFwySTLfoMYInXX3vuYMxlfuheINkkx2qL5n51PlMRXRaADfH_C2jmFFU5fNLHFSmTE_tvVMMdflEmQWwt1htz1g-EZp9wGMC88j56fXpoNeI4htppkOWn5mLioyZFoX5hqD7zPu3yQd6A9cistkQWvz0VBIOGQH0d-5eKlVkHIQpP289cx3ho2abxBmilDQsF0RhOueLaSPHtsuyuvOie-gcvmQUPYuMEkwUpEvyxrXqg976YwTGCcN1Rl6BG4RhCgj5ngF3HhPTj4HSO1X5-gynKLcL3S61yjD6je3WecFrYqj8xStZekffuYcD0TKyAOYE0BQndC7xQTg"

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
