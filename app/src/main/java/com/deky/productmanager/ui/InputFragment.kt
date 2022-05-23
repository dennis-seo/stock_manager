package com.deky.productmanager.ui

import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import com.deky.productmanager.R
import com.deky.productmanager.database.entity.Category
import com.deky.productmanager.database.entity.Manufacturer
import com.deky.productmanager.database.entity.Model
import com.deky.productmanager.database.entity.Product
import com.deky.productmanager.databinding.InputFragmentBinding
import com.deky.productmanager.model.InputViewModel
import com.deky.productmanager.model.BaseViewModel
import com.deky.productmanager.ui.dialog.FavoriteDialog
import com.deky.productmanager.util.DKLog
import com.deky.productmanager.util.PreferenceManager
import com.deky.productmanager.util.toast
import kotlinx.android.synthetic.main.input_fragment.*
import kotlinx.android.synthetic.main.productname_item_layout.view.*


/*
* Created by Dennis.Seo on 07/05/2020
*/

enum class ViewType {
    INPUT,
    MODIFY,
    CONFIRM
}

class InputFragment : BaseFragment() {

    companion object {
        private const val TAG = "InputFragment"
        private const val ARG_PRODUCT_ID = "product_id"
        private const val ARG_VIEW_TYPE = "view_type"
        const val DEFAULT_PRODUCT_ID: Long = -1

        fun newInstance(productId: Long, viewType: ViewType = ViewType.INPUT) = InputFragment().apply {
            arguments = bundleOf (
                ARG_PRODUCT_ID to productId,
                ARG_VIEW_TYPE to viewType
            )
        }
    }

    private var productId: Long = DEFAULT_PRODUCT_ID
    private var viewType: ViewType = ViewType.INPUT
    private lateinit var dataBinding: InputFragmentBinding
    private val viewModel: InputViewModel by lazy {
        ViewModelProvider(this, BaseViewModel.Factory(requireActivity().application)).get(InputViewModel::class.java)
    }
    private val inflater by lazy {
        context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    var isTagLock = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            productId = getLong(ARG_PRODUCT_ID, DEFAULT_PRODUCT_ID)
            viewType = get(ARG_VIEW_TYPE) as ViewType
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
        viewModel.manufacturerParentId.postValue(-1L)
        viewModel.modelParentId.postValue(-1L)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_load.visibility = if(viewType == ViewType.INPUT) View.VISIBLE else View.GONE

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

        // 품명 수동입력
        et_input_name?.doAfterTextChanged {
            val text = it.toString()
            if(text.isEmpty()) {
                viewModel.setClearProductName()
                viewModel.categoryParentId.postValue(-1L)
                iv_clear_product.visibility = View.GONE
            } else {
                viewModel.onNameChange(text)
                iv_clear_product.visibility = View.VISIBLE
            }
            DKLog.debug(TAG) { text }
        }

        // 제조사 수동입력
        et_input_manufacturer?.doAfterTextChanged {
            val text = it.toString()
            if(text.isEmpty()) {
                viewModel.setClearManufacturer()
                viewModel.manufacturerParentId.postValue(-1L)
                iv_clear_manufacturer.visibility = View.GONE
            } else {
                viewModel.onManufacturerChange(text)
                iv_clear_manufacturer.visibility = View.VISIBLE
            }
        }

        // 모델명 수동입력
        et_input_model?.doAfterTextChanged {
            val text = it.toString()
            if(text.isEmpty()) {
                viewModel.setClearModel()
                viewModel.modelParentId.postValue(-1L)
                iv_clear_model.visibility = View.GONE
            } else {
                viewModel.onModelChange(text)
                iv_clear_model.visibility = View.VISIBLE
            }
        }

        ed_input_size_length?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                ed_input_size_width.requestFocus()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        ed_input_size_width?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                ed_input_size_height.requestFocus()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

    }

    private fun initObservers() {
        viewModel.toastMessage.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let { messageRes ->
                context.toast(messageRes)
            }
        })
        viewModel.numberFormatExceptionEvent.observe(viewLifecycleOwner, Observer {
            et_input_amount.setText(it)
            Toast.makeText(context, R.string.message_toast_input_value_only_number, Toast.LENGTH_SHORT).show()
        })
        // 품명
        viewModel.productNameList.observe(viewLifecycleOwner, Observer {
//            if (it.isEmpty()) {
//                viewModel.products.value?.name = viewModel.getCategory()?.name ?: ""
//                viewModel.categoryParentId.postValue(-1L)
//            }
            DKLog.debug(TAG) { "productNameList list : ${it}" }
            initProductNameLayout(it)
        })
        // 제조사
        viewModel.manufacturerList.observe(viewLifecycleOwner, Observer {
//            if (it.isEmpty()) {
//                viewModel.products.value?.manufacturer = viewModel.getManufacturer()?.name ?: ""
//                viewModel.manufacturerParentId.postValue(-1L)
//            }
            DKLog.debug(TAG) { "manufacturerList  : ${it}" }
            initManufacturerInputLayout(it)
        })
        // 모델명
        viewModel.modelList.observe(viewLifecycleOwner, Observer {
//            if (it.isEmpty()) {
//                viewModel.products.value?.manufacturer = viewModel.getModel()?.name ?: ""
//                viewModel.modelParentId.postValue(-1L)
//            }
            DKLog.debug(TAG) { "model list : ${it}" }
            initModelInputLayout(it)
        })
    }

    private fun initProductNameLayout(list: List<Category>) {
        productname_layout.removeAllViews()
        DKLog.debug(TAG) { "initProductNameLayout" }
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

    private fun initModelInputLayout(list: List<Model>) {
        model_container.removeAllViews()
        list.forEach { model ->
            val buttonView = inflater.inflate(R.layout.productname_item_layout, model_container, false)
            buttonView.btn_name.text = model.name
            buttonView.btn_name.setOnClickListener {
                viewModel.onClickModel(it)
                viewModel.modelParentId.postValue(model.id)
            }
            model_container.addView(buttonView)
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

    fun onClickFavoriteData(button: View) {
        FavoriteDialog().apply {
            setOnItemClickListener(object: FavoriteDialog.OnFavoriteDialogClickListener{
                override fun onItemClick(product: Product) {
                    viewModel.loadFavoriteData(product)
                    dialog?.dismiss()
                }
            })
        }.show(childFragmentManager, "FavoriteDialog")
    }

    // 즐겨찾기 추가
    fun onClickFavorite(checkBox: View) {
        checkBox as CheckBox
        DKLog.debug("bbong") { "view.isChecked : ${checkBox.isChecked}"}
        viewModel.products.value?.favorite = checkBox.isChecked
    }
}