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
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.deky.productmanager.R
import com.deky.productmanager.database.ProductDB
import com.deky.productmanager.database.entity.Condition
import com.deky.productmanager.database.entity.Product
import com.deky.productmanager.databinding.InputFragmentBinding
import com.deky.productmanager.model.InputViewModel
import com.deky.productmanager.util.DKLog
import com.deky.productmanager.util.DateUtils
import kotlinx.android.synthetic.main.input_fragment.*
import java.util.logging.Logger


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
        ViewModelProvider(this).get(InputViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        dataBinding = DataBindingUtil.inflate<InputFragmentBinding>(
            inflater, R.layout.input_fragment, container, false).apply {
                lifecycleOwner = this@InputFragment
                viewModel = viewModel
                listener = this@InputFragment
            }
        return dataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dataBinding.radioInputConditionGroup.setOnCheckedChangeListener { radioGroup, checkedId ->
            when(checkedId) {
                R.id.radio_input_condition_high ->
                    viewModel.getCondition().value = Condition.HIGH
                R.id.radio_input_condition_middle ->
                    viewModel.getCondition().value = Condition.MIDDLE
                R.id.radio_input_condition_low ->
                    viewModel.getCondition().value = Condition.LOW
            }
        }

        clearData()
    }

    private fun clearData() {

        context?.let {
            ed_input_label.text = null
            btn_take_picture.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.ic_camera))
            btn_take_picture.scaleType = ImageView.ScaleType.CENTER
            // et_input_location.text = null
            et_input_name.text = null
            et_input_manufacturer.text = null
            et_input_model.text = null

            ed_input_size_length.text = null
            ed_input_size_width.text = null
            ed_input_size_height.text = null

            et_input_manufacture_date.text = null
            et_input_amount.text = null

            radio_input_condition_high.isSelected = false
            radio_input_condition_middle.isSelected = false
            radio_input_condition_low.isSelected = false

            et_input_note.text = null
        }
    }

    private fun saveData() {
        context?.let {
            ProductDB.getInstance(it).productDao().also { dao ->
                val product = Product(
                    viewModel.getLabel().value?: "",
                    viewModel.getImagePath().value?: "",
                    viewModel.getLocation().value?: "",
                    viewModel.getName().value?: "",
                    viewModel.getManufacturer().value?: "",
                    DateUtils.convertStringToDate(et_input_manufacture_date.text.toString()),
                    viewModel.getCondition().value?: Condition.NONE,
                    viewModel.getSize(),
                    viewModel.getModel().value?: "",
                    viewModel.getAmount().value?: 1
                )
                log.debug { "saveData() : $product" }

                dao.insert(product)
            }
        }

        Toast.makeText(context, R.string.message_success_save, Toast.LENGTH_SHORT).show()
    }

    fun onClearClick(view: View?) {
        clearData()
    }

    fun onSaveClick(view: View?) {
        saveData()
        clearData()
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

                viewModel.getImagePath().postValue(imageFile.path)
            }
        }
    }
}