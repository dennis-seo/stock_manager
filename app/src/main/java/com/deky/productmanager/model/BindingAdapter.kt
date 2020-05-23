package com.deky.productmanager.model

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.databinding.BindingAdapter
import coil.api.load
import com.deky.productmanager.R
import java.io.File


/*
* Copyright (C) 2020 Kakao corp. All rights reserved.
*
* Created by Dennis.Seo on 23/05/2020
*
*/
class BindingAdapter {

    companion object {
        @JvmStatic
        @BindingAdapter("loadImage")
        fun loadImage(view: ImageButton, imagePath: String) {
            if(imagePath.isNotEmpty()) {
                File(imagePath).takeIf { it.exists() }?.let { imageFile ->
                    view.load(imageFile)
                }
            } else {
                view.setImageResource(R.drawable.ic_camera)
            }
        }
    }
}