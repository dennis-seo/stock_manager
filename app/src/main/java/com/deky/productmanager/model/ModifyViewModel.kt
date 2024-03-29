package com.deky.productmanager.model

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.deky.productmanager.R
import com.deky.productmanager.database.entity.Condition
import com.deky.productmanager.database.entity.DEFAULT_DATE
import com.deky.productmanager.database.entity.DEFAULT_SIZE
import com.deky.productmanager.database.entity.Product
import com.deky.productmanager.database.repository.ProductRepository
import com.deky.productmanager.util.DKLog
import com.deky.productmanager.util.DateUtils
import com.deky.productmanager.util.NotNullMutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Deprecated ("InputViewModel 사용")
class ModifyViewModel(application: Application): BaseViewModel(application) {
    companion object {
        private const val TAG = "ModifyViewModel"
    }

    private var repository: ProductRepository = ProductRepository(application)

    private val _products: NotNullMutableLiveData<Product> = NotNullMutableLiveData(Product())
    val products: LiveData<Product> = _products

    var sizeLength: LiveData<String> = Transformations.map(_products) { product ->
        getManufactureSize(product, 0)
    }
    var sizeWidth: LiveData<String> = Transformations.map(_products) { product ->
        getManufactureSize(product, 1)
    }
    var sizeHeight: LiveData<String> = Transformations.map(_products) { product ->
        getManufactureSize(product, 2)
    }
    var manufactureDate: MutableLiveData<String> = MutableLiveData()        // 제조일자

    fun loadProductData(productId: Long) {
        val loadProduct = repository.getProductById(productId)
        _products.postValue(loadProduct)
    }

    fun setImageFilePath(filePath: String) {
        _products.value.imagePath = filePath
    }

    private fun getManufactureSize(product: Product, index: Int): String {
        val arrayProductSize = product.size.split("x")
        if(arrayProductSize.size == 3) {
            if(arrayProductSize[index] != "0") {
                return arrayProductSize[index]
            }
        }
        return ""
    }

    fun onLabelChange(text: CharSequence) {
        _products.value.label = if (text.isNotEmpty()) text.toString() else ""
    }

    fun onLocationChange(text: CharSequence) {
        _products.value.location = if(text.isNotEmpty()) text.toString() else ""
    }

    fun onNameChange(text: CharSequence) {
        _products.value.name = if(text.isNotEmpty()) text.toString() else ""
    }

    fun onManufacturerChange(text: CharSequence) {
        _products.value.manufacturer = if (text.isNotEmpty()) text.toString() else ""
    }

    fun onModelChange(text: CharSequence) {
        _products.value.model = if (text.isNotEmpty()) text.toString() else ""
    }

    fun onReplaceSize(index: Int, newValue: String) {
        val strOldSize = _products.value.size
        val intValue = newValue.toIntOrNull()

        if(newValue.isEmpty() || intValue == null)
            return

        val arrayProductSize: MutableList<String> = strOldSize.split("x").toMutableList()
        if(arrayProductSize.size == 3) {
            arrayProductSize[index] = newValue
            _products.value.size = buildString {
                append(arrayProductSize[0])
                    .append("x").append(arrayProductSize[1])
                    .append("x").append(arrayProductSize[2])
            }
        }
    }

    fun onManufactureDateChange(text: CharSequence) {
        manufactureDate.value = text.toString()

        DateUtils.convertStringToDate(text.toString())?.let {
            _products.value.manufactureDateX = it
        }
    }

    fun onAmountChange(text: CharSequence) {
        _products.value.amount = if (text.isNotEmpty()) text.toString().toInt() else 0
    }

    fun onNoteChange(text: CharSequence) {
        _products.value.note = if (text.isNotEmpty()) text.toString() else ""
    }

    /**
     * 날짜 포멧타입 확인
     */
    private fun isValidManufacturerDate(): Boolean {
        if(!manufactureDate.value.isNullOrBlank()
            && _products.value.manufactureDateX.time == DEFAULT_DATE.time) {
            return false
        }
        return true
    }

    /**
     * 모델 사이즈 값이 Default 동일하다면, "" 처리
     */
    private fun isValidManufactureSize() {
        if(_products.value.size == DEFAULT_SIZE) {
            _products.value.size = ""
        }
    }

    // 상태값 변경
    fun onConditionTypeChanged(checkedId: Int) {
        DKLog.debug(TAG) { "id : $checkedId" }
        when(checkedId) {
            R.id.radio_input_condition_high ->
                _products.value.condition = Condition.HIGH
            R.id.radio_input_condition_middle ->
                _products.value.condition = Condition.MIDDLE
            R.id.radio_input_condition_low ->
                _products.value.condition = Condition.LOW
        }
    }

    // 삭제버튼
    fun onClickClear() {
        CoroutineScope(Dispatchers.IO).launch {
            repository.delete(_products.value)
            _products.postValue(Product())
            showToastMessage(R.string.message_success_delete)
        }
        showToastMessage(R.string.message_success_delete)
    }

    // 저장버튼
    fun onClickSave() {
        isValidManufactureSize()

        if(isValidManufacturerDate()) {
            viewModelScope.launch {
                DKLog.debug("bbong") { "saveData() : ${products.value}" }
                repository.insert(_products.value)
                showToastMessage(R.string.message_success_save)
                _products.postValue(Product())
            }
        } else {
            showToastMessage(R.string.message_invalid_date)
        }
    }
}

