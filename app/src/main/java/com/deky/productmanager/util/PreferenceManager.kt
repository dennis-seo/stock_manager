package com.deky.productmanager.util

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import com.deky.productmanager.R

/*
* Copyright (C) 2021 Kakao Corp. All rights reserved.
*
* Created by Diane on 07/01/2021
*
*/
object PreferenceManager {
    private const val PREFERENCES_NAME = "preference"
    const val PREFERENCE_IMAGE_TAG = "preferenceImageTag"
    const val PREFERENCE_IMAGE_TAG_NAME = "preferenceImageTagName"
    const val PREFERENCE_SAVE_DIRECTORY_URI = "preferenceSaveDirectoryUri"

    fun getPreferences(context: Context):SharedPreferences {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    fun setImageTagAvailablity(context: Context, isTagAvailability: Boolean) {
        val prefs = getPreferences(context)
        val editor = prefs.edit()
        editor.putBoolean(PREFERENCE_IMAGE_TAG, isTagAvailability)
        editor.apply()
    }

    fun isImageTagAvailability(context: Context):Boolean {
        return getPreferences(context).getBoolean(PREFERENCE_IMAGE_TAG, false)
    }

    fun setImageTagName(context:Context, name: String) {
        val prefs = getPreferences(context)
        val editor = prefs.edit()
        editor.putString(PREFERENCE_IMAGE_TAG_NAME, name)
        editor.apply()
        Toast.makeText(context, R.string.input_tag_save_finish, Toast.LENGTH_SHORT).show()
    }

    fun getImageTagName(context: Context): String {
        return getPreferences(context).getString(PREFERENCE_IMAGE_TAG_NAME, "") ?: ""
    }

    fun setSaveDirectoryUri(context: Context, uri: String) {
        getPreferences(context).edit()
            .putString(PREFERENCE_SAVE_DIRECTORY_URI, uri).apply()
    }

    fun getSaveDirectoryUri(context: Context): String? {
        return getPreferences(context).getString(PREFERENCE_SAVE_DIRECTORY_URI, null)
    }
}