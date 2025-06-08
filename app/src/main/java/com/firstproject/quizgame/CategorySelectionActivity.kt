package com.firstproject.quizgame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firstproject.quizgame.Adapters.CategoryAdapter
import com.firstproject.quizgame.models.Category
import com.firstproject.quizgame.Utils.FirebaseUtils
import kotlinx.coroutines.launch

class CategorySelectionActivity : AppCompatActivity() {

    private lateinit var recyclerViewCategories: RecyclerView
    private lateinit var tvUsername: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnViewResults: Button

    private lateinit var categoryAdapter: CategoryAdapter
    private var categories = mutableListOf<Category>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_selection)

        // Initialize views
        recyclerViewCategories = findViewById(R.id.recyclerViewCategories)
        tvUsername = findViewById(R.id.tvUsername)
        btnLogout = findViewById(R.id.btnLogout)
        btnViewResults = findViewById(R.id.btnViewResults)

        // Set up RecyclerView
        categoryAdapter = CategoryAdapter(categories) { category ->
            navigateToQuiz(category)
        }

        recyclerViewCategories.apply {
            layoutManager = GridLayoutManager(this@CategorySelectionActivity, 2)
            adapter = categoryAdapter
        }

        // Get user info and categories
        loadUserInfo()
        loadCategories()

        // Set click listeners
        btnLogout.setOnClickListener {
            FirebaseUtils.logoutUser()
            navigateToLogin()
        }

        btnViewResults.setOnClickListener {
            navigateToResults()
        }
    }

    private fun loadUserInfo() {
        lifecycleScope.launch {
            try {
                val user = FirebaseUtils.getCurrentUser()
                if (user != null) {
                    tvUsername.text = "Welcome, ${user.username}!"
                } else {
                    navigateToLogin()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CategorySelectionActivity, "Error loading user data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            try {
                val fetchedCategories = FirebaseUtils.getCategories()

                // Update the UI on the main thread
                runOnUiThread {
                    categories.clear()

                    // Check if we have categories from Firestore
                    if (fetchedCategories.isNotEmpty()) {
                        // Add the fetched categories directly
                        categories.addAll(fetchedCategories)
                    } else {
                        // If no categories from Firestore, create dummy ones
                        createDummyCategories()
                    }

                    categoryAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                // Show toast on the main thread
                runOnUiThread {
                    Toast.makeText(this@CategorySelectionActivity, "Error loading categories: ${e.message}", Toast.LENGTH_SHORT).show()
                    // Create dummy categories on error
                    createDummyCategories()
                    categoryAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun createDummyCategories() {
        // Sample categories for testing
        val sampleCategories = listOf(
            Category(
                id = "science",
                name = "Science",
                description = "Test your knowledge of scientific facts and discoveries",
                imageResource = R.drawable.science
            ),
            Category(
                id = "history",
                name = "History",
                description = "Challenge yourself with historical events and figures",
                imageResource = R.drawable.history
            ),
            Category(
                id = "sports",
                name = "Sports",
                description = "Sports trivia from around the world",
                imageResource = R.drawable.sports
            ),
            Category(
                id = "entertainment",
                name = "Entertainment",
                description = "Movies, music, and pop culture questions",
                imageResource = R.drawable.entertainment
            )
        )

        categories.addAll(sampleCategories)
        categoryAdapter.notifyDataSetChanged()
    }

    private fun navigateToQuiz(category: Category) {
        val intent = Intent(this, QuizActivity::class.java).apply {
            putExtra("CATEGORY_ID", category.id)
            putExtra("CATEGORY_NAME", category.name)
        }
        startActivity(intent)
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToResults() {
        val intent = Intent(this, ResultActivity::class.java)
        startActivity(intent)
    }
}