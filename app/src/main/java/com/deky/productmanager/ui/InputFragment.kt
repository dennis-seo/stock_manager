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
import android.widget.*
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import com.deky.productmanager.R
import com.deky.productmanager.database.entity.Category
import com.deky.productmanager.database.entity.Manufacturer
import com.deky.productmanager.databinding.InputFragmentBinding
import com.deky.productmanager.model.InputViewModel
import com.deky.productmanager.model.BaseViewModel
import com.deky.productmanager.util.PreferenceManager
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

    var isTagLock = false

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context?.let {context ->
            if (PreferenceManager.isImageTagAvailability(context)) {
                tag_input_container.visibility = View.VISIBLE
                tag_checkbox.setChecked(true)
                tag_input.setText(PreferenceManager.getImageTagName(context))
                isTagLock = true
                tag_input.isEnabled = false
                tag_btn.text = getString(R.string.input_tag_editable)
            } else {
                tag_input_container.visibility = View.GONE
                tag_checkbox.setChecked(false)
                tag_input.setText(PreferenceManager.getImageTagName(context))
            }
        }

        manufacturer_textview?.setOnClickListener {
            viewModel.setClearManufacturer()
            viewModel.manufacturerParentId.postValue(-1L)
        }

        et_input_manufacturer?.setOnClickListener {
            viewModel.setClearManufacturer()
            viewModel.manufacturerParentId.postValue(-1L)
        }
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
            if (it.isEmpty()) {
                viewModel.products.value?.name = viewModel.getCategory()?.name ?: ""
                viewModel.categoryParentId.postValue(-1L)
            }
            initProductNameLayout(it)
        })
        viewModel.manufacturerList.observe(this, Observer {
            if (it.isEmpty()) {
                viewModel.products.value?.manufacturer = viewModel.getManufacturer()?.name ?: ""
                viewModel.manufacturerParentId.postValue(-1L)
            }
            initManufacturerInputLayout(it)
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

    private fun initManufacturerInputLayout(list: List<Manufacturer>) {
        manufacturer_container.removeAllViews()
        list.forEach { manufaturer ->
            val buttonView = inflater.inflate(R.layout.productname_item_layout, manufacturer_container, false)
            buttonView.btn_name.text = manufaturer.name
            buttonView.btn_name.setOnClickListener {
                viewModel.onClickManufacturer(it)
                viewModel.manufacturerParentId.postValue(manufaturer.id)
            }
            manufacturer_container.addView(buttonView)
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

    fun onClickCheckBox(checkBox: View?) {
        checkBox as CheckBox
        if (checkBox.isChecked) {
            tag_input_container.visibility = View.VISIBLE
            PreferenceManager.setImageTagAvailablity(context ?: return, true)
            tag_input.isEnabled = true
            return
        }
        tag_input_container.visibility = View.GONE
        PreferenceManager.setImageTagAvailablity(context ?: return, false)
    }

    fun onClickTagSave(button: View?) {
        val keyword = tag_input.text.toString()
        button as Button

        if(isTagLock) {
            isTagLock = false
            tag_input.isEnabled = true
            button.text = getString(R.string.input_tag_editable)
            return
        }
        if (keyword.isNullOrBlank()) {
            Toast.makeText(context, R.string.input_tag_name, Toast.LENGTH_SHORT).show()
            return
        }
        context?.let { context ->
            PreferenceManager.setImageTagName(context, keyword)
            PreferenceManager.setImageTagAvailablity(context, true)
            isTagLock = true
            button.text = getString(R.string.input_tag_save)
            tag_input.isEnabled = false
        }
    }
}