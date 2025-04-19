package com.example.anilist.data.repository

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.exception.ApolloException
import com.example.anilist.GetCurrentUserQuery
import com.example.anilist.data.models.AniResult
import timber.log.Timber
import javax.inject.Inject

class MainActivityRepository
    @Inject
    constructor() {
        suspend fun getUserId(accessCode: String): AniResult<Int> {
            Timber.d("requesting user id is starting")
            return try {
                // fixme temp solution for now, access code should be inject through Apollo clas interceptor
                val response =
                    ApolloClient.Builder()
                        .serverUrl("https://graphql.anilist.co")
                        .fetchPolicy(FetchPolicy.NetworkOnly)
                        .addHttpHeader("Authorization", "Bearer $accessCode").build()
                        .query(GetCurrentUserQuery()).execute()
                val data = response.data
                if (data?.Viewer?.id != null) {
                    AniResult.Success(data.Viewer.id)
                } else {
                    AniResult.Failure("Network error")
                }
            } catch (e: ApolloException) {
                AniResult.Failure(e.localizedMessage ?: "No exception message given")
            }
        }
    }
