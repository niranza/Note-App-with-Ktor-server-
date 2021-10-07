package com.niran.ktornoteapplication.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.niran.ktornoteapplication.NoteApplication
import com.niran.ktornoteapplication.R
import com.niran.ktornoteapplication.dataset.models.Note
import com.niran.ktornoteapplication.repositories.NoteRepository
import com.niran.ktornoteapplication.utils.Event
import com.niran.ktornoteapplication.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val repository: NoteRepository,
    app: Application
) : AndroidViewModel(app) {

    private val _addOwnerToNoteStatus = MutableLiveData<Event<Resource<String>>>()
    val addOwnerToNoteStatus: LiveData<Event<Resource<String>>> get() = _addOwnerToNoteStatus

    fun addOwnerToNote(owner: String, noteId: String) = with(getApplication<NoteApplication>()) {
        _addOwnerToNoteStatus.value = Event(Resource.Loading())
        if (owner.isEmpty() || noteId.isEmpty()) {
            _addOwnerToNoteStatus.value = Event((Resource.Error(getString(R.string.owner_empty))))
            return@with
        }
        viewModelScope.launch {
            val result = repository.addOwnerToNote(owner, noteId)
            _addOwnerToNoteStatus.postValue(Event(result))
        }
    }

    fun observeNoteById(noteId: String) = repository.collectNoteById(noteId)?.asLiveData()
}