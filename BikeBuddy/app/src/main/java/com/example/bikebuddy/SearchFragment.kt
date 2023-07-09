package com.example.bikebuddy

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import com.example.bikebuddy.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class SearchFragment : Fragment() {

    var searchListener: SearchListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Check if the parent activity implements the SearchListener interface
        if (context is SearchListener) {
            searchListener = context
        } else {
            throw IllegalStateException("Parent activity must implement SearchListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        val searchView = view.findViewById<SearchView>(R.id.searchViewLocation)

        // Set query submit listener
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // Call the onSearch method of the parent activity passing the query
                searchListener?.onSearch(query)
                // Redirect to the main screen
                requireActivity().supportFragmentManager.popBackStack()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                // Called when the user changes the search query text
                // Perform any actions you want with the updated query text
                return true
            }
        })

        // Set key press listener
        searchView.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                // Redirect to the main screen
                requireActivity().supportFragmentManager.popBackStack()
                return@setOnKeyListener true
            }
            false
        }

        return view
    }

    interface SearchListener {
        fun onSearch(query: String)
    }

    companion object {
        @JvmStatic
        fun newInstance() = SearchFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomNavigationView =
            requireActivity().findViewById<BottomNavigationView>(R.id.BottomNavigationView)
        bottomNavigationView.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val bottomNavigationView =
            requireActivity().findViewById<BottomNavigationView>(R.id.BottomNavigationView)
        bottomNavigationView.visibility = View.VISIBLE
    }
}
