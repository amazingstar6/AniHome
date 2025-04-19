package com.kevin.anihome.data.repository.mymedia

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface MyMediaRepositoryModule {
    @Binds
    fun bindMyMediaRepository(myMediaRepositoryImpl: MyMediaRepositoryImpl): MyMediaRepository
}
