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
import android.widget.RadioGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import com.deky.productmanager.R
import com.deky.productmanager.database.entity.Condition
import com.deky.productmanager.databinding.InputFragmentBinding
import com.deky.productmanager.model.InputViewModel
import com.deky.productmanager.model.ProductsBaseViewModel
import com.deky.productmanager.util.toast


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
    private val viewModel: InputViewModel by lazy {
        ViewModelProvider(this, ProductsBaseViewModel.Factory(activity!!.application)).get(InputViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        dataBinding = DataBindingUtil.inflate<InputFragmentBinding>(
            inflater, R.layout.input_fragment, container, false).apply {
                lifecycleOwner = this@InputFragment
                productViewModel = viewModel
                listener = this@InputFragment
            }
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

    fun onSplitTypeChanged(radioGroup: RadioGroup, checkedId: Int) {
        log.debug { "id : $checkedId" }
        when(checkedId) {
                R.id.radio_input_condition_high ->
                    viewModel.getProducts().value.condition = Condition.HIGH
                R.id.radio_input_condition_middle ->
                    viewModel.getProducts().value.condition = Condition.MIDDLE
                R.id.radio_input_condition_low ->
                    viewModel.getProducts().value.condition = Condition.LOW
        }
    }

    // 삭제버튼
//    fun onClickClear() {
//        viewModel.getProducts().postValue(Product())
//        Toast.makeText(context, R.string.message_success_delete, Toast.LENGTH_SHORT).show()
//    }

    // 저장버튼
//    fun onClickSave() {
//        isValidManufactureSize()
//
//        if(isValidManufacturerDate()) {
//            val product = viewModel.getProducts()
//            context?.let {
//                Executors.newSingleThreadExecutor().execute {
//                    ProductDB.getInstance(it).productDao().also { dao ->
//                        DKLog.debug("bbong") { "saveData() : ${product.value}" }
//                        dao.insert(product.value)
//                    }
//                }
//                Toast.makeText(it, R.string.message_success_save, Toast.LENGTH_SHORT).show()
//                onClickClear()
//            }
//        } else {
//            context?.let {
//                Toast.makeText(it, R.string.message_invalid_date, Toast.LENGTH_SHORT).show()
//            }
//        }
//    }


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

                viewModel.getProducts().value.imagePath = imageFile.path
            }
        }
    }
}