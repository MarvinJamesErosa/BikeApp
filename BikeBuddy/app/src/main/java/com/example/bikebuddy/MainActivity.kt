package com.example.bikebuddy

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
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
import androidx.appcompat.app.AlertDialog
import com.example.bikebuddy.Account
import com.example.bikebuddy.Community
import com.example.bikebuddy.Go
import com.example.bikebuddy.LoginActivity
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

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding
    private var sharedRoutesPopup: PopupWindow? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mMap: GoogleMap
    private lateinit var locationCallback: LocationCallback

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runBlocking {
            installSplashScreen()
            delay(1500)
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(Go())

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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        checkForLocationPermission()
        mMap.isMyLocationEnabled = true
        setUpLocationCallback()
        startLocationUpdates()
        centerMapToUserLocation()
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
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
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
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.mapFragment, fragment)
        fragmentTransaction.commit()
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
