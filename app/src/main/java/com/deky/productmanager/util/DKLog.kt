package com.deky.productmanager.util

import android.util.Log
import com.deky.productmanager.BuildConfig

/**
 * Copyright (C) 2020 Kakao corp. All rights reserved.
 *
 */
class DKLog {
    companion object {
        val isDebuggable: Boolean
            get() = BuildConfig.DEBUG

        inline fun verbose(tag: String, force: Boolean = false, log: () -> String) {
            if (isDebuggable || force) {
                Log.v(tag, appendExtraInfo(log()))
            }
        }

        inline fun debug(tag: String, force: Boolean = false, log: () -> String) {
            if (isDebuggable || force) {
                Log.d(tag, appendExtraInfo(log()))
            }
        }

        inline fun info(tag: String, force: Boolean = false, log: () -> String) {
            if (isDebuggable || force) {
                Log.i(tag, appendExtraInfo(log()))
            }
        }

        inline fun warn(tag: String, force: Boolean = false, log: () -> String) {
            if (isDebuggable || force) {
                Log.w(tag, appendExtraInfo(log()))
            }
        }

        inline fun error(tag: String, callStack: Boolean = false, log: () -> String) {
            if (callStack) {
                Log.e(tag, appendExtraInfo(log()), Throwable())
            } else {
                Log.e(tag, appendExtraInfo(log()))
            }
        }

        fun appendExtraInfo(message: String): String {
            return Thread.currentThread().run {
                "[$name] $message"
            }
        }
    }

    var tag: String = ""

    inline fun verbose(force: Boolean = false, log: () -> String) {
        verbose(tag, force, log)
    }

    inline fun debug(force: Boolean = false, log: () -> String) {
        debug(tag, force, log)
    }

    inline fun info(force: Boolean = false, log: () -> String) {
        info(tag, force, log)
    }

    inline fun warn(force: Boolean = false, log: () -> String) {
        warn(tag, force, log)
    }

    inline fun error(callStack: Boolean = false, log: () -> String) {
        error(tag, callStack, log)
    }
}