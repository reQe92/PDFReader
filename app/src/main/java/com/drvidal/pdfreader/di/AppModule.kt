package com.drvidal.pdfreader.di

import android.content.Context
import com.drvidal.pdfreader.repository.AnalyticsRepository
import com.drvidal.pdfreader.repository.FileListRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideFileListRepository(@ApplicationContext context: Context, analyticsRepository: AnalyticsRepository) : FileListRepository {
        return FileListRepository(context, analyticsRepository)
    }

    @Singleton
    @Provides
    fun provideAnalyticsRepository() : AnalyticsRepository {
        return AnalyticsRepository()
    }
}