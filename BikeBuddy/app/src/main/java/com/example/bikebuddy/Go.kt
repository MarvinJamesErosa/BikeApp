package com.example.bikebuddy

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import com.example.bikebuddy.SearchListener
import com.google.android.material.bottomnavigation.BottomNavigationView

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Go : Fragment(), SearchListener, SearchFragment.SearchListener {
    private var param1: String? = null
    private var param2: String? = null
    private var searchListener: SearchListener? = null

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
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchButton = view.findViewById<ImageButton>(R.id.searchButton)
        searchButton.setOnClickListener {
            openSearchFragment()
        }
    }

    override fun onResume() {
        super.onResume()

        val bottomNavigationView =
            requireActivity().findViewById<BottomNavigationView>(R.id.BottomNavigationView)
        bottomNavigationView.visibility = View.VISIBLE
    }

    override fun onSearch(query: String) {
        // Update the map in Go fragment with the entered location
        // You can access the map and update it according to your implementation
        // For example:
        // mapFragment.updateLocation(query)

        // Show the Go fragment with the map
        parentFragmentManager.popBackStack()
    }


    private fun openSearchFragment() {
        val searchFragment = SearchFragment.newInstance()
        searchFragment.searchListener = this // Pass the listener to communicate back
        parentFragmentManager.beginTransaction()
            .replace(R.id.MainActivity, searchFragment)
            .addToBackStack(null)
            .commit()
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