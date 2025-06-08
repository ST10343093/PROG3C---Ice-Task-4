package com.firstproject.quizgame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.firstproject.quizgame.Utils.FirebaseUtils
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var tvSwitchMode: TextView

    private var isLoginMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views
        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)
        tvSwitchMode = findViewById(R.id.tvSwitchMode)

        // Check if user is already logged in
        if (FirebaseUtils.getCurrentUserId() != null) {
            navigateToCategorySelection()
            return
        }

        updateUI()

        // Set click listeners
        btnLogin.setOnClickListener { handleLogin() }
        btnRegister.setOnClickListener { handleRegister() }
        tvSwitchMode.setOnClickListener {
            isLoginMode = !isLoginMode
            updateUI()
        }
    }

    private fun updateUI() {
        if (isLoginMode) {
            // Login mode
            etUsername.visibility = android.view.View.GONE
            btnLogin.visibility = android.view.View.VISIBLE
            btnRegister.visibility = android.view.View.GONE
            tvSwitchMode.text = "Don't have an account? Register"
        } else {
            // Register mode
            etUsername.visibility = android.view.View.VISIBLE
            btnLogin.visibility = android.view.View.GONE
            btnRegister.visibility = android.view.View.VISIBLE
            tvSwitchMode.text = "Already have an account? Login"
        }
    }

    private fun handleLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                FirebaseUtils.loginUser(email, password)
                navigateToCategorySelection()
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleRegister() {
        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                FirebaseUtils.registerUser(email, password, username)
                Toast.makeText(this@LoginActivity, "Registration successful", Toast.LENGTH_SHORT).show()
                navigateToCategorySelection()
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToCategorySelection() {
        val intent = Intent(this, CategorySelectionActivity::class.java)
        startActivity(intent)
        finish()
    }
}