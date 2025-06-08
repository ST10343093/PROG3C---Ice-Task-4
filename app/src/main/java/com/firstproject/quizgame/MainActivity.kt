package com.firstproject.quizgame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.firstproject.quizgame.Utils.FirebaseUtils


class MainActivity : AppCompatActivity() {

    private lateinit var btnStart: Button
    private lateinit var btnViewLeaderboard: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        btnStart = findViewById(R.id.btnStart)
        btnViewLeaderboard = findViewById(R.id.btnViewLeaderboard)

        // Check if user is already logged in
        if (FirebaseUtils.getCurrentUserId() != null) {
            // User is logged in, show category selection directly
            navigateToCategorySelection()
        }

        // Set click listeners
        btnStart.setOnClickListener {
            // Navigate to login screen
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        btnViewLeaderboard.setOnClickListener {
            // Navigate to results/leaderboard
            val intent = Intent(this, ResultActivity::class.java)
            startActivity(intent)
        }
    }

    private fun navigateToCategorySelection() {
        val intent = Intent(this, CategorySelectionActivity::class.java)
        startActivity(intent)
    }
}