package com.niran.ktornoteapplication.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.niran.ktornoteapplication.NoteApplication
import com.niran.ktornoteapplication.R
import com.niran.ktornoteapplication.repositories.NoteRepository
import com.niran.ktornoteapplication.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: NoteRepository,
    app: Application
) : AndroidViewModel(app) {

    private val _registerStatus = MutableLiveData<Resource<String>>()
    val registerStatus: LiveData<Resource<String>> get() = _registerStatus

    private val _loginStatus = MutableLiveData<Resource<String>>()
    val loginStatus: LiveData<Resource<String>> get() = _loginStatus

    fun register(email: String, password: String, repeatedPassword: String) =
        getApplication<NoteApplication>().apply {
            _registerStatus.value = Resource.Loading()
            if (email.isEmpty() || password.isEmpty() || repeatedPassword.isEmpty()) {
                _registerStatus.value = Resource.Error(getString(R.string.fill_all_fields))
                return@apply
            }
            if (password != repeatedPassword) {
                _registerStatus.value = Resource.Error(getString(R.string.password_do_not_match))
                return@apply
            }
            viewModelScope.launch {
                val result = repository.register(email, password)
                _registerStatus.postValue(result)
            }
        }

    fun login(email: String, password: String) =
        getApplication<NoteApplication>().apply {
            _loginStatus.value = Resource.Loading()
            if (email.isEmpty() || password.isEmpty()) {
                _loginStatus.value = Resource.Error(getString(R.string.fill_all_fields))
                return@apply
            }
            viewModelScope.launch {
                val result = repository.login(email, password)
                _loginStatus.postValue(result)
            }
        }
}