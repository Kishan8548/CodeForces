package com.example.codeforces.models

data class User(
    val handle: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val country: String? = null,
    val city: String? = null,
    val organization: String? = null,
    val contribution: Int? = null,
    val rank: String? = null,
    val rating: Int? = null,
    val maxRank: String? = null,
    val maxRating: Int? = null,
    val friendOfCount: Int? = null,
    val avatar: String? = null,
    val titlePhoto: String? = null
)
