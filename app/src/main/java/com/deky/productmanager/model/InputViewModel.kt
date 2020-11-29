package com.deky.productmanager.model

import android.app.Application
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.deky.productmanager.R
import com.deky.productmanager.database.entity.Condition
import com.deky.productmanager.database.entity.DEFAULT_SIZE
import com.deky.productmanager.database.entity.Product
import com.deky.productmanager.database.repository.ProductRepository
import com.deky.productmanager.util.DKLog
import com.deky.productmanager.util.NotNullMutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DEBUG_PROPERTY_VALUE_OFF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.StringBuilder


class InputViewModel(application: Application): BaseViewModel(application) {
    companion object {
        private const val TAG = "InputViewModel"
    }

    private var repository: ProductRepository = ProductRepository(application)

    private val _products: NotNullMutableLiveData<Product> = NotNullMutableLiveData(Product())
    val products: LiveData<Product> = _products

    var manufactureDate: MutableLiveData<String> = MutableLiveData()        // 제조일자
    val numberFormatExceptionEvent: MutableLiveData<String> = MutableLiveData() // 수량 입력시 숫자 아닌거 입력했을때 처리

    fun loadProductData(productId: Long) {
        val loadProduct = repository.getProductById(productId)
        _products.postValue(loadProduct)
    }

    fun setImageFilePath(filePath: String) {
        _products.value.imagePath = filePath
    }

    fun getManufactureSize(product: Product, index: Int): String {
        val arrayProductSize = product.size.split("x")
        if(arrayProductSize.size > index) {
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
        DKLog.debug("bbong") { "onReplaceSize() : ${arrayProductSize.size} > index : $index - newValue : $newValue" }

        arrayProductSize[index] = newValue

        val length = if(arrayProductSize.size > 0) arrayProductSize[0] else ""
        val width = if(arrayProductSize.size > 1) arrayProductSize[1] else ""
        val height = if(arrayProductSize.size > 2) arrayProductSize[2] else ""

        _products.value.size = buildString {
            append(length).append("x").append(width).append("x").append(height)
        }

        DKLog.debug("bbong") { "onReplaceSize() : ${_products.value.size}" }
    }

    fun onManufactureDateChange(text: CharSequence) {
//        manufactureDate.postValue(if (text.isNotEmpty()) splitDate(text.toString()) else "")
//        _products.value.manufactureDate = manufactureDate.value.toString()

        _products.value.manufactureDate = if (text.isNotEmpty()) splitDate(text.toString()) else ""

//        _products.value.manufactureDate = if (text.isNotEmpty()) splitDate(text.toString()) else ""
//        _products.postValue(_products.value)
    }

    fun onAmountChange(text: CharSequence) {
        _products.value.amount = try {
            if (text.isNotEmpty()) text.toString().toInt() else 0
        } catch (e: NumberFormatException) {
            numberFormatExceptionEvent.postValue(_products.value.amount.toString())
            _products.value.amount
        }
    }

    fun onNoteChange(text: CharSequence) {
        _products.value.note = if (text.isNotEmpty()) text.toString() else ""
    }

    fun splitDate(strDate: String): String {
        var tempDate: String = strDate
        if(strDate.contains(".")) {
            tempDate = strDate.replace(".","")
        }

        val length = tempDate.length
        val result: StringBuilder = StringBuilder()

        when {
            length >= 8 -> {
                val year = tempDate.subSequence(0, 4)
                val month = tempDate.subSequence(4, 6)
                val day = tempDate.subSequence(6, 8)
                result.append(year).append(".").append(month).append(".").append(day)
                return result.toString()

            }
            length >= 6 -> {
                val year = tempDate.subSequence(0, 4)
                val month = tempDate.subSequence(4, 6)
                val other = tempDate.substring(6)
                result.append(year).append(".").append(month).append(".").append(other)
                return result.toString()

            }
            length >= 4 -> {
                val year = tempDate.subSequence(0, 4)
                val other = tempDate.substring(4)
                result.append(year).append(".").append(other)
                return result.toString()
            }
            else -> {
                return strDate
            }
        }
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

    // 품명 입력버튼
    fun onClickNameButton(view: View) {
        if(view is Button) {
            _products.value.name = view.text.toString()
            _products.postValue(_products.value)
        }
    }

    // 삭제버튼
    fun onClickClear() {
        CoroutineScope(Dispatchers.IO).launch {
            repository.delete(_products.value)
            _products.postValue(Product())
            showToastMessage(R.string.message_success_delete)
        }
        manufactureDate.value = ""

    }

    // 저장버튼
    fun onClickSave() {
        isValidManufactureSize()

        viewModelScope.launch {
            DKLog.debug("bbong") { "saveData() : ${products.value}" }
            repository.insert(_products.value)
            showToastMessage(R.string.message_success_save)
            manufactureDate.value = ""
            val newProduct = Product()
            newProduct.location = _products.value.location
            _products.postValue(newProduct)
        }
    }
}

