package com.niran.ktornoteapplication.dataset.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import java.util.*

@Entity(tableName = "note_table")
data class Note(
    val title: String,
    val content: String,
    val date: Long,
    val owners: List<String>,
    val color: String,

    @Expose(serialize = false, deserialize = false)
    val isSynced: Boolean = false,

    @PrimaryKey(autoGenerate = false)
    val id: String = UUID.randomUUID().toString()
)