package com.zuyatna.placebook.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.zuyatna.placebook.R
import com.zuyatna.placebook.databinding.ActivityBookmarkDetailsBinding
import com.zuyatna.placebook.viewmodel.BookmarkDetailsViewModel
import kotlinx.coroutines.DelicateCoroutinesApi

class BookmarkDetailsActivity : AppCompatActivity() {
    private lateinit var dataBinding: ActivityBookmarkDetailsBinding
    private val bookmarkDetailsViewModel by viewModels<BookmarkDetailsViewModel>()
    private var bookmarkDetailsView: BookmarkDetailsViewModel.BookmarkDetailsView? = null

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_bookmark_details)
        setupToolbar()
        getIntentData()
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

    @DelicateCoroutinesApi
    private fun getIntentData() {
        val bookmarkId = intent.getLongExtra(MapsActivity.EXTRA_BOOKMARK_ID, 0)

        bookmarkDetailsViewModel.getBookmark(bookmarkId)?.observe(this
        ) {
            it?.let {
                bookmarkDetailsView = it
                dataBinding.bookmarkDetailsView = it
                populateImageView()
            }
        }
    }

    private fun saveChanges() {
        val name = dataBinding.editTextName.text.toString()
        if (name.isEmpty()) {
            return
        }

        bookmarkDetailsView?.let { bookmarkView ->
            bookmarkView.name = dataBinding.editTextName.text.toString()
            bookmarkView.notes = dataBinding.editTextNotes.text.toString()
            bookmarkView.address = dataBinding.editTextAddress.text.toString()
            bookmarkView.phone = dataBinding.editTextPhone.text.toString()
            bookmarkDetailsViewModel.updateBookmark(bookmarkView)
        }
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bookmark_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.action_save -> {
                saveChanges()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
}