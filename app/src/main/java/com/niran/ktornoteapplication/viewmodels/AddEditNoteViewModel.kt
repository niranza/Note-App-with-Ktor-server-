package com.niran.ktornoteapplication.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niran.ktornoteapplication.dataset.models.Note
import com.niran.ktornoteapplication.repositories.NoteRepository
import com.niran.ktornoteapplication.utils.Event
import com.niran.ktornoteapplication.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val _note = MutableLiveData<Event<Resource<Note>>>()
    val note: LiveData<Event<Resource<Note>>> get() = _note

    fun insertNote(note: Note) = GlobalScope.launch { repository.insertNote(note) }

    fun getNoteById(noteId: String) = viewModelScope.launch {
        _note.postValue(Event(Resource.Loading()))
        repository.getNoteById(noteId)?.let { note ->
            _note.postValue(Event(Resource.Success(note)))
        } ?: _note.postValue(Event(Resource.Error("Note not found")))
    }
}