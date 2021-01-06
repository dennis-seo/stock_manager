package com.deky.productmanager.ui

import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import com.deky.productmanager.R
import com.deky.productmanager.database.entity.Category
import com.deky.productmanager.databinding.InputFragmentBinding
import com.deky.productmanager.model.InputViewModel
import com.deky.productmanager.model.BaseViewModel
import com.deky.productmanager.util.toast
import kotlinx.android.synthetic.main.input_fragment.*
import kotlinx.android.synthetic.main.productname_item_layout.view.*


/*
* Copyright (C) 2020 Kakao corp. All rights reserved.
*
* Created by Dennis.Seo on 07/05/2020
*
*/
class InputFragment : BaseFragment() {

    companion object {
        private const val ARG_PRODUCT_ID = "product_id"
        const val DEFAULT_PRODUCT_ID: Long = -1

        fun newInstance(productId: Long) = InputFragment().apply {
            arguments = bundleOf (
                ARG_PRODUCT_ID to productId
            )
        }
    }

    private var productId: Long = DEFAULT_PRODUCT_ID
    private lateinit var dataBinding: InputFragmentBinding
    private val viewModel: InputViewModel by lazy {
        ViewModelProvider(this, BaseViewModel.Factory(requireActivity().application)).get(InputViewModel::class.java)
    }
    val inflater by lazy {
        context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            productId = getLong(ARG_PRODUCT_ID, DEFAULT_PRODUCT_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        dataBinding = DataBindingUtil.inflate<InputFragmentBinding>(
            inflater, R.layout.input_fragment, container, false).apply {
                lifecycleOwner = this@InputFragment
                productViewModel = viewModel
                listener = this@InputFragment
            }
        if(productId != DEFAULT_PRODUCT_ID) {
            viewModel.loadProductData(productId)
        }


        return dataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initObservers()
        viewModel.categoryParentId.postValue(-1L)
    }

    private fun initObservers() {
        viewModel.toastMessage.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { messageRes ->
                context.toast(messageRes)
            }
        })
        viewModel.numberFormatExceptionEvent.observe(this, Observer {
            et_input_amount.setText(it)
            Toast.makeText(context, R.string.message_toast_input_value_only_number, Toast.LENGTH_SHORT).show()
        })
        viewModel.productNameList.observe(this, Observer {
            if(it.isEmpty()) {
                viewModel.products.value?.name = viewModel.getCategory()?.name ?: ""
                viewModel.categoryParentId.postValue(-1L)
            }
            initProductNameLayout(it)
        })
    }

    private fun initProductNameLayout(list: List<Category>) {
        productname_layout.removeAllViews()
        list.forEach { category ->
            val buttonView = inflater.inflate(R.layout.productname_item_layout, productname_layout, false)
            buttonView.btn_name.text = category.name
            buttonView.btn_name.setOnClickListener {
                viewModel.onClickNameButton(it)
                viewModel.categoryParentId.postValue(category.id)
            }
            productname_layout.addView(buttonView)
        }
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