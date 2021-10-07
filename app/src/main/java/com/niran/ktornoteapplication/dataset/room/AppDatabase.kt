package com.niran.ktornoteapplication.dataset.room

import androidx.room.*
import com.niran.ktornoteapplication.dataset.models.LocallyDeletedNoteId
import com.niran.ktornoteapplication.dataset.models.Note
import com.niran.ktornoteapplication.dataset.room.converters.StringListConverter
import com.niran.ktornoteapplication.dataset.room.daos.NoteDao

@Database(entities = [Note::class, LocallyDeletedNoteId::class], version = 1, exportSchema = false)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract val noteDao: NoteDao

}