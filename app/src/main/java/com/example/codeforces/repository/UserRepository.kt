package com.example.codeforces.repository

import com.example.codeforces.models.AppUser
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * All Firestore read/write operations for the current user's profile.
 * Singleton backed by Firestore — no Room needed at this stage.
 */
object UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    // ─── Create / Update ─────────────────────────────────────────────────────

    /**
     * Creates or updates the user document in Firestore.
     * Called after Google Sign-In + handle verification.
     */
    suspend fun saveUser(firebaseUser: FirebaseUser, cfHandle: String): Result<Unit> {
        return try {
            val user = AppUser(
                uid = firebaseUser.uid,
                handle = cfHandle,
                displayName = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: "",
                photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                lastSeen = Timestamp.now()
            )
            usersCollection.document(firebaseUser.uid)
                .set(user)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates only the CF handle — used from Settings "Change Handle" flow.
     */
    suspend fun updateHandle(uid: String, newHandle: String): Result<Unit> {
        return try {
            usersCollection.document(uid)
                .update("handle", newHandle, "lastSeen", Timestamp.now())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    /**
     * Fetches the AppUser document for [uid]. Returns null if not found.
     */
    suspend fun getUser(uid: String): AppUser? {
        return try {
            val snap = usersCollection.document(uid).get().await()
            snap.toObject(AppUser::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Returns the CF handle stored in Firestore for the currently signed-in UID.
     * Falls back to null if not found (first-time user, or Firestore unreachable).
     */
    suspend fun getHandle(uid: String): String? {
        return getUser(uid)?.handle
    }

    // ─── Friends ─────────────────────────────────────────────────────────────

    /**
     * Adds a CF handle to the user's friends list (array-union — no duplicates).
     */
    suspend fun addFriend(uid: String, friendHandle: String): Result<Unit> {
        return try {
            usersCollection.document(uid)
                .set(mapOf("friends" to com.google.firebase.firestore.FieldValue.arrayUnion(friendHandle)), com.google.firebase.firestore.SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Removes a CF handle from the user's friends list.
     */
    suspend fun removeFriend(uid: String, friendHandle: String): Result<Unit> {
        return try {
            usersCollection.document(uid)
                .set(mapOf("friends" to com.google.firebase.firestore.FieldValue.arrayRemove(friendHandle)), com.google.firebase.firestore.SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Returns the friend handles list for [uid].
     */
    suspend fun getFriends(uid: String): List<String> {
        return getUser(uid)?.friends ?: emptyList()
    }

    // ─── Bookmarks ───────────────────────────────────────────────────────────

    suspend fun addBookmark(uid: String, problemKey: String): Result<Unit> {
        return try {
            usersCollection.document(uid)
                .update("bookmarkedProblems", com.google.firebase.firestore.FieldValue.arrayUnion(problemKey))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeBookmark(uid: String, problemKey: String): Result<Unit> {
        return try {
            usersCollection.document(uid)
                .update("bookmarkedProblems", com.google.firebase.firestore.FieldValue.arrayRemove(problemKey))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBookmarks(uid: String): List<String> {
        return getUser(uid)?.bookmarkedProblems ?: emptyList()
    }
}
