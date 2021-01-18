package com.deky.productmanager.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.deky.productmanager.util.DKLog
import com.deky.productmanager.util.FileUtils
import com.deky.productmanager.util.simpleTag
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


/**
 * Copyright (C) 2020 Kakao corp. All rights reserved.
 *
 */
abstract class BaseFragment : Fragment() {
    companion object {
        private const val TAG = "BaseFragment"

        const val REQUEST_IMAGE_CAPTURE = 1
    }

    protected val log by lazy(LazyThreadSafetyMode.NONE) {
        DKLog().apply {
            tag = this@BaseFragment.simpleTag()
        }
    }

    private var imageReq: ImageRequest? = null

    protected fun takePictureByIntent(result: (File) -> Unit = {}) {
        log.debug { "takePictureByIntent()" }

        val context = context ?: return
        val imageFile = try {
            FileUtils.createImageFile(context)
        } catch (e: Exception) {
            log.error {
                buildString {
                    append("takePictureByIntent() - Failed to create image file").append("\n")
                    append("exception message : ${e.message}")
                }
            }

            return
        }

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(context.packageManager)?.also {
                val imageUri = FileProvider.getUriForFile(
                    context,
                    "com.deky.productmanager.fileprovider",
                    imageFile
                )

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                imageReq = ImageRequest(imageFile, result)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == Activity.RESULT_OK) {
                    imageReq?.run {

                        val rotateMatrix = Matrix()

                        try {
                            val exif = ExifInterface(imageFile.absolutePath)

                            val exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                            val exifDegree = exifOrientationToDegree(exifOrientation)
                            rotateMatrix.postRotate(exifDegree.toFloat())
                        } catch (e: IOException) {
                            Log.e(TAG, e.stackTrace.toString())
                        }

                        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                        val rotateBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, rotateMatrix, false)
                        val scaleBitmap = Bitmap.createScaledBitmap(rotateBitmap, 1200, 900, false)
                        val fos = FileOutputStream(imageFile)
                        scaleBitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos)

                        log.debug {
                            buildString {
                                append("onActivityResult() - REQUEST_IMAGE_CAPTURE").append("\n")
                                append("imageFile : ${imageFile.absolutePath}").append("\n")
                                append("fileSize : ${imageFile.length()}")
                            }
                        }

                        block(imageFile)
                    }
                } else {
                    log.debug { "onActivityResult() - REQUEST_IMAGE_CAPTURE :: Result not Ok" }
                }

                imageReq = null
            }

            else -> {
                log.warn { "onActivityResult() - Unknown requestCode : $requestCode" }
            }
        }
    }

    private data class ImageRequest(
        val imageFile: File,
        val block: (File) -> Unit = {}
    )

    private fun exifOrientationToDegree(exifOrientation: Int): Int {
        return when(exifOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }
}