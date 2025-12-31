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
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.deky.productmanager.R
import com.deky.productmanager.database.entity.Condition
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
@Suppress("DEPRECATION")
@Deprecated ("InputFragment 사용")
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
        ViewModelProvider(this, BaseViewModel.Factory(requireActivity().application)).get(ModifyViewModel::class.java)
    }
    private var isUpdatingFromViewModel = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            productId = getLong(ARG_PRODUCT_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        dataBinding = ModifyFragmentBinding.inflate(inflater, container, false)
        viewModel.loadProductData(productId)
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        setupTextChangeListeners()
        setupRadioGroupListener()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initObservers()
    }

    private fun setupClickListeners() {
        dataBinding.btnTakePicture.setOnClickListener { onClickTakePicture(it) }
        dataBinding.btnClearData.setOnClickListener { viewModel.onClickClear() }
        dataBinding.btnConfirm.setOnClickListener { viewModel.onClickSave() }
    }

    private fun setupTextChangeListeners() {
        dataBinding.edInputLabel.doAfterTextChanged {
            if (!isUpdatingFromViewModel) {
                viewModel.onLabelChange(it.toString())
            }
        }
        dataBinding.etInputLocation.doAfterTextChanged {
            if (!isUpdatingFromViewModel) {
                viewModel.onLocationChange(it.toString())
            }
        }
        dataBinding.etInputName.doAfterTextChanged {
            if (!isUpdatingFromViewModel) {
                viewModel.onNameChange(it.toString())
            }
        }
        dataBinding.etInputManufacturer.doAfterTextChanged {
            if (!isUpdatingFromViewModel) {
                viewModel.onManufacturerChange(it.toString())
            }
        }
        dataBinding.etInputModel.doAfterTextChanged {
            if (!isUpdatingFromViewModel) {
                viewModel.onModelChange(it.toString())
            }
        }
        dataBinding.edInputSizeLength.doAfterTextChanged {
            if (!isUpdatingFromViewModel) {
                viewModel.onReplaceSize(0, it.toString())
            }
        }
        dataBinding.edInputSizeWidth.doAfterTextChanged {
            if (!isUpdatingFromViewModel) {
                viewModel.onReplaceSize(1, it.toString())
            }
        }
        dataBinding.edInputSizeHeight.doAfterTextChanged {
            if (!isUpdatingFromViewModel) {
                viewModel.onReplaceSize(2, it.toString())
            }
        }
        dataBinding.etInputManufactureDate.doAfterTextChanged {
            if (!isUpdatingFromViewModel) {
                viewModel.onManufactureDateChange(it.toString())
            }
        }
        dataBinding.etInputAmount.doAfterTextChanged {
            if (!isUpdatingFromViewModel) {
                viewModel.onAmountChange(it.toString())
            }
        }
        dataBinding.etInputNote.doAfterTextChanged {
            if (!isUpdatingFromViewModel) {
                viewModel.onNoteChange(it.toString())
            }
        }
    }

    private fun setupRadioGroupListener() {
        dataBinding.radioInputConditionGroup.setOnCheckedChangeListener { _, checkedId ->
            viewModel.onConditionTypeChanged(checkedId)
        }
    }

    private fun initObservers() {
        viewModel.toastMessage.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let { messageRes ->
                context.toast(messageRes)
            }
        })

        viewModel.products.observe(viewLifecycleOwner, Observer { product ->
            updateUIFromProduct(product)
        })

        viewModel.sizeLength.observe(viewLifecycleOwner, Observer { size ->
            if (dataBinding.edInputSizeLength.text.toString() != size) {
                isUpdatingFromViewModel = true
                dataBinding.edInputSizeLength.setText(size)
                isUpdatingFromViewModel = false
            }
        })

        viewModel.sizeWidth.observe(viewLifecycleOwner, Observer { size ->
            if (dataBinding.edInputSizeWidth.text.toString() != size) {
                isUpdatingFromViewModel = true
                dataBinding.edInputSizeWidth.setText(size)
                isUpdatingFromViewModel = false
            }
        })

        viewModel.sizeHeight.observe(viewLifecycleOwner, Observer { size ->
            if (dataBinding.edInputSizeHeight.text.toString() != size) {
                isUpdatingFromViewModel = true
                dataBinding.edInputSizeHeight.setText(size)
                isUpdatingFromViewModel = false
            }
        })

        viewModel.manufactureDate.observe(viewLifecycleOwner, Observer { date ->
            if (dataBinding.etInputManufactureDate.text.toString() != date) {
                isUpdatingFromViewModel = true
                dataBinding.etInputManufactureDate.setText(date)
                isUpdatingFromViewModel = false
            }
        })
    }

    private fun updateUIFromProduct(product: Product) {
        isUpdatingFromViewModel = true

        if (dataBinding.edInputLabel.text.toString() != product.label) {
            dataBinding.edInputLabel.setText(product.label)
        }
        if (dataBinding.etInputLocation.text.toString() != product.location) {
            dataBinding.etInputLocation.setText(product.location)
        }
        if (dataBinding.etInputName.text.toString() != product.name) {
            dataBinding.etInputName.setText(product.name)
        }
        if (dataBinding.etInputManufacturer.text.toString() != product.manufacturer) {
            dataBinding.etInputManufacturer.setText(product.manufacturer)
        }
        if (dataBinding.etInputModel.text.toString() != product.model) {
            dataBinding.etInputModel.setText(product.model)
        }

        val amountStr = if (product.amount > 0) product.amount.toString() else ""
        if (dataBinding.etInputAmount.text.toString() != amountStr) {
            dataBinding.etInputAmount.setText(amountStr)
        }

        when (product.condition) {
            Condition.HIGH -> dataBinding.radioInputConditionHigh.isChecked = true
            Condition.MIDDLE -> dataBinding.radioInputConditionMiddle.isChecked = true
            Condition.LOW -> dataBinding.radioInputConditionLow.isChecked = true
            else -> {}
        }

        if (dataBinding.etInputNote.text.toString() != product.note) {
            dataBinding.etInputNote.setText(product.note)
        }

        isUpdatingFromViewModel = false
    }

    private fun onClickTakePicture(view: View?) {
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
