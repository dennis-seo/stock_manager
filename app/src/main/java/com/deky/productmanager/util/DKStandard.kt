package com.deky.productmanager.util

import android.content.res.Resources

/**
 * Copyright (C) 2020 Kakao corp. All rights reserved.
 *
 */
fun Any.simpleTag(): String {
    return "${javaClass.simpleName}[@${Integer.toHexString(hashCode())}]"
}

fun String.prependIndent(indent: Int) = when (indent) {
    1 -> "    $this"
    2 -> "        $this"
    3 -> "            $this"
    4 -> "                $this"
    else -> this
}

fun dpToPx(dp: Int): Int {
    return (dp * Resources.getSystem().displayMetrics.density).toInt()
}