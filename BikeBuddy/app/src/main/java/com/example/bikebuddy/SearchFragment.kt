package com.example.bikebuddy

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class SearchFragment : Fragment() {

    var searchListener: SearchListener? = null
    private lateinit var placesClient: PlacesClient
    private lateinit var recentSearchesRecyclerView: RecyclerView
    private lateinit var recentSearchesAdapter: RecentSearchesAdapter
    private lateinit var suggestionsRecyclerView: RecyclerView
    private lateinit var autocompleteSuggestionsAdapter: AutocompleteSuggestionsAdapter
    private val recentSearchesList: MutableList<String> = mutableListOf()

    private val gson: Gson = Gson()
    private val recentSearchesType: Type = object : TypeToken<List<RecentSearch>>() {}.type
    private val PREF_RECENT_SEARCHES = "RecentSearches"

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
        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.google_maps_key))
        }
        placesClient = Places.createClient(requireContext())
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        val searchView = view.findViewById<SearchView>(R.id.searchViewLocation)
        suggestionsRecyclerView = view.findViewById<RecyclerView>(R.id.suggestionsRecyclerView)
        recentSearchesRecyclerView = view.findViewById(R.id.recentItemsRecyclerView)

        // Set up RecyclerView for autocomplete suggestions
        autocompleteSuggestionsAdapter = AutocompleteSuggestionsAdapter(emptyList()) { suggestion ->
            // Handle suggestion item click here
            searchListener?.onSearch(suggestion)
            onSearchSubmit(suggestion)
            requireActivity().supportFragmentManager.popBackStack()
        }
        suggestionsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        suggestionsRecyclerView.adapter = autocompleteSuggestionsAdapter

        searchView.isFocusableInTouchMode = true

        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        // Set query submit listener
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // Call the onSearch method of the parent activity passing the query
                searchListener?.onSearch(query)
                onSearchSubmit(query)
                // Redirect to the main screen
                requireActivity().supportFragmentManager.popBackStack()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isNotBlank()) {
                    // Hide the RecyclerView for recent searches when the user types something
                    recentSearchesRecyclerView.visibility = View.GONE
                    // Process the search query and show the autocomplete suggestions
                    ifQueryTextChanges(newText)
                } else {
                    // Show the RecyclerView for recent searches when the search query is empty
                    recentSearchesRecyclerView.visibility = View.VISIBLE
                    // Show the recent searches list again
                    loadRecentSearches()
                    // Clear the autocomplete suggestions UI when search bar text is cleared
                    autocompleteSuggestionsAdapter.setSearches(emptyList())
                    // Hide the RecyclerView for autocomplete suggestions
                    suggestionsRecyclerView.visibility = View.GONE
                }
                return true
            }
        })

        // Set key press listener
        searchView.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                // Redirect to the main screen
                requireActivity().supportFragmentManager.popBackStack()
                searchView.requestFocus()
                return@setOnKeyListener true
            }
            false
        }

        return view
    }

    private fun onSearchSubmit(query: String) {
        // Check if the query already exists in recent searches
        val existingIndex = recentSearchesList.indexOf(query)

        // If the query exists, remove it from the current position
        if (existingIndex != -1) {
            recentSearchesList.removeAt(existingIndex)
        }

        // Add the search query to the top of recent searches
        recentSearchesList.add(0, query)

        // Limit the recent searches list to 5 items
        if (recentSearchesList.size > 5) {
            recentSearchesList.removeAt(recentSearchesList.lastIndex)
        }

        // Update the adapter's data with the new recent searches and show the most recent on top
        recentSearchesAdapter.setSearches(recentSearchesList)
        recentSearchesAdapter.notifyDataSetChanged()

        // Serialize recent searches list and save it in SharedPreferences
        val recentSearchesJson = gson.toJson(recentSearchesList.map { RecentSearch(it) })
        val sharedPrefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putString(PREF_RECENT_SEARCHES, recentSearchesJson).apply()

        // Call the onSearch method of the parent activity passing the query
        searchListener?.onSearch(query)

        // Redirect to the main screen
        requireActivity().supportFragmentManager.popBackStack()
    }

    private fun removeRecentSearch(query: String) {
        val existingIndex = recentSearchesList.indexOf(query)
        if (existingIndex != -1) {
            recentSearchesList.removeAt(existingIndex)
            recentSearchesAdapter.setSearches(recentSearchesList)
            recentSearchesAdapter.notifyDataSetChanged()

            // Serialize recent searches list and save it in SharedPreferences
            val recentSearchesJson = gson.toJson(recentSearchesList.map { RecentSearch(it) })
            val sharedPrefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            sharedPrefs.edit().putString(PREF_RECENT_SEARCHES, recentSearchesJson).apply()
        }
    }

    private fun loadRecentSearches() {
        val sharedPrefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val recentSearchesJson = sharedPrefs.getString(PREF_RECENT_SEARCHES, null)
        recentSearchesJson?.let {
            val recentSearches: List<RecentSearch> = gson.fromJson(it, recentSearchesType)
            recentSearchesList.clear()
            recentSearchesList.addAll(recentSearches.map { recentSearch -> recentSearch.query })
        }
        // Initialize the RecyclerView with the loaded recent searches and set the remove click listener
        recentSearchesAdapter = RecentSearchesAdapter(recentSearchesList,
            { suggestion ->
                searchListener?.onSearch(suggestion)
                onSearchSubmit(suggestion)
                requireActivity().supportFragmentManager.popBackStack()
            },
            { query -> removeRecentSearch(query) }, // Pass the removeRecentSearch method to the adapter
            showRemoveButton = true // Set showRemoveButton to false for the suggestionsAdapter
        )

        recentSearchesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recentSearchesAdapter
        }
        recentSearchesAdapter.setSearches(recentSearchesList)
        recentSearchesAdapter.notifyDataSetChanged()
    }



    private fun ifQueryTextChanges(newText: String) {
        if (newText.isNotBlank()) {
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(newText)
                .setCountry("PH") // Use "PH" for the Philippines
                .build()

            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                    val predictions = response.autocompletePredictions
                    val filteredPredictions = mutableListOf<String>()

                    for (prediction in predictions) {
                        val primaryText = prediction.getPrimaryText(null).toString()
                        val secondaryText = prediction.getSecondaryText(null).toString()

                        // Include the autocomplete suggestion only if the primary text and
                        // secondary text are different from the query
                        if (!primaryText.equals(newText, ignoreCase = true) && !secondaryText.equals(newText, ignoreCase = true)) {
                            filteredPredictions.add("${prediction.getPrimaryText(null)}, ${prediction.getSecondaryText(null)}")
                        }
                    }

                    // Update the adapter's data with the new places (excluding the city itself)
                    autocompleteSuggestionsAdapter.setSearches(filteredPredictions.sorted())
                    // Show the RecyclerView for autocomplete suggestions
                    suggestionsRecyclerView.visibility = View.VISIBLE
                }
                .addOnFailureListener { exception: Exception ->
                    // Handle autocomplete failure
                    // Hide the RecyclerView for autocomplete suggestions
                    suggestionsRecyclerView.visibility = View.GONE
                }
        } else {
            // Clear the autocomplete suggestions UI
            autocompleteSuggestionsAdapter.setSearches(emptyList())
            // Hide the RecyclerView for autocomplete suggestions
            suggestionsRecyclerView.visibility = View.GONE
        }
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

        // Load and display recent searches when the fragment is created
        loadRecentSearches()

        val returnButton = view.findViewById<Button>(R.id.returnbutton)
        returnButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

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
