package com.niran.ktornoteapplication.utils

sealed class Resource<out T>(val data: T?, val message: String?) {

    class Loading<T>(data: T? = null) : Resource<T>(data, null)
    class Success<T>(data: T) : Resource<T>(data, null)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
}