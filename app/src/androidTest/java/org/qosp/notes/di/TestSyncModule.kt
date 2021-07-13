package org.qosp.notes.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.GlobalScope
import org.qosp.notes.App
import org.qosp.notes.data.repo.IdMappingRepository
import org.qosp.notes.data.repo.NoteRepository
import org.qosp.notes.data.sync.core.ConfigFactory
import org.qosp.notes.data.sync.core.SyncActor
import org.qosp.notes.data.sync.core.SyncManager
import org.qosp.notes.data.sync.local.LocalSyncProvider
import org.qosp.notes.data.sync.nextcloud.NextcloudManager
import org.qosp.notes.preferences.PreferenceRepository
import org.qosp.notes.ui.utils.ConnectionManager
import javax.inject.Named
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [SyncModule::class],
)
object TestSyncModule {
    @Provides
    @Singleton
    fun provideSyncManager(
        @ApplicationContext context: Context,
        syncActor: SyncActor,
        preferenceRepository: PreferenceRepository,
        nextcloudManager: NextcloudManager,
        localSyncProvider: LocalSyncProvider,
        configFactory: ConfigFactory,
    ) = SyncManager(
        nextcloudManager,
        localSyncProvider,
        configFactory,
        ConnectionManager(context),
        GlobalScope,
        preferenceRepository,
        syncActor,
    )

    @Provides
    @Singleton
    fun provideSyncActor(
        @Named(NO_SYNC) noteRepository: NoteRepository,
        idMappingRepository: IdMappingRepository,
    ) = SyncActor(noteRepository, idMappingRepository)

    @Provides
    @Singleton
    fun provideConfigFactory(
        @ApplicationContext context: Context,
        preferencesRepository: PreferenceRepository,
    ) = ConfigFactory(context, preferencesRepository)
}
