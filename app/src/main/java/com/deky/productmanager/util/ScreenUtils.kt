package com.deky.productmanager.util

import android.content.Context


/*
* Copyright (C) 2020 Kakao corp. All rights reserved.
*
* Created by Dennis.Seo on 16/05/2020
*
*/
object ScreenUtils {
    fun dipToPixel(context: Context?, dip: Float): Int {
        if (context == null) {
            return 0
        }
        val density = context.resources.displayMetrics.density
        return (dip * density).toInt()
    }

    fun pixelToDip(context: Context?, pixel: Int): Int {
        if (context == null) {
            return 0
        }
        val density = context.resources.displayMetrics.density
        return (pixel.toFloat() / density).toInt()
    }
}