package com.firstproject.quizgame.models

// Model class for user data
data class User(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val profilePictureUrl: String = "",
    val categoryProgress: Map<String, Int> = mapOf()
)