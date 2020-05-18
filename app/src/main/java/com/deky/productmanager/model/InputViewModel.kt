package com.deky.productmanager.model

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.deky.productmanager.R
import com.deky.productmanager.database.entity.DEFAULT_DATE
import com.deky.productmanager.database.entity.DEFAULT_SIZE
import com.deky.productmanager.database.entity.Product
import com.deky.productmanager.database.repository.ProductRepository
import com.deky.productmanager.util.DKLog
import com.deky.productmanager.util.DateUtils
import com.deky.productmanager.util.NotNullMutableLiveData
import kotlinx.coroutines.launch


class InputViewModel(application: Application): ProductsBaseViewModel(application) {
    companion object {
        private const val TAG = "InputViewModel"
    }

    private var repository: ProductRepository = ProductRepository(application)

    private var products: NotNullMutableLiveData<Product> = NotNullMutableLiveData(Product())
    fun getProducts() = products

    var sizeLength: LiveData<String> = Transformations.map(products) { product ->
        getManufactureSize(product, 0)
    }
    var sizeWidth: LiveData<String> = Transformations.map(products) { product ->
        getManufactureSize(product, 1)
    }
    var sizeHeight: LiveData<String> = Transformations.map(products) { product ->
        getManufactureSize(product, 2)
    }
    var manufactureDate: MutableLiveData<String> = MutableLiveData()        // 제조일자

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
        products.value.label = if (text.isNotEmpty()) text.toString() else ""
    }

    fun onLocationChange(text: CharSequence) {
        products.value.location = if(text.isNotEmpty()) text.toString() else ""
    }

    fun onNameChange(text: CharSequence) {
        products.value.name = if(text.isNotEmpty()) text.toString() else ""
    }

    fun onManufacturerChange(text: CharSequence) {
        products.value.manufacturer = if (text.isNotEmpty()) text.toString() else ""
    }

    fun onModelChange(text: CharSequence) {
        products.value.model = if (text.isNotEmpty()) text.toString() else ""
    }

    fun onReplaceSize(index: Int, newValue: String) {
        val strOldSize = products.value.size
        val intValue = newValue.toIntOrNull()

        if(newValue.isEmpty() || intValue == null)
            return

        val arrayProductSize: MutableList<String> = strOldSize.split("x").toMutableList()
        if(arrayProductSize.size == 3) {
            arrayProductSize[index] = newValue
            products.value.size = buildString {
                append(arrayProductSize[0])
                    .append("x").append(arrayProductSize[1])
                    .append("x").append(arrayProductSize[2])
            }
        }
    }

    fun onManufactureDateChange(text: CharSequence) {
        manufactureDate.value = text.toString()

        DateUtils.convertStringToDate(text.toString())?.let {
            products.value.manufactureDate = it
        }
    }

    fun onAmountChange(text: CharSequence) {
        products.value.amount = if (text.isNotEmpty()) text.toString().toInt() else 0
    }

    fun onNoteChange(text: CharSequence) {
        products.value.note = if (text.isNotEmpty()) text.toString() else ""
    }


    // 삭제버튼
    fun onClickClear() {
        products.postValue(Product())
        showToastMessage(R.string.message_success_delete)
    }

    // 저장버튼
    fun onClickSave() {
        isValidManufactureSize()

        if(isValidManufacturerDate()) {
            viewModelScope.launch {
                DKLog.debug("bbong") { "saveData() : ${products.value}" }
                repository.insert(products.value)
                showToastMessage(R.string.message_success_save)
                products.postValue(Product())
            }
        } else {
            showToastMessage(R.string.message_invalid_date)
        }
    }

    /**
     * 날짜 포멧타입 확인
     */
    private fun isValidManufacturerDate(): Boolean {
        if(!manufactureDate.value.isNullOrBlank()
            && products.value.manufactureDate.time == DEFAULT_DATE.time) {
            return false
        }
        return true
    }

    /**
     * 모델 사이즈 값이 Default 동일하다면, "" 처리
     */
    private fun isValidManufactureSize() {
        if(products.value.size == DEFAULT_SIZE) {
            products.value.size = ""
        }
    }
}

