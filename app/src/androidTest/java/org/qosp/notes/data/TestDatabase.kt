package org.qosp.notes.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider

interface UsesTestDatabase

val UsesTestDatabase.database by lazy {
    val context = ApplicationProvider.getApplicationContext<Context>()
    Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
}
