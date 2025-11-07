package com.example.spotfinder

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null

    private lateinit var etAddress: EditText
    private lateinit var etLatitude: EditText
    private lateinit var etLongitude: EditText
    private lateinit var btnSearch: Button
    private lateinit var btnAdd: Button
    private lateinit var btnUpdate: Button
    private lateinit var btnDelete: Button

    // Using our SQLiteOpenHelper-based DB
    private val db by lazy { SpotFinderDatabase.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // UI references
        etAddress = findViewById(R.id.etAddress)
        etLatitude = findViewById(R.id.etLatitude)
        etLongitude = findViewById(R.id.etLongitude)
        btnSearch = findViewById(R.id.btnSearch)
        btnAdd = findViewById(R.id.btnAdd)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnDelete = findViewById(R.id.btnDelete)

        // Button listeners
        btnSearch.setOnClickListener { onSearchClicked() }
        btnAdd.setOnClickListener { onAddClicked() }
        btnUpdate.setOnClickListener { onUpdateClicked() }
        btnDelete.setOnClickListener { onDeleteClicked() }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Initial the map on Toronto
        val toronto = LatLng(43.6532, -79.3832)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(toronto, 9f))
        googleMap?.uiSettings?.isZoomControlsEnabled = true

        // Allow user to pick location by tapping on the map
        googleMap?.setOnMapClickListener { latLng ->
            googleMap?.clear()
            googleMap?.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Picked location")
            )
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f))

            etLatitude.setText(latLng.latitude.toString())
            etLongitude.setText(latLng.longitude.toString())
        }
    }

    private fun onSearchClicked() {
        val address = etAddress.text.toString().trim()
        if (address.isEmpty()) {
            toast("Enter an address to search")
            return
        }

        lifecycleScope.launch {
            val location = withContext(Dispatchers.IO) {
                db.getByAddress(address)
            }

            if (location != null) {
                etLatitude.setText(location.latitude.toString())
                etLongitude.setText(location.longitude.toString())
                showOnMap(location.latitude, location.longitude, location.address)
            } else {
                toast("Address not found in database")
            }
        }
    }

    private fun onAddClicked() {
        val address = etAddress.text.toString().trim()
        val latStr = etLatitude.text.toString().trim()
        val lngStr = etLongitude.text.toString().trim()

        if (address.isEmpty() || latStr.isEmpty() || lngStr.isEmpty()) {
            toast("Fill address, latitude, and longitude")
            return
        }

        val lat = latStr.toDoubleOrNull()
        val lng = lngStr.toDoubleOrNull()
        if (lat == null || lng == null) {
            toast("Invalid latitude or longitude")
            return
        }

        lifecycleScope.launch {
            val existing = withContext(Dispatchers.IO) {
                db.getByAddress(address)
            }

            if (existing != null) {
                toast("Location already exists in the table")
                showOnMap(existing.latitude, existing.longitude, existing.address)
                return@launch
            }

            val newLocation = LocationEntity(
                address = address,
                latitude = lat,
                longitude = lng
            )

            withContext(Dispatchers.IO) {
                db.insert(newLocation)
            }

            toast("Location added")
            showOnMap(lat, lng, address)
        }
    }

    private fun onUpdateClicked() {
        val address = etAddress.text.toString().trim()
        val latStr = etLatitude.text.toString().trim()
        val lngStr = etLongitude.text.toString().trim()

        if (address.isEmpty() || latStr.isEmpty() || lngStr.isEmpty()) {
            toast("Fill address, latitude, and longitude")
            return
        }

        val lat = latStr.toDoubleOrNull()
        val lng = lngStr.toDoubleOrNull()
        if (lat == null || lng == null) {
            toast("Invalid latitude or longitude")
            return
        }

        lifecycleScope.launch {
            val rowsUpdated = withContext(Dispatchers.IO) {
                db.updateByAddress(address, lat, lng)
            }

            if (rowsUpdated > 0) {
                toast("Location updated")
                showOnMap(lat, lng, address)
            } else {
                toast("Address not found to update")
            }
        }
    }

    private fun onDeleteClicked() {
        val address = etAddress.text.toString().trim()
        if (address.isEmpty()) {
            toast("Enter an address to delete")
            return
        }

        lifecycleScope.launch {
            val rowsDeleted = withContext(Dispatchers.IO) {
                db.deleteByAddress(address)
            }

            if (rowsDeleted > 0) {
                toast("Location deleted")
            } else {
                toast("Address not found to delete")
            }
        }
    }

    private fun showOnMap(lat: Double, lng: Double, title: String) {
        val map = googleMap ?: return
        val pos = LatLng(lat, lng)
        map.clear()
        map.addMarker(MarkerOptions().position(pos).title(title))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 12f))
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
