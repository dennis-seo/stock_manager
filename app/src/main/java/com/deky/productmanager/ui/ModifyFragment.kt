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
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.deky.productmanager.R
import com.deky.productmanager.database.entity.Product
import com.deky.productmanager.databinding.ModifyFragmentBinding
import com.deky.productmanager.model.BaseViewModel
import com.deky.productmanager.model.ModifyViewModel
import com.deky.productmanager.util.toast


/*
* Copyright (C) 2020 Kakao corp. All rights reserved.
*
* Created by Dennis.Seo on 07/05/2020
*
*/
class ModifyFragment : BaseFragment() {

    companion object {
        private val ARG_PRODUCT_ID = "product_id"

        fun newInstance(productId: Long) = ModifyFragment().apply {
            arguments = bundleOf (
                ARG_PRODUCT_ID to productId
            )
        }
    }

    private var productId: Long = -1
    private lateinit var dataBinding: ModifyFragmentBinding
    private val viewModel: ModifyViewModel by lazy {
        ViewModelProvider(this, BaseViewModel.Factory(activity!!.application)).get(ModifyViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            productId = getLong(ARG_PRODUCT_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        dataBinding = DataBindingUtil.inflate<ModifyFragmentBinding>(
            inflater, R.layout.modify_fragment, container, false).apply {
                lifecycleOwner = this@ModifyFragment
                productViewModel = viewModel
                listener = this@ModifyFragment
            }

        viewModel.loadProductData(productId)

        return dataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initObservers()
    }

    private fun initObservers() {
        viewModel.toastMessage.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { messageRes ->
                context.toast(messageRes)
            }
        })
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
                        view.scaleType = ImageView.ScaleType.CENTER_INSIDE
                    }
                    else{
                        val decode = ImageDecoder.createSource(contentResolver,
                            Uri.fromFile(imageFile))
                        val bitmap = ImageDecoder.decodeBitmap(decode)
                        view.setImageBitmap(bitmap)
                        view.scaleType = ImageView.ScaleType.CENTER_INSIDE
                    }
                }

                viewModel.setImageFilePath(imageFile.path)
            }
        }
    }
}