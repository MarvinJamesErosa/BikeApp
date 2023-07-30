package com.example.bikebuddy

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
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
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
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
import com.google.android.gms.maps.model.MarkerOptions
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
import android.location.Geocoder
import android.view.ViewGroup
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Polyline
import java.io.IOException
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.maps.android.PolyUtil
import okhttp3.*
import org.json.JSONObject


class MainActivity : AppCompatActivity(), OnMapReadyCallback, SearchFragment.SearchListener, Communicator {

    private lateinit var binding: ActivityMainBinding
    private var sharedRoutesPopup: PopupWindow? = null
    private var stringQuery: String? = null
    private var toggleStatus: Boolean = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mMap: GoogleMap
    private lateinit var locationCallback: LocationCallback
    private lateinit var placesClient: PlacesClient
    private var isBottomNavigationViewVisible = true // Track the visibility state of BottomNavigationView
    private var startingTextViewText: String? = null
    private var destinedTextViewText: String? = null
    private var openedSearchFrag: Boolean = false
    private var areTextViewsFilled = false
    private var bottomSheetView: View? = null
    private var destinedLocationMarker: Marker? = null
    private var currentLocationMarker: Marker? = null
    private var firstLocationMarker: Marker? = null
    private var currentPolyline: Polyline? = null
    private var bottomSheetDialog: BottomSheetDialog? = null

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
        stringQuery = query
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

        val mapFrag =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFrag.getMapAsync(this)



        binding.BottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.Gonav -> replaceFragment(Go())
                R.id.Account -> replaceFragment(Account())
            }
            true
        }
        try {
            val appInfo =
                packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            val bundle = appInfo.metaData
            val apiKey = bundle.getString("com.google.android.places.API_KEY")

            // Initialize Places with the API key
            Places.initialize(applicationContext, apiKey)
            placesClient = Places.createClient(this)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }



        // Find the GoNow button inside the bottom sheet view
        bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet, null)

    // Find the GoNow button inside the bottom sheet view
        val GoNow = bottomSheetView?.findViewById<Button>(R.id.GoNow)
        GoNow?.setOnClickListener { onGoNowButtonClick(it) }
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

    private fun convertLocationToLatLng(location: String) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(location)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                if (response.autocompletePredictions.isNotEmpty()) {
                    determineTextViewContent()
                    val prediction = response.autocompletePredictions[0]
                    val placeId = prediction.placeId

                    val placeFields = listOf(
                        Place.Field.NAME,
                        Place.Field.LAT_LNG,
                        Place.Field.ADDRESS_COMPONENTS
                    )

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
                                            "${place.name}"
                                        } else {
                                            place.name
                                        }
                                    }
                                    else -> {
                                        if (country != null && country != place.name) {
                                            "${place.name}, $city"
                                        } else {
                                            "${place.name}, $city"
                                        }
                                    }
                                }

                                if (toggleStatus && startingTextViewText != null) {
                                    // If the location is the user's current location, show the blue marker
                                    if (title != startingTextViewText) {
                                        currentLocationMarker?.remove()
                                        // Add a marker for the destined location
                                        currentLocationMarker = mMap.addMarker(
                                            MarkerOptions()
                                                .position(LatLng(latitude, longitude))
                                                .title(title)
                                        )

                                    }
                                }
                                else if(!openedSearchFrag)
                                {
                                    firstLocationMarker = mMap.addMarker(
                                        MarkerOptions()
                                            .position(LatLng(latitude, longitude))
                                            .title(title))
                                }
                                else {
                                    firstLocationMarker?.remove()
                                    destinedLocationMarker?.remove()
                                    // Add a marker for the destined location
                                    destinedLocationMarker = mMap.addMarker(
                                        MarkerOptions()
                                            .position(LatLng(latitude, longitude))
                                            .title(title)
                                    )
                                    determineTextViewContent()
                                }

                                // Hide or remove the views you want to remove
                                findViewById<Button>(R.id.searchButton).visibility = View.GONE
                                // Hide or remove other views as needed
                                findViewById<BottomNavigationView>(R.id.BottomNavigationView).visibility =
                                    View.GONE

                                findViewById<ImageView>(R.id.account_topbar_text).visibility =
                                    View.GONE

                                findViewById<LinearLayout>(R.id.buttonLayout).visibility = View.GONE

                                findViewById<LinearLayout>(R.id.searchLayout).visibility =
                                    View.VISIBLE
                            }
                        }
                        .addOnFailureListener { exception: Exception ->
                            showDialog(
                                "Location Not Found",
                                "The location cannot be found or does not exist."
                            )
                        }
                } else {
                    // No predictions found for the location, handle accordingly
                    showDialog(
                        "Location Not Found",
                        "The location cannot be found or does not exist."
                    )
                }
            }
            .addOnFailureListener { exception: Exception ->
                showDialog("Error", "Failed to fetch location.")
            }
    }

    private fun setDestinedLocationText() {
        val destinedLocation = findViewById<TextView>(R.id.destinedlocation)
        destinedLocation.text = stringQuery
    }

    private fun setStartingLocationText() {
        val startingLocation = findViewById<TextView>(R.id.startinglocation)
        startingLocation.text = stringQuery
    }
    private fun determineTextViewContent() {
        if (toggleStatus) {
            setStartingLocationText()
            startingTextViewText = stringQuery
        } else if (!toggleStatus && !openedSearchFrag) {
            openedSearchFrag = true
            checkForLocationPermission()
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latLng = LatLng(location.latitude, location.longitude)
                    val geocoder = Geocoder(this)
                    try {
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        if (addresses?.isNotEmpty() == true) { // Check if the list is not null and not empty
                            val address = addresses[0]
                            val addressText = address.getAddressLine(0)
                            findViewById<TextView>(R.id.startinglocation).text = addressText
                            startingTextViewText = addressText
                            areTextViewsFilled = startingTextViewText?.isNotEmpty() == true &&
                                    destinedTextViewText?.isNotEmpty() == true
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } ?: run {
                    startLocationUpdates()
                }
            }
        } else {
            setDestinedLocationText()
            destinedTextViewText = stringQuery

            // Check if both TextViews have text
            areTextViewsFilled = startingTextViewText?.isNotEmpty() == true &&
                    destinedTextViewText?.isNotEmpty() == true
        }

        fetchTextViewContentForDirections()

        // Show the bottom sheet if both TextViews have text
        if (areTextViewsFilled || !openedSearchFrag)
        {
            // Show your bottom sheet here
            // ...
            showBottomSheetLayout()
        }
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
        bottomSheetDialog = BottomSheetDialog(this) // Use 'this' instead of 'requireContext()'
        bottomSheetDialog?.setContentView(bottomSheetView!!)

        // Show the bottom sheet dialog
        bottomSheetDialog?.show()

        // Find the TextView with ID "textlocation" in the bottomSheetView
        val textLocationTextView = bottomSheetView?.findViewById<TextView>(R.id.textlocation)

        // Set the text of the "textlocation" TextView to the value of destinedTextViewText
        textLocationTextView?.text = destinedTextViewText
    }
    // Add a variable to keep track of the inflated view for activity_pedometer.xml
    //  private var pedometerView: View? = null

    fun onGoNowButtonClick(view: View) {
        bottomSheetDialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
        startTurnByTurnNavigation()
        // Remove all layouts apart from the map
        removeAllLayoutsApartFromMap()

        // Continue with any additional logic you want to perform after the button is clicked


      //  if (pedometerView == null) {
            // Inflate the activity_pedometer.xml layout
           // pedometerView = layoutInflater.inflate(R.layout.activity_pedometer, null)
        //}

        // Set the inflated view as the content view for MainActivity
        //setContentView(pedometerView)
    }


    private fun startTurnByTurnNavigation() {
        if ((startingTextViewText == null || destinedTextViewText == null) && openedSearchFrag) {
            // Handle the case when the starting location or destined location is not set
            showDialog("Error", "Please set both starting and destined locations.")
            return
        }

        // Fetch directions and draw the route on the map
        fetchDirections(startingTextViewText!!, destinedTextViewText!!)
    }
    private fun removeAllLayoutsApartFromMap() {
        // Hide or remove the views you want to remove
        findViewById<Button>(R.id.searchButton).visibility = View.GONE
        findViewById<BottomNavigationView>(R.id.BottomNavigationView).visibility = View.GONE
        findViewById<ImageView>(R.id.account_topbar_text).visibility = View.GONE
        findViewById<LinearLayout>(R.id.buttonLayout).visibility = View.GONE
        findViewById<LinearLayout>(R.id.searchLayout).visibility = View.GONE

    }


    private fun fetchTextViewContentForDirections()
    {
        if ((startingTextViewText != null && destinedTextViewText != null) || !openedSearchFrag) {
            // Fetch directions using Directions API
            fetchDirections(startingTextViewText!!, destinedTextViewText!!)
        }
    }

    private fun fetchDirections(origin: String, destination: String) {
        val apiKey = getString(R.string.google_directions_key)
        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${origin.replace(" ", "+")}" +
                "&destination=${destination.replace(" ", "+")}" +
                "&key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    showDialog("Error", "Failed to fetch directions.")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                response.body?.close()

                try {
                    val json = JSONObject(responseData)
                    val routes = json.getJSONArray("routes")
                    if (routes.length() > 0) {
                        val points = routes.getJSONObject(0)
                            .getJSONObject("overview_polyline")
                            .getString("points")
                        val decodedPoints = PolyUtil.decode(points)

                        runOnUiThread {
                            drawRouteOnMap(decodedPoints)
                        }
                    } else {
                        runOnUiThread {
                            showDialog("No Routes", "No routes found between the locations.")
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        showDialog("Error", "Failed to parse response data.")
                    }
                }
            }
        })
    }

    private fun drawRouteOnMap(decodedPoints: List<LatLng>) {
        val routeColor = Color.parseColor("#061BB0") // Specify the desired color here
        val polylineOptions = PolylineOptions()
            .addAll(decodedPoints)
            .color(routeColor)
            .width(12f)
            .geodesic(true)
        currentPolyline?.remove()
        // Add the new polyline to the map and store its reference
        currentPolyline = mMap.addPolyline(polylineOptions)

        // Calculate the bounds of the polyline
        val builder = LatLngBounds.Builder()
        for (point in decodedPoints) {
            builder.include(point)
        }
        val bounds = builder.build()

        // Calculate the padding to add to the bounds (optional)
        val padding = 20

        // Move the camera to the bounds with padding
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
        mMap.animateCamera(cameraUpdate)
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
                    if (!zoomedToLocation && locationResult.locations.isNotEmpty()) {
                        val currentLatLong = LatLng(location.latitude, location.longitude)
                        zoomedToLocation = true
                        mMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                currentLatLong,
                                DEFAULT_ZOOM
                            )
                        )
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

        sharedRoutesPopup = PopupWindow(
            sharedRoutesView,
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT,
            true
        )
        sharedRoutesPopup?.showAtLocation(sharedRoutesView, 0, 0, 0)

        val closeButton = sharedRoutesView.findViewById<Button>(R.id.closeButton)
        closeButton.setOnClickListener {
            sharedRoutesPopup?.dismiss()
        }
    }

    override fun passToggle(toggleInput: Boolean) {
        toggleStatus = toggleInput
    }

}


