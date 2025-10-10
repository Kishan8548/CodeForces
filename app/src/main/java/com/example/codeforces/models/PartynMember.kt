package com.example.codeforces.models

data class Party(
    val contestId: Int?,
    val members: List<Member>,
    val participantType: String,
    val teamId: Int?,
    val teamName: String?,
    val ghost: Boolean,
    val room: Int?,
    val startTimeSeconds: Int?
)


data class Member(
    val handle: String,
    val name: String?
)

