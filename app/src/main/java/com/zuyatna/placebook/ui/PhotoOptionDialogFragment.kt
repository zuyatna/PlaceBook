package com.zuyatna.placebook.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.DialogFragment

class PhotoOptionDialogFragment : DialogFragment() {
    interface PhotoOptionDialogListener {
        fun onCaptureClick()
        fun onPickClick()
    }

    private lateinit var listener: PhotoOptionDialogListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        listener = activity as PhotoOptionDialogListener

        var captureSelectIdx = -1
        var pickSelectIdx = -1

        val options = ArrayList<String>()
        val context =  activity as Context

        if (canCapture(context)) {
            options.add("Camera")
            captureSelectIdx = 0
        }

        if (canPick(context)) {
            options.add("Gallery")
            pickSelectIdx = if (captureSelectIdx == 0) 1 else 0
        }

        return AlertDialog.Builder(context)
            .setTitle("Photo Option")
            .setItems(options.toTypedArray<CharSequence>()) { _, which ->
                if (which == captureSelectIdx) {
                    listener.onCaptureClick()
                } else if (which == pickSelectIdx) {
                    listener.onPickClick()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
    }

    companion object {
        fun canPick(context: Context): Boolean {
            val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            return (pickIntent.resolveActivity(context.packageManager) != null)
        }

        fun canCapture(context: Context): Boolean {
            // TODO: create method canCapture() that can return true or false from intent(media store capture)
            return false
        }

        // TODO: create method newInstance() that return PhotoOptionsDialogFragment() method from statement canPick, canCapture or all its null
    }
}