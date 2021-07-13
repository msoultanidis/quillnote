package org.qosp.notes.di

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import org.qosp.notes.data.repo.NotebookRepository
import org.qosp.notes.data.sync.local.LocalFilesAPI
import org.qosp.notes.data.sync.local.LocalSyncProvider
import org.qosp.notes.data.sync.nextcloud.NextcloudAPI
import org.qosp.notes.data.sync.nextcloud.NextcloudManager
import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalSyncProviderModule {

    @Provides
    @Singleton
    fun provideLocalFilesAPI(@ApplicationContext context: Context): LocalFilesAPI {
        return LocalFilesAPI(context)
    }

    @Provides
    @Singleton
    fun provideLocalSyncProvider(
        localFilesAPI: LocalFilesAPI,
        @Named(NO_SYNC) notebookRepository: NotebookRepository,
    ) = LocalSyncProvider(localFilesAPI, notebookRepository)
}
