package org.qosp.notes.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Entity(tableName = "notebooks")
@Serializable
@Parcelize
data class Notebook(
    @ColumnInfo(name = "notebookName")
    val name: String,
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L
) : Parcelable
