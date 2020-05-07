package com.deky.productmanager.util

import android.util.Log

/**
 * Copyright (C) 2020 Kakao corp. All rights reserved.
 *
 */
class Logger {
    companion object {
        @JvmStatic
        fun v(tag: String, message: String) {
            Log.v(tag, appendExtraInfo(message))
        }

        @JvmStatic
        fun d(tag: String, message: String) {
            Log.d(tag, appendExtraInfo(message))
        }

        @JvmStatic
        fun i(tag: String, message: String) {
            Log.i(tag, appendExtraInfo(message))
        }

        @JvmStatic
        fun w(tag: String, message: String) {
            Log.w(tag, appendExtraInfo(message))
        }

        @JvmStatic
        fun e(tag: String, message: String) {
            Log.e(tag, appendExtraInfo(message))
        }

        private fun appendExtraInfo(message: String): String {
            return Thread.currentThread().run {
                "[$name][$id] $message"
            }
        }
    }
}