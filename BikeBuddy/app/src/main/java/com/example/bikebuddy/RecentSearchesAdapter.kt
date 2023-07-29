package com.example.bikebuddy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageButton

data class RecentSearch(val query: String)

class RecentSearchesAdapter(  private var searches: List<String>,
                              private val clickListener: (String) -> Unit = {}, // Click listener for suggestion items
                              private val removeClickListener: (String) -> Unit, // Add a click listener for the remove button
                              private val showRemoveButton: Boolean = false
) : RecyclerView.Adapter<RecentSearchesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recent_search, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val search = searches[position]
        holder.bind(search)
        holder.itemView.setOnClickListener { clickListener(search) } // Set click listener for each suggestion item

        // Show or hide the "remove" button based on the flag
        holder.itemView.findViewById<ImageButton>(R.id.removeRecentSearchButton).apply {
            visibility = if (showRemoveButton) View.VISIBLE else View.GONE
            if (showRemoveButton) {
                setOnClickListener { removeClickListener(search) }
            }
        }

        // Set click listener for the suggestion item (if showRemoveButton is false)
        if (!showRemoveButton) {
            holder.itemView.setOnClickListener { clickListener(search) }
        }
    }

    override fun getItemCount(): Int {
        return searches.size
    }

    fun setSearches(newSearches: List<String>) {
        searches = newSearches
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(search: String) {
            itemView.findViewById<TextView>(R.id.recentSearchesTextView).text = search
        }
    }
}
