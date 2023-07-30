package com.example.bikebuddy

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Go : Fragment(), SearchListener, SearchFragment.SearchListener {
    private var param1: String? = null
    private var param2: String? = null
    private var toggleVal: Boolean = false
    private lateinit var communicator: Communicator



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


        val gobackButton = view.findViewById<Button>(R.id.gobackbutton)
        gobackButton.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
        }
        communicator = activity as Communicator

        val startinglocation = view.findViewById<TextView>(R.id.startinglocation)
        startinglocation.setOnClickListener {
            openSearchFragment()
            toggleVal = true
            communicator.passToggle(toggleVal)
        }


        val destinedlocation = view.findViewById<TextView>(R.id.destinedlocation)
        destinedlocation.setOnClickListener {
            openSearchFragment()
            toggleVal = false
            communicator.passToggle(toggleVal)
        }

        val searchButton = view.findViewById<Button>(R.id.searchButton)
        searchButton.setOnClickListener {
            openSearchFragment()

        }

    }


    override fun onResume() {
        super.onResume()

        val bottomNavigationView =
            requireActivity().findViewById<BottomNavigationView>(R.id.BottomNavigationView)
        bottomNavigationView.visibility = View.VISIBLE


        val linearLayout =
            requireActivity().findViewById<LinearLayout>(R.id.searchLayout)
                linearLayout.visibility = View.GONE

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