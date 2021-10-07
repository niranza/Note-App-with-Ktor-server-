package com.niran.ktornoteapplication.viewmodels

import androidx.lifecycle.*
import com.niran.ktornoteapplication.dataset.models.Note
import com.niran.ktornoteapplication.repositories.NoteRepository
import com.niran.ktornoteapplication.utils.Event
import com.niran.ktornoteapplication.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val _forceUpdate = MutableLiveData(false)

    private val _noteList
        get() = _forceUpdate.switchMap {
            repository.getAllNotes().asLiveData(viewModelScope.coroutineContext)
        }.switchMap { MutableLiveData(Event(it)) }
    val noteList: LiveData<Event<Resource<List<Note>>>> get() = _noteList

    fun syncAllNotes() = _forceUpdate.apply { value = true }

    fun insertNote(note: Note) = viewModelScope.launch {
        repository.insertNote(note)
    }

    fun deleteNote(noteId: String) = viewModelScope.launch {
        repository.deleteNote(noteId)
    }

    fun deletedLocallyDeletedNoteId(deletedNoteId: String) = viewModelScope.launch {
        repository.deleteLocallyDeletedNoteId(deletedNoteId)
    }

    fun deleteAllCachedNotes() = GlobalScope.launch { repository.deleteAllCachedNotes() }
}