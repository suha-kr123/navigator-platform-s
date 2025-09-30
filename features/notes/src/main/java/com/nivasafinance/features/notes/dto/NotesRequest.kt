package com.nivasafinance.features.notes.dto

import java.util.UUID

data class NotesRequest(
    val title: String,
    val content: String
)