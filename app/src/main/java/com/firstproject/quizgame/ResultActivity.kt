package com.firstproject.quizgame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firstproject.quizgame.Adapters.ResultAdapter
import com.firstproject.quizgame.models.QuizResult
import com.firstproject.quizgame.Utils.FirebaseUtils

import kotlinx.coroutines.launch

class ResultActivity : AppCompatActivity() {

    private lateinit var tvScore: TextView
    private lateinit var tvTime: TextView
    private lateinit var tvPerformance: TextView
    private lateinit var recyclerViewResults: RecyclerView
    private lateinit var btnPlayAgain: Button
    private lateinit var btnHome: Button

    private lateinit var resultAdapter: ResultAdapter
    private var results = mutableListOf<QuizResult>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        // Initialize views
        tvScore = findViewById(R.id.tvScore)
        tvTime = findViewById(R.id.tvTime)
        tvPerformance = findViewById(R.id.tvPerformance)
        recyclerViewResults = findViewById(R.id.recyclerViewResults)
        btnPlayAgain = findViewById(R.id.btnPlayAgain)
        btnHome = findViewById(R.id.btnHome)

        // Get result data from intent
        val score = intent.getIntExtra("SCORE", 0)
        val totalQuestions = intent.getIntExtra("TOTAL_QUESTIONS", 10)
        val timeSpent = intent.getIntExtra("TIME_SPENT", 0)
        val categoryName = intent.getStringExtra("CATEGORY_NAME") ?: ""

        // Display current result
        tvScore.text = "Score: $score/$totalQuestions"

        // Format time
        val minutes = timeSpent / 60
        val seconds = timeSpent % 60
        tvTime.text = "Time: ${if (minutes > 0) "$minutes min " else ""}$seconds sec"

        // Calculate and display performance
        val performanceScore = calculatePerformanceScore(score, totalQuestions, timeSpent)
        tvPerformance.text = "Performance: $performanceScore%"

        // Set up RecyclerView
        resultAdapter = ResultAdapter(results)
        recyclerViewResults.apply {
            layoutManager = LinearLayoutManager(this@ResultActivity)
            adapter = resultAdapter
        }

        // Load previous results
        loadResults()

        // Set click listeners
        btnPlayAgain.setOnClickListener {
            // Return to category selection
            val intent = Intent(this, CategorySelectionActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnHome.setOnClickListener {
            // Return to main activity
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun calculatePerformanceScore(score: Int, totalQuestions: Int, timeSpent: Int): Int {
        // Formula: (score / totalQuestions) * 100 * (1 + (600 - timeSpent) / 600)
        // This rewards both accuracy and speed
        // Maximum time consideration is 10 minutes (600 seconds)
        val accuracyScore = (score.toFloat() / totalQuestions.toFloat()) * 100
        val timeBonus = 1f + (Math.max(0, 600 - timeSpent) / 600f)
        return (accuracyScore * timeBonus).toInt()
    }

    private fun loadResults() {
        lifecycleScope.launch {
            try {
                val userId = FirebaseUtils.getCurrentUserId()
                if (userId != null) {
                    val userResults = FirebaseUtils.getUserResults(userId)
                    results.clear()
                    results.addAll(userResults)
                    resultAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}