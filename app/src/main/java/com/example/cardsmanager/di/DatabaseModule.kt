package com.example.cardsmanager.di

import android.content.Context
import com.example.cardsmanager.data.CardsDao
import com.example.cardsmanager.data.CardsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @Provides
    @Singleton
    fun provideCardsDatabase(
        @ApplicationContext context: Context,
        scope: CoroutineScope
    ): CardsDatabase {
        return CardsDatabase.getDatabase(context, scope)
    }

    @Provides
    @Singleton
    fun provideCardsDao(database: CardsDatabase): CardsDao {
        return database.cardsDao()
    }
}
