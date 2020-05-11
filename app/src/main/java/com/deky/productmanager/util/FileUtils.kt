package com.deky.productmanager.util

import android.content.Context
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Copyright (C) 2020 Kakao corp. All rights reserved.
 *
 */
object FileUtils {
    private const val DATA_DIRECTORY_NAME = "ProductManager"

    fun getDataDirectory(context: Context): File {
        return File(context.getExternalFilesDir(null) ?: context.filesDir, DATA_DIRECTORY_NAME)
    }

    @Throws(Exception::class)
    fun createImageFile(context: Context, filePrefixName: String = getImagePrefixName()): File {
        return File.createTempFile(
            /* prefix */ filePrefixName,
            /* suffix */ ".png",
            /* directory */ context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )
    }

    private fun getImagePrefixName(): String {
        return SimpleDateFormat("yyyyMMdd_", Locale.getDefault()).format(Date())
            .let { timeStamp ->
                "Image_${timeStamp}"
            }
    }
}