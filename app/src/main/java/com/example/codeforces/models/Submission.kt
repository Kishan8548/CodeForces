package com.example.codeforces.models

data class Submission(
    val id: Long,
    val contestId: Int? = null,
    val creationTimeSeconds: Long,
    val problem: Problem,
    val author: Party,
    val programmingLanguage: String,
    val verdict: String? = null,
    val passedTestCount: Int? = null,
    val timeConsumedMillis: Int? = null,
    val memoryConsumedBytes: Long? = null
)

