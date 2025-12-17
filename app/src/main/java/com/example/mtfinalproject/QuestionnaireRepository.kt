package com.example.mtfinalproject

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object QuestionnaireRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun saveAnswers(
        age: String,
        healthCondition: String,
        exerciseHabit: String,
        strength: String,
        assistanceInWalking: String,
        riseFromChair: String,
        climbStairs: String,
        falls: String,
        sarcfScore: Int,
        sarcfRisk: String
    ): Result<Unit> {
        return try {
            val uid = UserRepository.getUid()
            val data = hashMapOf(
                "age" to age,
                "healthCondition" to healthCondition,
                "exerciseHabit" to exerciseHabit,
                "strength" to strength,
                "assistanceInWalking" to assistanceInWalking,
                "riseFromChair" to riseFromChair,
                "climbStairs" to climbStairs,
                "falls" to falls,
                "sarcfScore" to sarcfScore,
                "sarcfRisk" to sarcfRisk,
                "completedAt" to System.currentTimeMillis()
            )
            db.collection("users").document(uid)
                .collection("questionnaire").document("answers")
                .set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAnswers(): QuestionnaireData? {
        return try {
            val uid = UserRepository.getUid()
            val document = db.collection("users").document(uid)
                .collection("questionnaire").document("answers")
                .get().await()
            if (document.exists()) {
                QuestionnaireData(
                    age = document.getString("age") ?: "",
                    healthCondition = document.getString("healthCondition") ?: "",
                    exerciseHabit = document.getString("exerciseHabit") ?: "",
                    strength = document.getString("strength") ?: "",
                    assistanceInWalking = document.getString("assistanceInWalking") ?: "",
                    riseFromChair = document.getString("riseFromChair") ?: "",
                    climbStairs = document.getString("climbStairs") ?: "",
                    falls = document.getString("falls") ?: "",
                    sarcfScore = document.getLong("sarcfScore")?.toInt() ?: 0,
                    sarcfRisk = document.getString("sarcfRisk") ?: ""
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun isCompleted(): Boolean {
        return try {
            val uid = UserRepository.getUid()
            val document = db.collection("users").document(uid)
                .collection("questionnaire").document("answers")
                .get().await()
            document.exists()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun clearAnswers(): Result<Unit> {
        return try {
            val uid = UserRepository.getUid()
            db.collection("users").document(uid)
                .collection("questionnaire").document("answers")
                .delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class QuestionnaireData(
    val age: String,
    val healthCondition: String,
    val exerciseHabit: String,
    val strength: String,
    val assistanceInWalking: String,
    val riseFromChair: String,
    val climbStairs: String,
    val falls: String,
    val sarcfScore: Int,
    val sarcfRisk: String
)
