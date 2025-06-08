package com.firstproject.quizgame.models

// Model class for quiz questions
data class Question(
    val id: String = "",
    val categoryId: String = "",
    val text: String = "",
    val options: List<String> = emptyList(),
    val correctOptionIndex: Int = 0,
    var isAnswered: Boolean = false,
    var selectedOption: Int = -1
)