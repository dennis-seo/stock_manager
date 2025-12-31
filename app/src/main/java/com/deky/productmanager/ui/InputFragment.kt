package com.deky.productmanager.ui

import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
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
import com.deky.productmanager.databinding.ProductnameItemLayoutBinding
import com.deky.productmanager.model.InputViewModel
import com.deky.productmanager.model.BaseViewModel
import com.deky.productmanager.ui.dialog.FavoriteDialog
import com.deky.productmanager.util.DKLog
import com.deky.productmanager.util.PreferenceManager
import com.deky.productmanager.util.toast


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

        dataBinding.btnLoad.visibility = if(viewType == ViewType.INPUT) View.VISIBLE else View.GONE

        context?.let {context ->
            if (PreferenceManager.isImageTagAvailability(context)) {
                dataBinding.tagInputContainer.visibility = View.VISIBLE
                dataBinding.tagCheckbox.isChecked = true
                dataBinding.tagInput.setText(PreferenceManager.getImageTagName(context))
                isTagLock = true
                dataBinding.tagInput.isEnabled = false
                dataBinding.tagBtn.text = getString(R.string.input_tag_editable)
            } else {
                dataBinding.tagInputContainer.visibility = View.GONE
                dataBinding.tagCheckbox.isChecked = false
                dataBinding.tagInput.setText(PreferenceManager.getImageTagName(context))
            }
        }

        // 품명 수동입력
        dataBinding.etInputName.doAfterTextChanged {
            val text = it.toString()
            if(text.isEmpty()) {
                viewModel.setClearProductName()
                viewModel.categoryParentId.postValue(-1L)
                dataBinding.ivClearProduct.visibility = View.GONE
            } else {
                viewModel.onNameChange(text)
                dataBinding.ivClearProduct.visibility = View.VISIBLE
            }
            DKLog.debug(TAG) { text }
        }

        // 제조사 수동입력
        dataBinding.etInputManufacturer.doAfterTextChanged {
            val text = it.toString()
            if(text.isEmpty()) {
                viewModel.setClearManufacturer()
                viewModel.manufacturerParentId.postValue(-1L)
                dataBinding.ivClearManufacturer.visibility = View.GONE
            } else {
                viewModel.onManufacturerChange(text)
                dataBinding.ivClearManufacturer.visibility = View.VISIBLE
            }
        }

        // 모델명 수동입력
        dataBinding.etInputModel.doAfterTextChanged {
            val text = it.toString()
            if(text.isEmpty()) {
                viewModel.setClearModel()
                viewModel.modelParentId.postValue(-1L)
                dataBinding.ivClearModel.visibility = View.GONE
            } else {
                viewModel.onModelChange(text)
                dataBinding.ivClearModel.visibility = View.VISIBLE
            }
        }

        dataBinding.edInputSizeLength.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                dataBinding.edInputSizeWidth.requestFocus()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        dataBinding.edInputSizeWidth.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                dataBinding.edInputSizeHeight.requestFocus()
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
            dataBinding.etInputAmount.setText(it)
            Toast.makeText(context, R.string.message_toast_input_value_only_number, Toast.LENGTH_SHORT).show()
        })
        // 품명
        viewModel.productNameList.observe(viewLifecycleOwner, Observer {
            DKLog.debug(TAG) { "productNameList list : ${it}" }
            initProductNameLayout(it)
        })
        // 제조사
        viewModel.manufacturerList.observe(viewLifecycleOwner, Observer {
            DKLog.debug(TAG) { "manufacturerList  : ${it}" }
            initManufacturerInputLayout(it)
        })
        // 모델명
        viewModel.modelList.observe(viewLifecycleOwner, Observer {
            DKLog.debug(TAG) { "model list : ${it}" }
            initModelInputLayout(it)
        })
    }

    private fun initProductNameLayout(list: List<Category>) {
        dataBinding.productnameLayout.removeAllViews()
        DKLog.debug(TAG) { "initProductNameLayout" }
        list.forEach { category ->
            val itemBinding = ProductnameItemLayoutBinding.inflate(
                LayoutInflater.from(context), dataBinding.productnameLayout, false
            )
            itemBinding.btnName.text = category.name
            itemBinding.btnName.setOnClickListener {
                viewModel.onClickNameButton(it)
                viewModel.categoryParentId.postValue(category.id)
            }
            dataBinding.productnameLayout.addView(itemBinding.root)
        }
    }

    private fun initManufacturerInputLayout(list: List<Manufacturer>) {
        dataBinding.manufacturerContainer.removeAllViews()
        list.forEach { manufaturer ->
            val itemBinding = ProductnameItemLayoutBinding.inflate(
                LayoutInflater.from(context), dataBinding.manufacturerContainer, false
            )
            itemBinding.btnName.text = manufaturer.name
            itemBinding.btnName.setOnClickListener {
                viewModel.onClickManufacturer(it)
                viewModel.manufacturerParentId.postValue(manufaturer.id)
            }
            dataBinding.manufacturerContainer.addView(itemBinding.root)
        }
    }

    private fun initModelInputLayout(list: List<Model>) {
        dataBinding.modelContainer.removeAllViews()
        list.forEach { model ->
            val itemBinding = ProductnameItemLayoutBinding.inflate(
                LayoutInflater.from(context), dataBinding.modelContainer, false
            )
            itemBinding.btnName.text = model.name
            itemBinding.btnName.setOnClickListener {
                viewModel.onClickModel(it)
                viewModel.modelParentId.postValue(model.id)
            }
            dataBinding.modelContainer.addView(itemBinding.root)
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
            dataBinding.tagInputContainer.visibility = View.VISIBLE
            PreferenceManager.setImageTagAvailablity(context ?: return, true)
            dataBinding.tagInput.isEnabled = true
            return
        }
        dataBinding.tagInputContainer.visibility = View.GONE
        PreferenceManager.setImageTagAvailablity(context ?: return, false)
    }

    fun onClickTagSave(button: View?) {
        val keyword = dataBinding.tagInput.text.toString()
        button as Button

        if(isTagLock) {
            isTagLock = false
            dataBinding.tagInput.isEnabled = true
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
            dataBinding.tagInput.isEnabled = false
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
