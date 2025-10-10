package com.example.codeforces.models

data class ProblemSetResponse(
    val problems: List<Problem>,
    val problemStatistics: List<ProblemStatistics>
)
