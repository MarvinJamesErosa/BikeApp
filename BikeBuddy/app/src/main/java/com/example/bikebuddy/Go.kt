package com.example.bikebuddy

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SearchView
import com.example.bikebuddy.SearchListener

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Go : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var searchListener: SearchListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Check if the parent activity implements the SearchListener interface
        if (context is SearchListener) {
            searchListener = context
        } else {
            throw IllegalStateException("Parent activity must implement SearchListener")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_go, container, false)
        val recenterButton = view.findViewById<Button>(R.id.recenterButton)
        recenterButton.setOnClickListener {
            (activity as MainActivity).centerMapToUserLocation()
        }
        val searchView = view.findViewById<SearchView>(R.id.searchViewLocation)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // Call the onSearch method of the searchListener
                searchListener?.onSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                // Called when the user changes the search query text
                // Perform any actions you want with the updated query text
                return true
            }
        })
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Go().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
