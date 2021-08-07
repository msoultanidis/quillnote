package org.qosp.notes.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// version 1.3.0
object MIGRATION_1_2 : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            // Migrate from old id_mappings to new
            """
                CREATE TABLE id_mappings (
                    localNoteId INTEGER,
                    remoteNoteId INTEGER NOT NULL,
                    provider TEXT NOT NULL,
                    extras TEXT,
                    PRIMARY KEY(remoteNoteId, provider)
                )
            """.trimIndent()
        )

        database.execSQL(
            """
                INSERT INTO id_mappings (localNoteId, remoteNoteId, provider, extras)
                SELECT localNoteId, remoteNoteId, provider, extras FROM cloud_ids
                WHERE remoteNoteId IS NOT NULL AND provider IS NOT NULL
            """.trimIndent()
        )

        database.execSQL(
            """
                DROP TABLE cloud_ids
            """.trimIndent()
        )

        // Add modifiedStrictDate to Note table
        database.execSQL("ALTER TABLE notes ADD COLUMN modifiedDateStrict INTEGER")
        database.execSQL("UPDATE notes SET modifiedDateStrict = modifiedDate")
    }
}
