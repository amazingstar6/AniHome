package com.example.anilist

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.http.HttpHeader
import com.apollographql.apollo3.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo3.cache.normalized.normalizedCache
import com.example.anilist.data.models.Notification

class Apollo {
    companion object Client {
        // Creates a 10MB MemoryCacheFactory
        // todo store cache in sql database
        private const val ONE_HOUR_IN_MILLI_SECONDS: Long = 3600000L
        private val cacheFactory = MemoryCacheFactory(
            maxSizeBytes = 10 * 1024 * 1024,
            ONE_HOUR_IN_MILLI_SECONDS,
        )
        val apolloClient =
            ApolloClient.Builder().httpHeaders(
                listOf(
                    HttpHeader(
                        "Authorization",
                        "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6IjI0MzFmYjM2MjMwNWFiM2Y2NzI5MmNlMGY4M2RmOGM0MmM1NGI0NmEyYmI5NjNlN2ZiODA1YzI1YTU0ZGU4ZjJmZTk5NWUxMDEwNGRiNWUxIn0.eyJhdWQiOiIxMzYxNiIsImp0aSI6IjI0MzFmYjM2MjMwNWFiM2Y2NzI5MmNlMGY4M2RmOGM0MmM1NGI0NmEyYmI5NjNlN2ZiODA1YzI1YTU0ZGU4ZjJmZTk5NWUxMDEwNGRiNWUxIiwiaWF0IjoxNjg5NzYwNTQ3LCJuYmYiOjE2ODk3NjA1NDcsImV4cCI6MTcyMTM4Mjk0Nywic3ViIjoiODA4NjI2Iiwic2NvcGVzIjpbXX0.RD0LdCD8AmzNlJM_OIWbrPz-Ec9RBKZrtGE0vWhvM7G9cs5vWf4WF3QGrd0P5k5-YZJB_Cr7YJCe8mV_n2B0yHm3Ia0kde7gdRx1V9aaXDRNH-MNidYjVq-RuVLfkI-bgw82vGXQ42Y_dhFZypJiYdh2SYIY09OWgNqwvxLu-D-EYVJMBEdsWbd6RRJdKzyCQ0EMsUxgmBCgHuMt2KghA5FMhTj_eWzT30rEs1ziREGTpIz_aJS-pHed8husWF-WhwC0YY0r0NXbuge--tpGvGd8ShJb2AQ0lDvQ7JomvFlkqEUXZf7jC_rQqeLApKqnx-iwCTy-0JMDLuRGHkvXtn6pGCgdFwySTLfoMYInXX3vuYMxlfuheINkkx2qL5n51PlMRXRaADfH_C2jmFFU5fNLHFSmTE_tvVMMdflEmQWwt1htz1g-EZp9wGMC88j56fXpoNeI4htppkOWn5mLioyZFoX5hqD7zPu3yQd6A9cistkQWvz0VBIOGQH0d-5eKlVkHIQpP289cx3ho2abxBmilDQsF0RhOueLaSPHtsuyuvOie-gcvmQUPYuMEkwUpEvyxrXqg976YwTGCcN1Rl6BG4RhCgj5ngF3HhPTj4HSO1X5-gynKLcL3S61yjD6je3WecFrYqj8xStZekffuYcD0TKyAOYE0BQndC7xQTg",
                    ),
                ),
            )
//                .normalizedCache(cacheFactory)
                .serverUrl("https://graphql.anilist.co").build()

//        suspend fun<D:Query.Data> executeQuery(call: ApolloCall<D>): ResultData {
//            try {
//                val response = apolloClient.query(query).execute()
//            } catch (exception: ApolloException) {
//                // handle exception here,, these are mainly for network errors
//                return ResultData(ResultStatus.ERROR, exception.message ?: "No error message")
//            }
//            val result = apolloClient.query(query).execute()
//            if (result.hasErrors()) {
//                // these errors are related to GraphQL errors
//                return ResultData(ResultStatus.ERROR, result.errors.toString())
//            }
//            val data = result.data
//            return if (data == null) ResultData(ResultStatus.NO_DATA) else ResultData(
//                ResultStatus.SUCCESSFUL,
//                data = data
//            )
//        }
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
    val data: List<Notification>? = null,
)
