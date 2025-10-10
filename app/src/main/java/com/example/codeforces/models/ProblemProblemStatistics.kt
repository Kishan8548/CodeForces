package com.example.codeforces.models

data class Problem(
    val contestId: Int?,
    val index: String,
    val name: String,
    val type: String,
    val points: Float?,
    val rating: Int?,
    val tags: List<String>
)


data class ProblemStatistics(
    val contestId: Int? = null,
    val index: String,
    val solvedCount: Int
)
