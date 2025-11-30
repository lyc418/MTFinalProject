package com.example.mtfinalproject

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object SignInRepository {
    private val db = FirebaseFirestore.getInstance()
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE // YYYY-MM-DD format

    /**
     * Records today's sign-in date to Firestore.
     * Only adds the date if it hasn't been recorded today.
     */
    suspend fun recordSignIn(): Result<Unit> {
        return try {
            val uid = UserRepository.getUid()
            val today = LocalDate.now().format(dateFormatter)

            val docRef = db.collection("users").document(uid)
                .collection("profile").document("signInHistory")

            val document = docRef.get().await()

            if (document.exists()) {
                val lastSignIn = document.getString("lastSignIn")
                if (lastSignIn != today) {
                    // Update with today's date
                    docRef.update(
                        mapOf(
                            "dates" to FieldValue.arrayUnion(today),
                            "lastSignIn" to today
                        )
                    ).await()
                }
                // If lastSignIn == today, do nothing (already recorded)
            } else {
                // First time sign-in, create the document
                val data = hashMapOf(
                    "dates" to listOf(today),
                    "lastSignIn" to today,
                    "createdAt" to System.currentTimeMillis()
                )
                docRef.set(data).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets the sign-in history from Firestore.
     */
    suspend fun getSignInHistory(): SignInHistory? {
        return try {
            val uid = UserRepository.getUid()
            val document = db.collection("users").document(uid)
                .collection("profile").document("signInHistory")
                .get().await()

            if (document.exists()) {
                @Suppress("UNCHECKED_CAST")
                val dates = document.get("dates") as? List<String> ?: emptyList()
                val lastSignIn = document.getString("lastSignIn") ?: ""
                SignInHistory(dates = dates, lastSignIn = lastSignIn)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Calculates the current streak based on consecutive days.
     * Returns the number of consecutive days up to and including today.
     */
    suspend fun calculateStreak(): Int {
        val history = getSignInHistory() ?: return 0
        if (history.dates.isEmpty()) return 0

        val today = LocalDate.now()
        val sortedDates = history.dates
            .mapNotNull { runCatching { LocalDate.parse(it, dateFormatter) }.getOrNull() }
            .sortedDescending()

        if (sortedDates.isEmpty()) return 0

        // Check if today or yesterday is in the list (streak is still active)
        val mostRecent = sortedDates.first()
        val daysSinceMostRecent = ChronoUnit.DAYS.between(mostRecent, today)

        // If most recent sign-in is more than 1 day ago, streak is broken
        if (daysSinceMostRecent > 1) return 0

        var streak = 1
        var expectedDate = mostRecent.minusDays(1)

        for (i in 1 until sortedDates.size) {
            if (sortedDates[i] == expectedDate) {
                streak++
                expectedDate = expectedDate.minusDays(1)
            } else if (sortedDates[i] < expectedDate) {
                // Gap found, streak ends
                break
            }
            // If sortedDates[i] == sortedDates[i-1], it's a duplicate, skip
        }

        return streak
    }
}

data class SignInHistory(
    val dates: List<String>,
    val lastSignIn: String
)
