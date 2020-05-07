package com.deky.productmanager.ui

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.deky.productmanager.util.FileUtils
import com.deky.productmanager.util.Logger
import java.io.File

/**
 * Copyright (C) 2020 Kakao corp. All rights reserved.
 *
 */
abstract class BaseFragment : Fragment() {
    companion object {
        private const val TAG = "BaseFragment"

        const val REQUEST_IMAGE_CAPTURE = 1
    }

    private var imageReq: ImageRequest? = null

    protected fun takePictureIntent(result: (File) -> Unit = {}) {
        Logger.d(TAG, "takePictureIntent()")

        val context = context ?: return
        val imageFile = try {
            FileUtils.createImageFile(context)
        } catch (e: Exception) {
            Logger.e(TAG, buildString {
                append("takePictureIntent() - Failed to create image file").append("\n")
                append("exception message : ${e.message}")
            })

            return
        }

        Logger.d(TAG, "takePictureIntent() - Image file : ${imageFile.name}")

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
                        Logger.v(TAG, buildString {
                            append("onActivityResult() - REQUEST_IMAGE_CAPTURE").append("\n")
                            append("imageFile : ${imageFile.absolutePath}").append("\n")
                            append("fileSize : ${imageFile.length()}")
                        })

                        block(imageFile)
                    }
                } else {
                    Logger.d(TAG, "onActivityResult() - REQUEST_IMAGE_CAPTURE :: Result not Ok")
                }

                imageReq = null
            }

            else -> {
                Logger.w(TAG, "onActivityResult() - Unknown requestCode : $requestCode")
            }
        }
    }

    private data class ImageRequest(
        val imageFile: File,
        val block: (File) -> Unit = {}
    )
}