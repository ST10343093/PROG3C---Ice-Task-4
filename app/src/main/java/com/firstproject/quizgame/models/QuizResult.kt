package com.firstproject.quizgame.models

import java.security.Timestamp

// Model class for quiz results to be stored in Firestore
data class QuizResult(
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val timeSpentInSeconds: Int = 0,
    //val timestamp: Timestamp = Timestamp.now()
    val dateCompleted: String = ""
) {
    // Calculate performance score based on correct answers and time
    fun calculatePerformanceScore(): Int {
        // Formula: (score / totalQuestions) * 100 * (1 + (600 - timeSpentInSeconds) / 600)
        // This rewards both accuracy and speed
        // Maximum time consideration is 10 minutes (600 seconds)
        val accuracyScore = (score.toFloat() / totalQuestions.toFloat()) * 100
        val timeBonus = 1f + (Math.max(0, 600 - timeSpentInSeconds) / 600f)
        return (accuracyScore * timeBonus).toInt()
    }
}