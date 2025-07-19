package com.you4me.you4me.adapter.mydates

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.you4me.you4me.R
import com.you4me.you4me.models.mydates.DateOption

class DateTypeAdapter(
    context: Context,
    private val items: List<DateOption>
    ) : ArrayAdapter<DateOption>(context, 0, items) {

        // View for the closed spinner (selected item)
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return createItemView(position, convertView, parent)
        }

        // View for the dropdown list
        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            return createItemView(position, convertView, parent)
        }

        private fun createItemView(position: Int, recycledView: View?, parent: ViewGroup): View {
            val view = recycledView ?: LayoutInflater.from(context).inflate(R.layout.date_type_dropdown_item, parent, false)

            val item = items[position]

            val icon = view.findViewById<ImageView>(R.id.icon)
            val title = view.findViewById<TextView>(R.id.title)
            val description = view.findViewById<TextView>(R.id.subtitle)

            if (item.iconResId != 0) {
                icon.setImageResource(item.iconResId)
                icon.visibility = View.VISIBLE
            } else {
                icon.visibility = View.GONE
            }

            title.text = item.title
            description.text = item.subtitle

            return view
        }
    }
