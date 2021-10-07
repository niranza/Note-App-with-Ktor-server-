package com.niran.ktornoteapplication.dataset.retrofit.apis

import com.niran.ktornoteapplication.dataset.models.Note
import com.niran.ktornoteapplication.dataset.retrofit.requests.AccountRequest
import com.niran.ktornoteapplication.dataset.retrofit.requests.AddOwnerToNoteRequest
import com.niran.ktornoteapplication.dataset.retrofit.requests.DeleteNoteRequest
import com.niran.ktornoteapplication.dataset.retrofit.responses.SimpleResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface NoteApi {

    @POST("/register")
    suspend fun register(
        @Body registerRequest: AccountRequest
    ): Response<SimpleResponse>

    @POST("/login")
    suspend fun login(
        @Body loginRequest: AccountRequest
    ): Response<SimpleResponse>

    @POST("/addNote")
    suspend fun addNote(
        @Body note: Note
    ): Response<ResponseBody>

    @POST("/deleteNote")
    suspend fun deleteNote(
        @Body deleteNoteRequest: DeleteNoteRequest
    ): Response<ResponseBody>

    @POST("/addOwnerToNote")
    suspend fun addOwnerToNote(
        @Body addOwnerToNoteRequest: AddOwnerToNoteRequest
    ): Response<SimpleResponse>

    @GET("/getNotes")
    suspend fun getNotes(): Response<List<Note>>
}