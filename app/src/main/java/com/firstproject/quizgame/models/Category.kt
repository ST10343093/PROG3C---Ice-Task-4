package com.firstproject.quizgame.models

// Model class for quiz categories
data class Category(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageResource: Int = 0,
    val questions: List<Question> = emptyList(),
    var progress: Int = 0, // Progress value for the seekbar
    var completed: Boolean = false
)