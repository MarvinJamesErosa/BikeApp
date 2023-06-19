package com.example.bikebuddy

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Camera
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
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
import androidx.core.app.ActivityCompat
import com.example.bikebuddy.Account
import com.example.bikebuddy.Community
import com.example.bikebuddy.Go
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding
    private var sharedRoutesPopup: PopupWindow? = null

    private lateinit var mMap: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient : FusedLocationProviderClient

    companion object {
        private const val LOCATION_REQUEST_CODE = 1
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

        val mapFrag = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFrag.getMapAsync(this)

        binding.BottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.Gonav -> replaceFragment(Go())
                R.id.Community -> replaceFragment(Community())
                R.id.Account -> replaceFragment(Account())
                else -> {
                }
            }
            true
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true

        setUpMap()

    }

    private fun setUpMap(){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED)
            { ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
            return
        }

        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null)
                lastLocation = location
            val currentLatLong = LatLng(location.latitude, location.longitude)
            mMap.setMyLocationEnabled(true)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 14f))
        }
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
