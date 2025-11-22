package com.example.mtfinalproject

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

object UserRepository {
    private val auth = FirebaseAuth.getInstance()

    suspend fun getUid(): String {
        var user = auth.currentUser
        if (user == null) {
            // Sign in anonymously
            val result = auth.signInAnonymously().await()
            user = result.user
        }
        return user?.uid ?: throw IllegalStateException("Failed to get user")
    }

    fun getCurrentUid(): String? {
        return auth.currentUser?.uid
    }

    fun isSignedIn(): Boolean {
        return auth.currentUser != null
    }

    fun signOut() {
        auth.signOut()
    }
}
