package com.example.bikebuddy

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
import com.example.bikebuddy.Account
import com.example.bikebuddy.Community
import com.example.bikebuddy.Go
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap


class MainActivity : AppCompatActivity() {

    private val locationProvider = LocationProvider(this)
    private val permissionManager = PermissionsManager(this, locationProvider)

    private lateinit var binding: ActivityMainBinding
    private var sharedRoutesPopup: PopupWindow? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runBlocking {
            installSplashScreen()
            delay(1500)
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(Go())

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
    }
    fun onMapReady(googleMap: GoogleMap) {

        locationProvider.liveLocation.observe(this) { latLng ->
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
        }

        val permissionManager = PermissionsManager(this, locationProvider)
        permissionManager.requestUserLocation()

        googleMap.uiSettings.isZoomControlsEnabled = true
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.mapFragment, fragment)
        fragmentTransaction.commit()
    }

    fun goToMainScreen(view: View) {
        // Handle the onClick event for Explore button
        // Implement the logic to go back to the main screen here
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
