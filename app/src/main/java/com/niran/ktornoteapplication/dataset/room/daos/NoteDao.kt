package com.niran.ktornoteapplication.dataset.room.daos

import androidx.room.*
import com.niran.ktornoteapplication.dataset.models.LocallyDeletedNoteId
import com.niran.ktornoteapplication.dataset.models.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<Note>)

    @Query("DELETE FROM note_table WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: String)

    @Query("DELETE FROM note_table WHERE isSynced = 1")
    suspend fun deleteAllSyncedNotes()

    @Query("DELETE FROM note_table")
    suspend fun deleteAllNotes()

    @Query("SELECT * FROM note_table WHERE id = :noteId")
    fun collectNoteById(noteId: String): Flow<Note?>?

    @Query("SELECT * FROM note_table WHERE id = :noteId")
    suspend fun getNoteById(noteId: String): Note?

    @Query("SELECT * FROM note_table ORDER BY date DESC")
    fun collectAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM note_table WHERE isSynced = 0")
    suspend fun getAllUnSyncedNotes(): List<Note>

    @Query("SELECT deleted_note_id FROM locally_deleted_note_id_table")
    suspend fun getAllLocallyDeletedNoteIds(): List<String>

    @Query("DELETE FROM locally_deleted_note_id_table WHERE deleted_note_id = :deletedNoteId")
    suspend fun deleteLocallyDeletedNoteId(deletedNoteId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocallyDeletedNoteId(locallyDeletedNoteId: LocallyDeletedNoteId)
}