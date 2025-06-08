package com.firstproject.quizgame.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firstproject.quizgame.R
import com.firstproject.quizgame.models.Category


class CategoryAdapter(
    private val categories: List<Category>,
    private val onCategoryClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category)

        // Set click listener
        holder.itemView.setOnClickListener {
            onCategoryClick(category)
        }
    }

    override fun getItemCount(): Int = categories.size

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        private val tvCategoryDescription: TextView = itemView.findViewById(R.id.tvCategoryDescription)
        private val imgCategory: ImageView = itemView.findViewById(R.id.imgCategory)
        private val seekBarProgress: SeekBar = itemView.findViewById(R.id.seekBarProgress)

        fun bind(category: Category) {
            tvCategoryName.text = category.name
            tvCategoryDescription.text = category.description

            // Set category image
            if (category.imageResource != 0) {
                imgCategory.setImageResource(category.imageResource)
            }

            // Set progress
            seekBarProgress.max = 10 // Each category has 10 questions
            seekBarProgress.progress = category.progress
            seekBarProgress.isEnabled = false // Disable user interaction with seekbar
        }
    }
}