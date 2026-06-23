package com.example.codeforces.utils

import com.example.codeforces.models.Contest
import com.example.codeforces.models.Problem
import com.example.codeforces.models.Submission
import com.example.codeforces.models.User

/**
 * Simple in-memory cache to prevent refetching data every time tabs are switched.
 * Data is only refetched when the user performs a Swipe-to-Refresh.
 */
object DataCache {
    var cachedUser: User? = null
    var cachedSubmissions: List<Submission>? = null
    var cachedContests: List<Contest>? = null
    var cachedProblems: List<Problem>? = null
    var cachedStatistics: List<com.example.codeforces.models.ProblemStatistics>? = null
    var cachedFriends: List<User>? = null

    fun clear() {
        cachedUser = null
        cachedSubmissions = null
        cachedContests = null
        cachedProblems = null
        cachedStatistics = null
        cachedFriends = null
    }
}
