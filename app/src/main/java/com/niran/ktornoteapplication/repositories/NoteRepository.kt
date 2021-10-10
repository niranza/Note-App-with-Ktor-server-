package com.niran.ktornoteapplication.repositories

import android.app.Application
import com.niran.ktornoteapplication.R
import com.niran.ktornoteapplication.dataset.models.LocallyDeletedNoteId
import com.niran.ktornoteapplication.dataset.models.Note
import com.niran.ktornoteapplication.dataset.retrofit.apis.NoteApi
import com.niran.ktornoteapplication.dataset.retrofit.requests.AccountRequest
import com.niran.ktornoteapplication.dataset.retrofit.requests.AddOwnerToNoteRequest
import com.niran.ktornoteapplication.dataset.retrofit.requests.DeleteNoteRequest
import com.niran.ktornoteapplication.dataset.room.daos.NoteDao
import com.niran.ktornoteapplication.utils.InternetUtils.hasInternetConnection
import com.niran.ktornoteapplication.utils.Resource
import com.niran.ktornoteapplication.utils.networkBoundResource
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

class NoteRepository @Inject constructor(
    private val dao: NoteDao,
    private val api: NoteApi,
    private val app: Application
) {

    suspend fun insertNote(note: Note) {

        val response = try {
            api.addNote(note)
        } catch (e: Exception) {
            null
        }

        if (response != null && response.isSuccessful)
            dao.insertNote(note.copy(isSynced = true))
        else dao.insertNote(note.copy(isSynced = false))
    }

//    private var currentNotesResponse: Response<List<Note>>? = null

    suspend fun syncNotes() {
        dao.getAllLocallyDeletedNoteIds().forEach { noteId -> deleteNote(noteId) }
        dao.getAllUnSyncedNotes().forEach { note -> insertNote(note) }
//        currentNotesResponse = api.getNotes()
//        currentNotesResponse?.body()?.let { notes ->
//            dao.deleteAllNotes()
//            dao.insertNotes(notes.map { it.copy(isSynced = true) })
//        }
    }

    fun getAllNotes(): Flow<Resource<List<Note>>> = networkBoundResource(
        query = {
            dao.collectAllNotes()
        },
        fetch = {
            syncNotes()
//            currentNotesResponse
            api.getNotes()
        },
        saveFetchResult = { response ->
            response.body()?.let { noteList ->
                dao.deleteAllNotes()
                dao.insertNotes(noteList.map { it.copy(isSynced = true) })
            }
        },
        shouldFetch = {
            app.hasInternetConnection()
        }
    )

    suspend fun deleteNote(noteId: String) {
        val response = try {
            api.deleteNote(DeleteNoteRequest(noteId))
        } catch (e: Exception) {
            null
        }

        if (response != null && response.isSuccessful) {
            dao.deleteLocallyDeletedNoteId(noteId)
        } else {
            dao.insertLocallyDeletedNoteId(LocallyDeletedNoteId(noteId))
        }

        dao.deleteNoteById(noteId)
    }

    suspend fun deleteAllCachedNotes() = dao.deleteAllNotes()

    fun collectNoteById(noteId: String) = dao.collectNoteById(noteId)

    suspend fun deleteLocallyDeletedNoteId(deletedNoteId: String) =
        dao.deleteLocallyDeletedNoteId(deletedNoteId)

    suspend fun getNoteById(noteId: String) = dao.getNoteById(noteId)

    suspend fun register(email: String, password: String): Resource<String> = with(app) {
        try {
            val response = api.register(AccountRequest(email, password))
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    if (body.successful) return@with Resource.Success(body.message)
                }
                return@with Resource.Error(
                    response.body()?.message ?: getString(R.string.problem_with_server)
                )
            } else return@with Resource.Error(response.message())
        } catch (e: Exception) {
            Timber.e("Error -> ${e.message}")
            return@with Resource.Error(getString(R.string.could_not_connect_to_server))
        }
    }

    suspend fun login(email: String, password: String): Resource<String> = with(app) {
        try {
            val response = api.login(AccountRequest(email, password))
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    if (body.successful) return@with Resource.Success(body.message)
                }
                return@with Resource.Error(
                    response.body()?.message ?: getString(R.string.problem_with_server)
                )
            } else return@with Resource.Error(response.message())
        } catch (e: Exception) {
            return@with Resource.Error(getString(R.string.could_not_connect_to_server))
        }
    }

    suspend fun addOwnerToNote(owner: String, noteId: String): Resource<String> = with(app) {
        try {
            val response = api.addOwnerToNote(AddOwnerToNoteRequest(noteId, owner))
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    if (body.successful) return@with Resource.Success(body.message)
                }
                return@with Resource.Error(
                    response.body()?.message ?: getString(R.string.problem_with_server)
                )
            } else return@with Resource.Error(response.message())
        } catch (e: Exception) {
            return@with Resource.Error(getString(R.string.could_not_connect_to_server))
        }
    }
}