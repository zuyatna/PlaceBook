package com.zuyatna.placebook.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.zuyatna.placebook.R
import com.zuyatna.placebook.databinding.ActivityBookmarkDetailsBinding
import com.zuyatna.placebook.util.ImageUtils
import com.zuyatna.placebook.viewmodel.BookmarkDetailsViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import java.io.File

class BookmarkDetailsActivity : AppCompatActivity(), PhotoOptionDialogFragment.PhotoOptionDialogListener {
    private lateinit var dataBinding: ActivityBookmarkDetailsBinding
    private val bookmarkDetailsViewModel by viewModels<BookmarkDetailsViewModel>()
    private var bookmarkDetailsView: BookmarkDetailsViewModel.BookmarkDetailsView? = null
    private var photoFile: File? = null

    companion object {
        private const val REQUEST_CAPTURE_IMAGE = 1
    }

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

        dataBinding.imageViewPlace.setOnClickListener {
            replaceImage()
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

    private fun replaceImage() {
        val newFragment = PhotoOptionDialogFragment.newInstance(this)
        newFragment?.show(supportFragmentManager, "photoOptionDialog")
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

    override fun onCaptureClick() {
        photoFile = null

        try {
            photoFile = ImageUtils.createUniqueImageFile(this)
        } catch (ex: java.io.IOException) {
            return
        }

        photoFile?.let { photoFile ->
            val photoUri = FileProvider.getUriForFile(this, "com.zuyatna.placebook.FileProvider", photoFile)
            val captureIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            captureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri)

            val intentActivities = packageManager.queryIntentActivities(captureIntent, PackageManager.MATCH_DEFAULT_ONLY)
            intentActivities.map { it.activityInfo.packageName }
                .forEach { grantUriPermission(it, photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION) }

            startActivityForResult(captureIntent, REQUEST_CAPTURE_IMAGE)

//            val getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//                if (it.resultCode == Activity.RESULT_OK) {
//                    val value = it.data?.getStringExtra("input")
//                }
//            }
//
//            getResult.launch(captureIntent)
        }
    }

    override fun onPickClick() {
        Toast.makeText(this, "Gallery Pick", Toast.LENGTH_SHORT).show()
    }
}