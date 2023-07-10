package com.example.bikebuddy

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.SearchView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Go : Fragment(), SearchListener, SearchFragment.SearchListener {
    private var param1: String? = null
    private var param2: String? = null
    private var bottomSheetView: View? = null



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
            val mainActivityIntent = Intent(requireContext(), MainActivity::class.java)
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(mainActivityIntent)
            requireActivity().finish()
        }


        val startinglocation = view.findViewById<Button>(R.id.startinglocation)
        startinglocation.setOnClickListener {
            openSearchFragment()
        }

        val destinedlocation = view.findViewById<Button>(R.id.destinedlocation)
        destinedlocation.setOnClickListener {
            openSearchFragment()
        }

        val searchButton = view.findViewById<Button>(R.id.searchButton)
        searchButton.setOnClickListener {
            openSearchFragment()
        }

       // val searchView = view.findViewById<SearchView>(R.id.destinedlocation)
      //  searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        //    override fun onQueryTextSubmit(query: String): Boolean {
                // Handle the Enter key press event here
                // Show the bottom sheet layout
               // showBottomSheetLayout()
               // return true
          //  }

           // override fun onQueryTextChange(newText: String): Boolean {
                // Handle the text change event if needed
             //   return false
          //  }
        //})

    }

    override fun onResume() {
        super.onResume()

        val bottomNavigationView =
            requireActivity().findViewById<BottomNavigationView>(R.id.BottomNavigationView)
        bottomNavigationView.visibility = View.VISIBLE

       // val searchView =
           // requireActivity().findViewById<SearchView>(R.id.destinationlocation)
              //  searchView.visibility = View.GONE

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


    private fun showBottomSheetLayout() {
        // Inflate the bottom sheet layout
        if (bottomSheetView == null) {
            bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet, null)
        }

        // Check if the bottom sheet view already has a parent
        val parent = bottomSheetView?.parent
        if (parent is ViewGroup) {
            // Remove the bottom sheet view from its parent
            parent.removeView(bottomSheetView)
        }

        // Create a BottomSheetDialog and set the bottom sheet view
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(bottomSheetView!!)

        // Show the bottom sheet dialog
        bottomSheetDialog.show()
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