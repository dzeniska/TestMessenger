package com.dzenis_ska.testmessenger.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.FragmentActivity
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream


object ImageManager {

    private const val MAX_IMAGE_SIZE = 1000
    private const val WIDTH = 0
    private const val HEIGHT = 1

    private suspend fun getImageSize(uri: Uri, act: FragmentActivity): List<Int>? =
        withContext(Dispatchers.IO) {
            val inStream = try {
                act.contentResolver.openInputStream(uri)
            } catch (e: Exception) {
                null
            }

            if (inStream != null) {
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeStream(inStream, null, options)

                val imgRotation = imageRotationNew(uri, act)

                return@withContext if (imgRotation == 90 || imgRotation == 270) {
                    listOf(options.outHeight, options.outWidth)
                } else {
                    listOf(options.outWidth, options.outHeight)
                }
            } else {
                null
            }
        }

    private suspend fun imageRotationNew(uri: Uri, act: FragmentActivity): Int =
        withContext(Dispatchers.IO) {

            val inStream = try {
                act.contentResolver.openInputStream(uri)
            } catch (e: Exception) {
                null
            }
            val exif = inStream?.let { ExifInterface(it) }

            return@withContext when (exif?.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                else -> 0
            }
        }

    suspend fun imageResize(
        uri: Uri,
        act: FragmentActivity,
        callback: (bitmap: ByteArray) -> Unit
    ) = withContext(Dispatchers.IO) {
        var tempList = listOf<Int>()

        val size = getImageSize(uri, act)


        if (size != null) {
            val imageRatio = size[WIDTH].toDouble() / size[HEIGHT].toDouble()
            if (imageRatio > 1) {
                tempList = if (size[WIDTH] > MAX_IMAGE_SIZE) {
                    listOf(MAX_IMAGE_SIZE, (MAX_IMAGE_SIZE / imageRatio).toInt())

                } else {
                    listOf(size[WIDTH], size[HEIGHT])
                }
            } else if (imageRatio < 1) {
                tempList = if (size[HEIGHT] > MAX_IMAGE_SIZE) {
                    listOf((MAX_IMAGE_SIZE * imageRatio).toInt(), MAX_IMAGE_SIZE)
                } else {
                    listOf(size[WIDTH], size[HEIGHT])
                }
            } else {
                if (size[WIDTH] > MAX_IMAGE_SIZE) {
                    tempList = listOf(MAX_IMAGE_SIZE, MAX_IMAGE_SIZE)
                }
            }
        } else {
            return@withContext null
        }
        val rotationDegres = imageRotationNew(uri, act)

        val bitmap = Picasso.get()
            .load(uri)
            .resize(tempList[WIDTH], tempList[HEIGHT])
            .rotate(rotationDegres!!.toFloat())
            .get()

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
        callback(baos.toByteArray())
    }
}