package com.firstproject.quizgame.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firstproject.quizgame.R
import com.firstproject.quizgame.models.QuizResult

class ResultAdapter(
    private val results: List<QuizResult>
) : RecyclerView.Adapter<ResultAdapter.ResultViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_result, parent, false)
        return ResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        val result = results[position]
        holder.bind(result)
    }

    override fun getItemCount(): Int = results.size

    inner class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvResultCategory: TextView = itemView.findViewById(R.id.tvResultCategory)
        private val tvResultScore: TextView = itemView.findViewById(R.id.tvResultScore)
        private val tvResultTime: TextView = itemView.findViewById(R.id.tvResultTime)
        private val tvResultDate: TextView = itemView.findViewById(R.id.tvResultDate)
        private val tvResultPerformance: TextView = itemView.findViewById(R.id.tvResultPerformance)

        fun bind(result: QuizResult) {
            tvResultCategory.text = result.categoryName
            tvResultScore.text = "Score: ${result.score}/${result.totalQuestions}"

            // Format time
            val minutes = result.timeSpentInSeconds / 60
            val seconds = result.timeSpentInSeconds % 60
            tvResultTime.text = "Time: ${if (minutes > 0) "$minutes min " else ""}$seconds sec"

            // Format date
            tvResultDate.text = "Date: ${result.dateCompleted}"

            // Calculate and display performance
            val performanceScore = result.calculatePerformanceScore()
            tvResultPerformance.text = "Performance: $performanceScore%"
        }
    }
}