package com.niran.ktornoteapplication.dataset.retrofit.requests

data class AddOwnerToNoteRequest(
    val noteId: String,
    val owner: String
)
