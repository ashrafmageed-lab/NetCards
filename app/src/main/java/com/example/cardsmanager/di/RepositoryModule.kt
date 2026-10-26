package com.example.cardsmanager.di

import android.content.Context
import com.example.cardsmanager.data.CardsDao
import com.example.cardsmanager.data.CardsRepository
import com.example.cardsmanager.data.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideCardsRepository(dao: CardsDao): CardsRepository {
        return CardsRepository(dao)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context
    ): SettingsRepository {
        return SettingsRepository(context)
    }
}
