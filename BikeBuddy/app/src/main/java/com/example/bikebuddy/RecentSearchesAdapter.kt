package com.example.bikebuddy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecentSearchesAdapter(  private var searches: List<String>,
                              private val clickListener: (String) -> Unit = {} // Click listener for suggestion items
) : RecyclerView.Adapter<RecentSearchesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recent_search, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val search = searches[position]
        holder.bind(search)
        holder.itemView.setOnClickListener { clickListener(search) } // Set click listener for each suggestion item
    }

    override fun getItemCount(): Int {
        return searches.size
    }

    fun setSearches(newSearches: List<String>) {
        searches = newSearches.reversed() // Reverse the order to show recent searches on top
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(search: String) {
            itemView.findViewById<TextView>(R.id.suggestionsViewSearch).text = search
        }
    }
}
