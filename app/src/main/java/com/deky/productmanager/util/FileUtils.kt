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