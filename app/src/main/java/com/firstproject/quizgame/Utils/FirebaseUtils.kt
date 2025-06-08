package com.firstproject.quizgame.Utils

import com.firstproject.quizgame.models.Category
import com.firstproject.quizgame.models.Question
import com.firstproject.quizgame.models.QuizResult
import com.firstproject.quizgame.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

object FirebaseUtils {
    private const val TAG = "FirebaseUtils"

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Collections
    private const val USERS_COLLECTION = "users"
    private const val CATEGORIES_COLLECTION = "categories"
    private const val QUESTIONS_COLLECTION = "questions"
    private const val RESULTS_COLLECTION = "results"

    // Authentication functions
    suspend fun registerUser(email: String, password: String, username: String): String {
        try {
            Log.d(TAG, "Attempting to register user: $email")
            // Create authentication account
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("Failed to create user")

            // Create user document in Firestore
            val user = User(
                id = userId,
                username = username,
                email = email
            )
            firestore.collection(USERS_COLLECTION).document(userId).set(user).await()
            Log.d(TAG, "User registered successfully with ID: $userId")

            return userId
        } catch (e: Exception) {
            Log.e(TAG, "Error registering user: ${e.message}", e)
            throw e
        }
    }

    suspend fun loginUser(email: String, password: String): String {
        try {
            Log.d(TAG, "Attempting to login user: $email")
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("Login failed")
            Log.d(TAG, "User logged in successfully with ID: $userId")
            return userId
        } catch (e: Exception) {
            Log.e(TAG, "Error logging in user: ${e.message}", e)
            throw e
        }
    }

    fun logoutUser() {
        Log.d(TAG, "Logging out user")
        auth.signOut()
    }

    fun getCurrentUserId(): String? {
        val userId = auth.currentUser?.uid
        Log.d(TAG, "Current user ID: $userId")
        return userId
    }

    suspend fun getCurrentUser(): User? {
        try {
            val userId = getCurrentUserId() ?: return null
            Log.d(TAG, "Fetching user data for ID: $userId")
            val document = firestore.collection(USERS_COLLECTION).document(userId).get().await()

            // Check if document exists
            if (!document.exists()) {
                Log.w(TAG, "User document does not exist for ID: $userId")

                // If user authenticated but no document, create a default one
                if (auth.currentUser != null) {
                    val email = auth.currentUser?.email ?: "unknown@email.com"
                    val defaultUser = User(
                        id = userId,
                        username = "User_${userId.takeLast(5)}",
                        email = email
                    )
                    firestore.collection(USERS_COLLECTION).document(userId).set(defaultUser).await()
                    Log.d(TAG, "Created default user document for ID: $userId")
                    return defaultUser
                }
                return null
            }

            // Convert document to User object
            val user = document.toObject(User::class.java)
            Log.d(TAG, "Retrieved user: ${user?.username}")
            return user
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user: ${e.message}", e)
            // Return a default user rather than failing
            val userId = getCurrentUserId()
            if (userId != null) {
                return User(
                    id = userId,
                    username = "Temporary_User",
                    email = "temporary@email.com"
                )
            }
            return null
        }
    }

    // Category and Question functions
    suspend fun getCategories(): List<Category> {
        try {
            Log.d(TAG, "Fetching categories from Firestore")
            val snapshot = firestore.collection(CATEGORIES_COLLECTION).get().await()
            val categories = snapshot.toObjects(Category::class.java)
            Log.d(TAG, "Retrieved ${categories.size} categories")
            return categories
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching categories: ${e.message}", e)
            return emptyList()
        }
    }

    suspend fun getQuestionsForCategory(categoryId: String): List<Question> {
        try {
            Log.d(TAG, "Fetching questions for category: $categoryId")
            val snapshot = firestore.collection(QUESTIONS_COLLECTION)
                .whereEqualTo("categoryId", categoryId)
                .get().await()
            val questions = snapshot.toObjects(Question::class.java)
            Log.d(TAG, "Retrieved ${questions.size} questions for category: $categoryId")
            return questions
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching questions: ${e.message}", e)
            return emptyList()
        }
    }

    // Quiz result functions
    suspend fun saveQuizResult(result: QuizResult): String {
        try {
            Log.d(TAG, "Saving quiz result for user: ${result.userId}, category: ${result.categoryName}")
            val resultRef = firestore.collection(RESULTS_COLLECTION).document()
            val resultWithId = result.copy(id = resultRef.id)

            // Format date
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val dateCompleted = dateFormat.format(Date())

            // Save result
            val finalResult = resultWithId.copy(dateCompleted = dateCompleted)
            resultRef.set(finalResult).await()
            Log.d(TAG, "Quiz result saved with ID: ${resultRef.id}")

            // Check if user exists and update progress if needed
            val userId = getCurrentUserId() ?: throw Exception("User not logged in")
            val userRef = firestore.collection(USERS_COLLECTION).document(userId)

            try {
                // Get current progress
                val userDoc = userRef.get().await()
                if (userDoc.exists()) {
                    val user = userDoc.toObject(User::class.java) ?: throw Exception("User not found")
                    Log.d(TAG, "Updating progress for user: ${user.username}")

                    // Update progress
                    val categoryProgress = user.categoryProgress.toMutableMap()
                    categoryProgress[result.categoryId] = result.score
                    userRef.update("categoryProgress", categoryProgress).await()
                    Log.d(TAG, "User progress updated")
                } else {
                    Log.w(TAG, "User document does not exist, skipping progress update")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating user progress: ${e.message}", e)
                // Continue even if updating progress fails
            }

            return resultRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error saving quiz result: ${e.message}", e)
            throw e
        }
    }

    suspend fun getUserResults(userId: String): List<QuizResult> {
        try {
            Log.d(TAG, "Fetching results for user: $userId")
            val snapshot = firestore.collection(RESULTS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get().await()
            val results = snapshot.toObjects(QuizResult::class.java)
            Log.d(TAG, "Retrieved ${results.size} results for user: $userId")
            return results
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user results: ${e.message}", e)
            return emptyList()
        }
    }

    suspend fun getTopResults(limit: Int = 10): List<QuizResult> {
        try {
            Log.d(TAG, "Fetching top $limit results")
            val snapshot = firestore.collection(RESULTS_COLLECTION)
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get().await()
            val results = snapshot.toObjects(QuizResult::class.java)
            Log.d(TAG, "Retrieved ${results.size} top results")
            return results
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching top results: ${e.message}", e)
            return emptyList()
        }
    }
}