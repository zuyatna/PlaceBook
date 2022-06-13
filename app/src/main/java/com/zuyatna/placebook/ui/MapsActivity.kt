package com.zuyatna.placebook.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.zuyatna.placebook.R
import com.zuyatna.placebook.adapter.BookmarkInfoWindowAdapter
import com.zuyatna.placebook.adapter.BookmarkListAdapter
import com.zuyatna.placebook.databinding.ActivityMapsBinding
import com.zuyatna.placebook.viewmodel.MapsViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@DelicateCoroutinesApi
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var binding: ActivityMapsBinding
    private lateinit var placesClient: PlacesClient
    private lateinit var bookmarkListAdapter: BookmarkListAdapter

    private val mapsViewModel by viewModels<MapsViewModel>()
    private var markers = HashMap<Long, Marker>()

    companion object {
        const val EXTRA_BOOKMARK_ID = "com.zuyatna.placebook.EXTRA_BOOKMARK_ID"
        private const val REQUEST_LOCATION = 1
        private const val TAG = "MapsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupLocationClient()
        setupToolbar()
        setupPlacesClient()
        setupNavigationDrawer()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setupMapListeners()
        createBookmarkObserver()
        getCurrentLocation()
    }

    private fun setupPlacesClient() {
        Places.initialize(applicationContext, resources.getString(R.string.google_maps_api))
        placesClient = Places.createClient(this)
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Log.e(TAG, "Location permission denied")
            }
        }
    }

    private fun setupLocationClient() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION
        )
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermissions()
        } else {
            map.isMyLocationEnabled = true

            fusedLocationProviderClient.lastLocation.addOnCompleteListener {
                val location = it.result

                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    val update = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)

                    map.moveCamera(update)
                } else {
                    Log.e(TAG, "No location found")
                }
            }
        }
    }

    private fun displayPoi(pointOfInterest: PointOfInterest) {
        displayPoiGetPlaceStep(pointOfInterest)
    }

    private fun displayPoiGetPhotoStep(place: Place) {
        val photoMetaData = place
            .photoMetadatas?.get(0)

        if (photoMetaData == null) {
            displayPoiDisplayStep(place, null)
            return
        }

        val photoRequest = FetchPhotoRequest
            .builder(photoMetaData)
            .setMaxWidth(resources.getDimensionPixelSize(R.dimen.default_image_width))
            .setMaxHeight(resources.getDimensionPixelSize(R.dimen.default_image_height))
            .build()

        placesClient.fetchPhoto(photoRequest)
            .addOnSuccessListener { fetchPhotoResponse ->
                val bitmap = fetchPhotoResponse.bitmap
                displayPoiDisplayStep(place, bitmap)
            }.addOnFailureListener { exception ->
                if (exception is ApiException) {
                    val statusCode = exception.statusCode
                    Log.e(TAG,
                        "Place not found: " + exception.message + ", " + "statusCode: " + statusCode)
                }
            }
    }

    private fun displayPoiGetPlaceStep(pointOfInterest: PointOfInterest) {
        val placeId = pointOfInterest.placeId
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.PHONE_NUMBER,
            Place.Field.PHOTO_METADATAS,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )

        val request = FetchPlaceRequest
            .builder(placeId, placeFields)
            .build()

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
                displayPoiGetPhotoStep(place)
            }.addOnFailureListener { exception ->
                if (exception is ApiException) {
                    val statusCode = exception.statusCode
                    Log.e(TAG,
                        "Place not found: " + exception.message + ", " + "statusCode: " + statusCode
                    )
                }
            }
    }

    private fun displayPoiDisplayStep(place: Place, photo: Bitmap?) {
        val marker = map.addMarker(MarkerOptions()
            .position(place.latLng as LatLng)
            .title(place.name)
            .snippet(place.phoneNumber)
        )

        marker?.tag = PlaceInfo(place, photo)
        marker?.showInfoWindow()
    }

    private fun setupMapListeners() {
        map.setInfoWindowAdapter(BookmarkInfoWindowAdapter(this))

        map.setOnPoiClickListener {
            displayPoi(it)
        }

        map.setOnInfoWindowClickListener {
            handleInfoWindowsClick(it)
        }
    }

    private fun handleInfoWindowsClick(marker: Marker) {

        when (marker.tag) {
            is PlaceInfo -> {
                val placeInfo = (marker.tag as PlaceInfo)
                if (placeInfo.place != null && placeInfo.image != null) {
                    GlobalScope.launch {
                        mapsViewModel.addBookmarkFromPlace(placeInfo.place, placeInfo.image)
                    }
                }
                marker.remove()
            }
            is MapsViewModel.BookmarkView -> {
                val bookmarkView = (marker.tag as MapsViewModel.BookmarkView)
                marker.hideInfoWindow()
                bookmarkView.id?.let {
                    startBookmarkDetails(it)
                }
            }
        }
    }

    private fun addPlaceMarker(bookmark: MapsViewModel.BookmarkView) : Marker? {
        val marker = map.addMarker(MarkerOptions()
            .position(bookmark.location)
            .title(bookmark.name)
            .snippet(bookmark.phone)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            .alpha(0.8f)
        )

        if (marker != null) {
            marker.tag = bookmark
        }

        bookmark.id?.let {
            if (marker != null) {
                markers[it] = marker
            }
        }

        return marker
    }

    private fun displayAllBookmarks(bookmarks: List<MapsViewModel.BookmarkView>) {
        bookmarks.forEach { addPlaceMarker(it) }
    }

    private fun createBookmarkObserver() {
        mapsViewModel.getBookmarkViews()?.observe(
            this
        ) {
            map.clear()
            markers.clear()
            it?.let {
                displayAllBookmarks(it)
                bookmarkListAdapter.setBookmarkData(it)
            }
        }
    }

    private fun startBookmarkDetails(bookmarkId: Long) {
        val intent = Intent(this, BookmarkDetailsActivity::class.java)
        intent.putExtra(EXTRA_BOOKMARK_ID, bookmarkId)
        startActivity(intent)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.mainMapView.toolbar)

        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.mainMapView.toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )
        toggle.syncState()
    }

    private fun setupNavigationDrawer() {
        val layoutManager = LinearLayoutManager(this)
        binding.drawerViewMaps.bookmarkRecyclerView.layoutManager = layoutManager
        bookmarkListAdapter = BookmarkListAdapter(null, this)
        binding.drawerViewMaps.bookmarkRecyclerView.adapter = bookmarkListAdapter
    }

    private fun updateMapToLocation(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f))
    }

    fun moveToBookmark(bookmark: MapsViewModel.BookmarkView) {
        binding.drawerLayout.closeDrawer(binding.drawerViewMaps.drawerView)

        val marker = markers[bookmark.id]
        marker?.showInfoWindow()

        val location = Location("")
        location.latitude = bookmark.location.latitude
        location.longitude = bookmark.location.longitude

        updateMapToLocation(location)
    }

    class PlaceInfo(val place: Place? = null, val image: Bitmap? = null)
}