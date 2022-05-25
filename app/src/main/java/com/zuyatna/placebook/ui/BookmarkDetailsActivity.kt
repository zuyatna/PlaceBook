package com.zuyatna.placebook.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.zuyatna.placebook.R
import com.zuyatna.placebook.databinding.ActivityBookmarkDetailsBinding
import com.zuyatna.placebook.viewmodel.BookmarkDetailsViewModel

class BookmarkDetailsActivity : AppCompatActivity() {
    private lateinit var dataBinding: ActivityBookmarkDetailsBinding
    private val bookmarkDetailsViewModel by viewModels<BookmarkDetailsViewModel>()
    private var bookmarkDetailsView: BookmarkDetailsViewModel.BookmarkDetailsView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_bookmark_details)
        setupToolbar()
    }

    private fun setupToolbar() {
        setSupportActionBar(dataBinding.toolbar)
    }

    private fun populateImageView() {
        bookmarkDetailsView?.let { bookmarkView ->
            val placeImage = bookmarkView.getImage(this)
            placeImage?.let {
                dataBinding.imageViewPlace.setImageBitmap(placeImage)
            }
        }
    }
}