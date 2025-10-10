package com.example.codeforces.models

data class ContestStandingsResult(
    val contest: Contest,
    val problems: List<Problem>,
    val rows: List<RanklistRow>
)
