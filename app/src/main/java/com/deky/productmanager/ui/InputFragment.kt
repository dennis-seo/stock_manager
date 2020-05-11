package com.deky.productmanager.ui

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import com.deky.productmanager.R
import com.deky.productmanager.databinding.InputFragmentBinding


/*
* Copyright (C) 2020 Kakao corp. All rights reserved.
*
* Created by Dennis.Seo on 07/05/2020
*
*/
class InputFragment : BaseFragment() {


    companion object {
        fun newInstance() = InputFragment()
    }

    private lateinit var dataBinding: InputFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        dataBinding = DataBindingUtil.inflate<InputFragmentBinding>(
            inflater, R.layout.input_fragment, container, false).apply {
                lifecycleOwner = this@InputFragment
                listener = this@InputFragment
            }
        return dataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        onClearClick()
    }

    private fun clearData() {

    }

    private fun saveData() {

    }

    fun onClearClick() {
        clearData()
    }

    fun onSaveClick() {
        saveData()
    }


    fun onClickTakePicture(view: View?) {

        takePictureByIntent{ imageFile ->
            if(imageFile.exists()) {
                val contentResolver = activity?.contentResolver ?: return@takePictureByIntent

                log.debug { "onClickTakePicture() : ${imageFile.absoluteFile}" }
                if(view is ImageButton) {
                    if (Build.VERSION.SDK_INT < 28) {
                        val bitmap = MediaStore.Images.Media
                            .getBitmap(activity?.contentResolver, Uri.fromFile(imageFile))
                        view.setImageBitmap(bitmap)
                        view.scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                    else{
                        val decode = ImageDecoder.createSource(contentResolver,
                            Uri.fromFile(imageFile))
                        val bitmap = ImageDecoder.decodeBitmap(decode)
                        view.setImageBitmap(bitmap)
                        view.scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                }
            }
        }
    }
}