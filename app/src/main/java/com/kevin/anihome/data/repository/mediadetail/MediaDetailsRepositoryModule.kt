package com.kevin.anihome.data.repository.mediadetail

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface MediaDetailsRepositoryModule {
    @Binds
    fun bindHomeRepository(homeRepositoryImpl: MediaDetailsRepositoryImpl): MediaDetailsRepository
}
