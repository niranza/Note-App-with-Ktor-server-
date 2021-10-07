package com.niran.ktornoteapplication.dataset.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locally_deleted_note_id_table")
data class LocallyDeletedNoteId(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "deleted_note_id")
    val deletedNoteId: String
)