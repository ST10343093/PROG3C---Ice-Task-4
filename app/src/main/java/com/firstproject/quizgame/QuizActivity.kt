package com.firstproject.quizgame

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.firstproject.quizgame.models.Question
import com.firstproject.quizgame.Utils.FirebaseUtils
import com.firstproject.quizgame.models.QuizResult
import kotlinx.coroutines.launch
import com.firstproject.quizgame.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*


class QuizActivity : AppCompatActivity() {

    private val TAG = "QuizActivity" // Add a tag for logging

    private lateinit var tvCategoryName: TextView
    private lateinit var tvQuestionNumber: TextView
    private lateinit var tvQuestion: TextView
    private lateinit var radioGroupOptions: RadioGroup
    private lateinit var btnNext: Button
    private lateinit var seekBarProgress: SeekBar
    private lateinit var tvTimer: TextView

    private lateinit var categoryId: String
    private lateinit var categoryName: String
    private var questions = listOf<Question>()
    private var currentQuestionIndex = 0
    private var score = 0
    private var startTime: Long = 0
    private var timeSpentInSeconds = 0

    private lateinit var countDownTimer: CountDownTimer
    private val questionTimeInMillis = 60000L // 60 seconds per question

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        // Get category information from intent
        categoryId = intent.getStringExtra("CATEGORY_ID") ?: ""
        categoryName = intent.getStringExtra("CATEGORY_NAME") ?: ""

        if (categoryId.isEmpty()) {
            Toast.makeText(this, "Invalid category", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize views
        tvCategoryName = findViewById(R.id.tvCategoryName)
        tvQuestionNumber = findViewById(R.id.tvQuestionNumber)
        tvQuestion = findViewById(R.id.tvQuestion)
        radioGroupOptions = findViewById(R.id.radioGroupOptions)
        btnNext = findViewById(R.id.btnNext)
        seekBarProgress = findViewById(R.id.seekBarProgress)
        tvTimer = findViewById(R.id.tvTimer)

        // Set category name
        tvCategoryName.text = categoryName

        // Load questions
        loadQuestions()

        // Set click listener for next button
        btnNext.setOnClickListener {
            if (validateAnswer()) {
                saveAnswer()
                if (currentQuestionIndex < questions.size - 1) {
                    currentQuestionIndex++
                    displayQuestion()
                } else {
                    // Log before finishing quiz
                    Log.d(TAG, "Last question answered, finishing quiz")
                    finishQuiz()
                }
            } else {
                Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show()
            }
        }

        // Start timer
        startTime = System.currentTimeMillis()
    }

    private fun loadQuestions() {
        lifecycleScope.launch {
            try {
                // Log attempt to load questions
                Log.d(TAG, "Attempting to load questions for category: $categoryId")

                questions = FirebaseUtils.getQuestionsForCategory(categoryId)
                Log.d(TAG, "Received ${questions.size} questions from Firebase")

                if (questions.isEmpty()) {
                    // Create dummy questions if none exist
                    Log.d(TAG, "No questions found, creating dummy questions")
                    questions = createDummyQuestions()
                    Log.d(TAG, "Created ${questions.size} dummy questions")
                }

                // Set up progress seekbar
                seekBarProgress.max = questions.size
                seekBarProgress.progress = 0

                // Display first question
                displayQuestion()
            } catch (e: Exception) {
                Log.e(TAG, "Error loading questions: ${e.message}", e)
                Toast.makeText(this@QuizActivity, "Error loading questions: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun createDummyQuestions(): List<Question> {
        // Sample questions for each category (simplified for example)
        return when (categoryId) {
            "science" -> createScienceQuestions()
            "history" -> createHistoryQuestions()
            "sports" -> createSportsQuestions()
            "entertainment" -> createEntertainmentQuestions()
            else -> emptyList()
        }
    }

    private fun createScienceQuestions(): List<Question> {
        return List(10) { index ->
            Question(
                id = "science_$index",
                categoryId = "science",
                text = when (index) {
                    0 -> "What is the chemical symbol for gold?"
                    1 -> "Which planet is known as the Red Planet?"
                    2 -> "What is the hardest natural substance on Earth?"
                    3 -> "What is the largest organ in the human body?"
                    4 -> "Which gas do plants absorb from the atmosphere?"
                    5 -> "What is the speed of light in vacuum?"
                    6 -> "What is the chemical formula for water?"
                    7 -> "Which element has the atomic number 1?"
                    8 -> "What is the process by which plants make their food?"
                    9 -> "Which particle has a positive charge?"
                    else -> "Science question $index"
                },
                options = when (index) {
                    0 -> listOf("Au", "Ag", "Fe", "Cu")
                    1 -> listOf("Mars", "Venus", "Jupiter", "Saturn")
                    2 -> listOf("Diamond", "Platinum", "Steel", "Titanium")
                    3 -> listOf("Skin", "Heart", "Liver", "Brain")
                    4 -> listOf("Carbon dioxide", "Oxygen", "Nitrogen", "Helium")
                    5 -> listOf("300,000 km/s", "150,000 km/s", "200,000 km/s", "100,000 km/s")
                    6 -> listOf("H2O", "CO2", "O2", "H2SO4")
                    7 -> listOf("Hydrogen", "Helium", "Carbon", "Oxygen")
                    8 -> listOf("Photosynthesis", "Respiration", "Transpiration", "Fermentation")
                    9 -> listOf("Proton", "Electron", "Neutron", "Photon")
                    else -> listOf("Option A", "Option B", "Option C", "Option D")
                },
                correctOptionIndex = when (index) {
                    0 -> 0  // Au
                    1 -> 0  // Mars
                    2 -> 0  // Diamond
                    3 -> 0  // Skin
                    4 -> 0  // Carbon dioxide
                    5 -> 0  // 300,000 km/s
                    6 -> 0  // H2O
                    7 -> 0  // Hydrogen
                    8 -> 0  // Photosynthesis
                    9 -> 0  // Proton
                    else -> 0
                }
            )
        }
    }

    private fun createHistoryQuestions(): List<Question> {
        return List(10) { index ->
            Question(
                id = "history_$index",
                categoryId = "history",
                text = when (index) {
                    0 -> "In which year did World War II end?"
                    1 -> "Who was the first President of the United States?"
                    2 -> "Which ancient civilization built the Great Pyramids?"
                    3 -> "Which famous explorer first circumnavigated the globe?"
                    4 -> "Who wrote the Declaration of Independence?"
                    5 -> "In which year did the Berlin Wall fall?"
                    6 -> "Which empire was ruled by Genghis Khan?"
                    7 -> "Who was the first woman to win a Nobel Prize?"
                    8 -> "Which treaty ended World War I?"
                    9 -> "Who discovered penicillin?"
                    else -> "History question $index"
                },
                options = when (index) {
                    0 -> listOf("1945", "1944", "1946", "1943")
                    1 -> listOf("George Washington", "Thomas Jefferson", "Abraham Lincoln", "John Adams")
                    2 -> listOf("Egyptians", "Romans", "Greeks", "Maya")
                    3 -> listOf("Ferdinand Magellan", "Christopher Columbus", "Vasco da Gama", "James Cook")
                    4 -> listOf("Thomas Jefferson", "Benjamin Franklin", "George Washington", "John Adams")
                    5 -> listOf("1989", "1991", "1987", "1985")
                    6 -> listOf("Mongol Empire", "Roman Empire", "Ottoman Empire", "Byzantine Empire")
                    7 -> listOf("Marie Curie", "Rosalind Franklin", "Dorothy Hodgkin", "Ada Lovelace")
                    8 -> listOf("Treaty of Versailles", "Treaty of Paris", "Treaty of Rome", "Treaty of London")
                    9 -> listOf("Alexander Fleming", "Louis Pasteur", "Joseph Lister", "Robert Koch")
                    else -> listOf("Option A", "Option B", "Option C", "Option D")
                },
                correctOptionIndex = when (index) {
                    0 -> 0  // 1945
                    1 -> 0  // George Washington
                    2 -> 0  // Egyptians
                    3 -> 0  // Ferdinand Magellan
                    4 -> 0  // Thomas Jefferson
                    5 -> 0  // 1989
                    6 -> 0  // Mongol Empire
                    7 -> 0  // Marie Curie
                    8 -> 0  // Treaty of Versailles
                    9 -> 0  // Alexander Fleming
                    else -> 0
                }
            )
        }
    }

    private fun createSportsQuestions(): List<Question> {
        return List(10) { index ->
            Question(
                id = "sports_$index",
                categoryId = "sports",
                text = when (index) {
                    0 -> "In which sport would you perform a slam dunk?"
                    1 -> "How many players are there in a standard soccer team?"
                    2 -> "Which country has won the most FIFA World Cups?"
                    3 -> "How many rings are there in the Olympic flag?"
                    4 -> "Which sport uses the lightest ball?"
                    5 -> "In which sport would you find a 'peloton'?"
                    6 -> "Which swimming stroke is typically regarded as the fastest?"
                    7 -> "What is the diameter of a basketball hoop in inches?"
                    8 -> "Which Grand Slam tennis tournament is played on clay courts?"
                    9 -> "Which boxer was known as 'The Greatest'?"
                    else -> "Sports question $index"
                },
                options = when (index) {
                    0 -> listOf("Basketball", "Volleyball", "Tennis", "Soccer")
                    1 -> listOf("11", "10", "9", "12")
                    2 -> listOf("Brazil", "Germany", "Italy", "Argentina")
                    3 -> listOf("5", "4", "6", "3")
                    4 -> listOf("Table Tennis", "Tennis", "Baseball", "Cricket")
                    5 -> listOf("Cycling", "Rowing", "Marathon Running", "Swimming")
                    6 -> listOf("Freestyle", "Butterfly", "Backstroke", "Breaststroke")
                    7 -> listOf("18", "16", "20", "22")
                    8 -> listOf("French Open", "Wimbledon", "US Open", "Australian Open")
                    9 -> listOf("Muhammad Ali", "Mike Tyson", "Floyd Mayweather", "Evander Holyfield")
                    else -> listOf("Option A", "Option B", "Option C", "Option D")
                },
                correctOptionIndex = when (index) {
                    0 -> 0  // Basketball
                    1 -> 0  // 11
                    2 -> 0  // Brazil
                    3 -> 0  // 5
                    4 -> 0  // Table Tennis
                    5 -> 0  // Cycling
                    6 -> 0  // Freestyle
                    7 -> 0  // 18
                    8 -> 0  // French Open
                    9 -> 0  // Muhammad Ali
                    else -> 0
                }
            )
        }
    }

    private fun createEntertainmentQuestions(): List<Question> {
        return List(10) { index ->
            Question(
                id = "entertainment_$index",
                categoryId = "entertainment",
                text = when (index) {
                    0 -> "Which movie won the Oscar for Best Picture in 2020?"
                    1 -> "Who played Iron Man in the Marvel Cinematic Universe?"
                    2 -> "Which band released the album 'Abbey Road'?"
                    3 -> "Who wrote the Harry Potter series?"
                    4 -> "What was the highest-grossing movie of all time (without adjusting for inflation)?"
                    5 -> "Which character in Friends was a paleontologist?"
                    6 -> "Who is known as the 'King of Pop'?"
                    7 -> "Which TV show featured characters named Walter White and Jesse Pinkman?"
                    8 -> "Which actress played Katniss Everdeen in 'The Hunger Games'?"
                    9 -> "Who directed the 'Lord of the Rings' trilogy?"
                    else -> "Entertainment question $index"
                },
                options = when (index) {
                    0 -> listOf("Parasite", "1917", "Joker", "Once Upon a Time in Hollywood")
                    1 -> listOf("Robert Downey Jr.", "Chris Evans", "Chris Hemsworth", "Mark Ruffalo")
                    2 -> listOf("The Beatles", "The Rolling Stones", "Led Zeppelin", "Pink Floyd")
                    3 -> listOf("J.K. Rowling", "Stephenie Meyer", "George R.R. Martin", "Suzanne Collins")
                    4 -> listOf("Avatar", "Avengers: Endgame", "Titanic", "Star Wars: The Force Awakens")
                    5 -> listOf("Ross", "Chandler", "Joey", "Phoebe")
                    6 -> listOf("Michael Jackson", "Elvis Presley", "Prince", "David Bowie")
                    7 -> listOf("Breaking Bad", "The Walking Dead", "Game of Thrones", "The Sopranos")
                    8 -> listOf("Jennifer Lawrence", "Emma Watson", "Shailene Woodley", "Emma Stone")
                    9 -> listOf("Peter Jackson", "Steven Spielberg", "James Cameron", "Christopher Nolan")
                    else -> listOf("Option A", "Option B", "Option C", "Option D")
                },
                correctOptionIndex = when (index) {
                    0 -> 0  // Parasite
                    1 -> 0  // Robert Downey Jr.
                    2 -> 0  // The Beatles
                    3 -> 0  // J.K. Rowling
                    4 -> 0  // Avatar
                    5 -> 0  // Ross
                    6 -> 0  // Michael Jackson
                    7 -> 0  // Breaking Bad
                    8 -> 0  // Jennifer Lawrence
                    9 -> 0  // Peter Jackson
                    else -> 0
                }
            )
        }
    }

    private fun displayQuestion() {
        // Get current question
        val question = questions[currentQuestionIndex]
        Log.d(TAG, "Displaying question ${currentQuestionIndex + 1}/${questions.size}")

        // Update question text
        tvQuestionNumber.text = "Question ${currentQuestionIndex + 1}/${questions.size}"
        tvQuestion.text = question.text

        // Clear previous options
        radioGroupOptions.removeAllViews()

        // Add options as radio buttons
        question.options.forEachIndexed { index, option ->
            val radioButton = RadioButton(this)
            radioButton.id = View.generateViewId()
            radioButton.text = option
            radioButton.tag = index

            // Check if this question has been answered before
            if (question.isAnswered && question.selectedOption == index) {
                radioButton.isChecked = true
            }

            radioGroupOptions.addView(radioButton)
        }

        // Update progress
        seekBarProgress.progress = currentQuestionIndex + 1

        // Start timer for current question
        startQuestionTimer()
    }

    private fun startQuestionTimer() {
        // Cancel previous timer if exists
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }

        countDownTimer = object : CountDownTimer(questionTimeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                tvTimer.text = "Time: $secondsLeft s"
            }

            override fun onFinish() {
                // Time's up, move to next question
                if (!isFinishing) {
                    showTimeUpDialog()
                }
            }
        }.start()
    }

    private fun showTimeUpDialog() {
        AlertDialog.Builder(this)
            .setTitle("Time's Up!")
            .setMessage("You ran out of time for this question.")
            .setPositiveButton("Continue") { _, _ ->
                saveAnswer() // Save current selection if any
                if (currentQuestionIndex < questions.size - 1) {
                    currentQuestionIndex++
                    displayQuestion()
                } else {
                    Log.d(TAG, "Time's up on last question, finishing quiz")
                    finishQuiz()
                }
            }
            .setCancelable(false)
            .show()
    }

    private fun validateAnswer(): Boolean {
        // Check if an option is selected
        return radioGroupOptions.checkedRadioButtonId != -1
    }

    private fun saveAnswer() {
        // Get current question
        val question = questions[currentQuestionIndex]
        Log.d(TAG, "Saving answer for question ${currentQuestionIndex + 1}")

        // Get selected option
        val checkedId = radioGroupOptions.checkedRadioButtonId
        if (checkedId != -1) {
            val selectedRadioButton = findViewById<RadioButton>(checkedId)
            val selectedOptionIndex = selectedRadioButton.tag as Int

            // Update question with selection
            question.isAnswered = true
            question.selectedOption = selectedOptionIndex

            // Update score if correct
            if (selectedOptionIndex == question.correctOptionIndex) {
                score++
                Log.d(TAG, "Correct answer! New score: $score")
            }
        }

        // Cancel timer
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
    }

    private fun finishQuiz() {
        // Calculate time spent
        timeSpentInSeconds = ((System.currentTimeMillis() - startTime) / 1000).toInt()
        Log.d(TAG, "Quiz finished. Final score: $score, Time spent: $timeSpentInSeconds seconds")

        // Save result to Firestore
        saveResult()
    }

    private fun saveResult() {
        Log.d(TAG, "Attempting to save quiz result")
        lifecycleScope.launch {
            try {
                val user = FirebaseUtils.getCurrentUser()
                if (user != null) {
                    Log.d(TAG, "User found: ${user.username}")
                    val result = QuizResult(
                        userId = user.id,
                        username = user.username,
                        categoryId = categoryId,
                        categoryName = categoryName,
                        score = score,
                        totalQuestions = questions.size,
                        timeSpentInSeconds = timeSpentInSeconds
                    )

                    Log.d(TAG, "Saving result to Firebase")
                    val resultId = withContext(Dispatchers.IO) {
                        FirebaseUtils.saveQuizResult(result)
                    }
                    Log.d(TAG, "Result saved with ID: $resultId")

                    // Navigate to results screen
                    Log.d(TAG, "Navigating to results screen")
                    navigateToResults(result)
                } else {
                    Log.e(TAG, "Error: User not logged in")
                    Toast.makeText(this@QuizActivity, "Error: User not logged in", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving result: ${e.message}", e)
                Toast.makeText(this@QuizActivity, "Error saving result: ${e.message}", Toast.LENGTH_SHORT).show()

                // Even if saving fails, try to navigate to results
                try {
                    val result = QuizResult(
                        userId = "error",
                        username = "error",
                        categoryId = categoryId,
                        categoryName = categoryName,
                        score = score,
                        totalQuestions = questions.size,
                        timeSpentInSeconds = timeSpentInSeconds
                    )
                    navigateToResults(result)
                } catch (e2: Exception) {
                    Log.e(TAG, "Error navigating to results: ${e2.message}", e2)
                    finish()
                }
            }
        }
    }

    private fun navigateToResults(result: QuizResult) {
        Log.d(TAG, "Creating intent for ResultActivity")
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra("SCORE", result.score)
            putExtra("TOTAL_QUESTIONS", result.totalQuestions)
            putExtra("TIME_SPENT", result.timeSpentInSeconds)
            putExtra("CATEGORY_NAME", result.categoryName)
        }
        Log.d(TAG, "Starting ResultActivity")
        startActivity(intent)
        Log.d(TAG, "Finishing QuizActivity")
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "QuizActivity destroyed")
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
    }
}