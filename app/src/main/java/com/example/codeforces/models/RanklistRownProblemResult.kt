package com.example.codeforces.models

data class RanklistRow(
    val party: Party,
    val rank: Int,
    val points: Float,
    val penalty: Int,
    val successfulHackCount: Int,
    val unsuccessfulHackCount: Int,
    val problemResults: List<ProblemResult>,
    val lastSubmissionTimeSeconds: Int?
)


data class ProblemResult(
    val points: Float,
    val penalty: Int?,
    val rejectedAttemptCount: Int,
    val type: String,
    val bestSubmissionTimeSeconds: Int?
)
