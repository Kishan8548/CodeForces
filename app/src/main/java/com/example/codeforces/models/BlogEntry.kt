package com.example.codeforces.models

data class BlogEntry(
    val id: Int,
    val authorHandle: String,
    val title: String,
    val content: String? = null, // optional if short version
    val creationTimeSeconds: Long,
    val modificationTimeSeconds: Long? = null,
    val tags: List<String>? = null,
    val rating: Int? = null
)
