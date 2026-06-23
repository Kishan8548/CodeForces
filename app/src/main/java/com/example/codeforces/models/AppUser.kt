package com.example.codeforces.models

import com.google.firebase.Timestamp

/**
 * Firestore user document stored at: users/{uid}/
 */
data class AppUser(
    val uid: String = "",
    val handle: String = "",           // Codeforces handle
    val displayName: String = "",      // Google account display name
    val email: String = "",
    val photoUrl: String = "",
    val lastSeen: Timestamp? = null,
    val friends: List<String> = emptyList(),          // CF handles of in-app friends
    val bookmarkedProblems: List<String> = emptyList() // e.g. "1234/A"
) {
    /** Required for Firestore deserialization */
    constructor() : this("", "", "", "", "")
}
