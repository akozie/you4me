package com.you4me.you4me.adapter.languageSelection

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import androidx.recyclerview.widget.RecyclerView

class LanguageAdapter(
    private val languages: List<String>,
    private val onLanguageSelected: (String) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

    private var selectedPosition = 0 // default to first language

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_single_choice, parent, false)
        return LanguageViewHolder(view)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val language = languages[position]
        holder.bind(language, position == selectedPosition)

        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousPosition)
            notifyItemChanged(position)

            onLanguageSelected(language)
        }
    }

    override fun getItemCount(): Int = languages.size

    class LanguageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkedTextView: CheckedTextView = itemView as CheckedTextView

        fun bind(language: String, isSelected: Boolean) {
            checkedTextView.text = language
            checkedTextView.isChecked = isSelected
        }
    }
}
