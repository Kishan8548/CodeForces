package com.example.codeforces.models

data class Contest(
    val id: Int,
    val name: String,
    val type: String,
    val phase: String,
    val frozen: Boolean,
    val durationSeconds: Int,
    val freezeDurationSeconds: Int?,
    val startTimeSeconds: Long?,
    val relativeTimeSeconds: Int?,
    val preparedBy: String?,
    val websiteUrl: String?,
    val description: String?,
    val difficulty: Int?,
    val kind: String?,
    val icpcRegion: String?,
    val country: String?,
    val city: String?,
    val season: String?
)
