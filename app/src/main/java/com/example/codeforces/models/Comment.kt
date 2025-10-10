package com.example.codeforces.models

data class Comment(
    val id: Int,
    val creationTimeSeconds: Long,
    val commentatorHandle: String,
    val text: String,
    val parentCommentId: Int? = null,
    val rating: Int? = null
)
