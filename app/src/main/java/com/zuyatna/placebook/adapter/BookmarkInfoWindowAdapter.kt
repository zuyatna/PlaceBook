package com.zuyatna.placebook.adapter

import android.app.Activity
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.zuyatna.placebook.databinding.ContentBookmarkInfoBinding
import com.zuyatna.placebook.ui.MapsActivity
import com.zuyatna.placebook.viewmodel.MapsViewModel

class BookmarkInfoWindowAdapter(val context: Activity) : GoogleMap.InfoWindowAdapter {

    private val binding = ContentBookmarkInfoBinding.inflate(context.layoutInflater)

    override fun getInfoContents(marker: Marker): View {
        binding.title.text = marker.title ?: ""
        binding.phone.text = marker.snippet ?: ""

        val imageView = binding.photo

        when(marker.tag) {
            is MapsActivity.PlaceInfo -> {
                imageView.setImageBitmap((marker.tag as MapsActivity.PlaceInfo).image)
            }

            is MapsViewModel.BookmarkMarkerView -> {
                val bookmarkView = marker.tag as MapsViewModel.BookmarkMarkerView
                imageView.setImageBitmap(bookmarkView.getImage(context))
            }
        }

        return binding.root
    }

    override fun getInfoWindow(marker: Marker): View? {
        // This function is required, but can return null if
        // not replacing the entire info window
        return null
    }
}