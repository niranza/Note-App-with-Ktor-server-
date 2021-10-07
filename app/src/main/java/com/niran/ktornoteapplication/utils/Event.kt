package com.niran.ktornoteapplication.utils

open class Event <out T>(private val content: T){

    private var _hasBeenHandled = false
    val hasBeenHandled get() = _hasBeenHandled

    fun getContentIfNotHandled() = if (_hasBeenHandled) null else {
        _hasBeenHandled = true
        content
    }

    fun peekContent() = content
}