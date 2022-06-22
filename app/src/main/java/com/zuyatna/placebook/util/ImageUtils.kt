package com.zuyatna.placebook.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Environment
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.jvm.Throws

object ImageUtils {
    fun saveBitmapToFile(context: Context, bitmap: Bitmap, filename: String) {
        val stream = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

        val bytes = stream.toByteArray()

        saveBytesToFile(context, bytes, filename)
    }

    fun loadBitmapFromFile(context: Context, filename: String): Bitmap? {
        val filePath = File(context.filesDir, filename).absolutePath
        return BitmapFactory.decodeFile(filePath)
    }

    @Throws(IOException::class)
    fun createUniqueImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val filename = "PlaceBook_" + timeStamp + "_"
        val filesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(filename, ".jpg", filesDir)
    }

    fun decodeFileToSize(
        filePath: String,
        width: Int,
        height: Int
    ): Bitmap {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, options)

        options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, width, height)

        options.inJustDecodeBounds = false

        return BitmapFactory.decodeFile(filePath, options)
    }

    private fun saveBytesToFile(context: Context, bytes: ByteArray, filename: String) {
        val outputStream: FileOutputStream

        try {
            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE)
            outputStream.write(bytes)
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun calculateInSampleSize(
        width: Int,
        height: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight &&
                    halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private fun rotateImage(img: Bitmap, degree: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(degree)

        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }
}