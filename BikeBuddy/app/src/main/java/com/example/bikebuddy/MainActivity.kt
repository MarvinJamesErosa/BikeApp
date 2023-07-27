package com.example.bikebuddy

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import com.example.bikebuddy.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import android.view.View
import android.widget.PopupWindow
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.ButtonBarLayout
import com.example.bikebuddy.Account
import com.example.bikebuddy.Community
import com.example.bikebuddy.Go
import com.example.bikebuddy.LoginActivity
import com.example.bikebuddy.SearchFragment
import com.example.bikebuddy.SearchListener
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.location.*
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AddressComponent
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), OnMapReadyCallback, SearchFragment.SearchListener {

    private lateinit var binding: ActivityMainBinding
    private var sharedRoutesPopup: PopupWindow? = null


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mMap: GoogleMap
    private lateinit var locationCallback: LocationCallback
    private lateinit var searchView: SearchView
    private lateinit var placesClient: PlacesClient
    private var isBottomNavigationViewVisible = true // Track the visibility state of BottomNavigationView


    companion object {
        private const val LOCATION_REQUEST_CODE = 145
        private const val LOCATION_SETTINGS_REQUEST_CODE = 25
        private val DEFAULT_ZOOM = 15f
    }

    private var zoomedToLocation: Boolean = false

    private val locationRequest: LocationRequest by lazy {
        LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    override fun onSearch(query: String) {
        convertLocationToLatLng(query)
        addToRecentSearches(query)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lock the orientation to portrait
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        runBlocking {
            installSplashScreen()
            delay(1500)
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(Go())

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFrag = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFrag.getMapAsync(this)


        binding.BottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.Gonav -> replaceFragment(Go())
                R.id.Community -> replaceFragment(Community())
                R.id.Account -> replaceFragment(Account())
            }
            true
        }
        try {
            val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            val bundle = appInfo.metaData
            val apiKey = bundle.getString("com.google.android.places.API_KEY")

            // Initialize Places with the API key
            Places.initialize(applicationContext, apiKey)
            placesClient = Places.createClient(this)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        if (!isBottomNavigationViewVisible) {
            val bottomNavigationView = findViewById<BottomNavigationView>(R.id.BottomNavigationView)
            bottomNavigationView.visibility = View.VISIBLE
            isBottomNavigationViewVisible = true
        } else {
            super.onBackPressed()
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        checkForLocationPermission()
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = false
        setUpLocationCallback()
        startLocationUpdates()
        centerMapToUserLocation()
    }
    // Declare a member variable to hold the reference to the previous marker
    private var previousMarker: Marker? = null

    fun convertLocationToLatLng(location: String) {
        // Remove the previous marker from the map
        previousMarker?.remove()

        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(location)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                if (response.autocompletePredictions.isNotEmpty()) {
                    val prediction = response.autocompletePredictions[0]
                    val placeId = prediction.placeId

                    val placeFields = listOf(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS_COMPONENTS)

                    val placeRequest = FetchPlaceRequest.builder(placeId, placeFields)
                        .build()

                    placesClient.fetchPlace(placeRequest)
                        .addOnSuccessListener { response: FetchPlaceResponse ->
                            val place = response.place
                            val name = place.name
                            val latLng = place.latLng
                            val addressComponents = place.addressComponents?.asList()
                            val country = getAddressComponent(addressComponents, "country")
                            val city = getAddressComponent(addressComponents, "locality") ?: ""

                            val latitude = latLng?.latitude
                            val longitude = latLng?.longitude

                            if (latitude != null && longitude != null && name != null) {
                                val title = when {
                                    city.isEmpty() || city == place.name || city == country -> {
                                        if (country != null && country != place.name) {
                                            "${place.name}, $country"
                                        } else {
                                            place.name
                                        }
                                    }
                                    else -> {
                                        if (country != null && country != place.name) {
                                            "${place.name}, $city, $country"
                                        } else {
                                            "${place.name}, $city"
                                        }
                                    }
                                }

                                val cameraPosition = CameraPosition.Builder()
                                    .target(LatLng(latitude, longitude))
                                    .zoom(DEFAULT_ZOOM)
                                    .build()
                                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                                previousMarker?.remove()

                                // Add the new marker to the map and store the reference
                                val newMarker = mMap.addMarker(
                                    MarkerOptions()
                                        .position(LatLng(latitude, longitude))
                                        .title(title)
                                )
                                previousMarker = newMarker

                                // Hide or remove the views you want to remove
                                findViewById<Button>(R.id.searchButton).visibility = View.GONE
                                // Hide or remove other views as needed
                                findViewById<BottomNavigationView>(R.id.BottomNavigationView).visibility = View.GONE

                                findViewById<ImageView>(R.id.account_topbar_text).visibility = View.GONE

                                findViewById<LinearLayout>(R.id.buttonLayout).visibility = View.GONE

                                findViewById<LinearLayout>(R.id.searchLayout).visibility = View.VISIBLE
                            }
                        }
                        .addOnFailureListener { exception: Exception ->
                            showDialog("Location Not Found", "The location cannot be found or does not exist.")
                        }
                } else {
                    // No predictions found for the location, handle accordingly
                    showDialog("Location Not Found", "The location cannot be found or does not exist.")
                }
            }
            .addOnFailureListener { exception: Exception ->
                showDialog("Error", "Failed to fetch location.")
            }
    }


    private fun getAddressComponent(components: List<AddressComponent>?, type: String): String? {
        components?.forEach { component ->
            component.types?.forEach { componentType ->
                if (componentType == type) {
                    return component.name
                }
            }
        }
        return null
    }
    private fun addToRecentSearches(query: String) {
        val sharedPrefs = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val recentSearches = sharedPrefs.getStringSet("RecentSearches", mutableSetOf<String>())
        recentSearches?.let {
            val newRecentSearches = mutableSetOf<String>()
            newRecentSearches.add(query)
            newRecentSearches.addAll(it)
            if (newRecentSearches.size > 5) {
                newRecentSearches.remove(newRecentSearches.last())
            }
            sharedPrefs.edit().putStringSet("RecentSearches", newRecentSearches).apply()
        }
    }

    private fun showDialog(title: String, message: String) {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
        alertDialog.show()
    }

    private fun checkForLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
        }
    }

    private fun setUpLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    if (!zoomedToLocation && locationResult.locations.isNotEmpty())  {
                        val currentLatLong = LatLng(location.latitude, location.longitude)
                        zoomedToLocation = true
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, DEFAULT_ZOOM))
                    }
                }
            }
        }
    }

    private fun startLocationUpdates() {
        val locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()

        val settingsClient = LocationServices.getSettingsClient(this)
        settingsClient.checkLocationSettings(locationSettingsRequest).addOnSuccessListener {
            checkForLocationPermission()
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        }.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    // Show dialog to enable location settings
                    exception.startResolutionForResult(
                        this@MainActivity,
                        LOCATION_SETTINGS_REQUEST_CODE
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_SETTINGS_REQUEST_CODE) {
            if (isLocationEnabled()) {
                centerMapToUserLocation()
            } else {
                startLocationUpdates()
            }
        }
    }

    fun centerMapToUserLocation() {
        checkForLocationPermission()
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val latLng = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM))
            } ?: run {
                startLocationUpdates()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback) //Stops location updates when the activity is paused to save battery
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.mapFragment, fragment)
        fragmentTransaction.commit()

        // Set the visibility state of BottomNavigationView when replacing the fragment
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.BottomNavigationView)
        bottomNavigationView.visibility = View.VISIBLE
        isBottomNavigationViewVisible = true
    }

    fun showSharedRoutesDialog(view: View) {
        // Handle the onClick event for SharedRoutes button
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val sharedRoutesView = inflater.inflate(R.layout.sharedroutespopup, null)

        sharedRoutesPopup = PopupWindow(sharedRoutesView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true)
        sharedRoutesPopup?.showAtLocation(sharedRoutesView, 0, 0, 0)

        val closeButton = sharedRoutesView.findViewById<Button>(R.id.closeButton)
        closeButton.setOnClickListener {
            sharedRoutesPopup?.dismiss()
        }
    }
}
