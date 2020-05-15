package com.deky.productmanager.model

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseMethod
import androidx.lifecycle.*
import com.deky.productmanager.database.ProductDB
import com.deky.productmanager.database.entity.Condition
import com.deky.productmanager.database.entity.Product
import com.deky.productmanager.util.DKLog
import com.deky.productmanager.util.DateUtils
import com.deky.productmanager.util.NotNullMutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.*
import java.util.concurrent.Executors


class InputViewModel: ViewModel() {
    companion object {
        private const val TAG = "InputViewModel"
    }


//    private var mLabel: String = ""
//        get() = if (field.isEmpty()) "" else field
//    private var mImagePath: String =""
//        get() = if (field.isEmpty()) "" else field
//    private var mLocation: String = ""
//        get() = if (field.isEmpty()) "" else field
//    private var mName: String = ""
//        get() = if (field.isEmpty()) "" else field
//    private var mManufacturer: String = ""
//        get() = if (field.isEmpty()) "" else field
//    private var mModel: String = ""
//        get() = if (field.isEmpty()) "" else field
//
//    private var mSizeLength: String = ""
//        get() = if (field.isEmpty()) "" else field
//    private var mSizeWidth: String = ""
//        get() = if (field.isEmpty()) "" else field
//    private var mSizeHeight: String = ""
//        get() = if (field.isEmpty()) "" else field
//
//    private var mManufactureDate: String = ""
//        get() = if (field.isEmpty()) "" else field
//    private var mAmount: String = ""
//        get() = if (field.isEmpty()) "" else field
//    private var mNote: String = ""
//        get() = if (field.isEmpty()) "" else field
    private var products: NotNullMutableLiveData<Product> = NotNullMutableLiveData(Product())
    fun getProducts() = products

    private var label: MutableLiveData<String> = MutableLiveData()                  // 라벨번호
    private var imagePath: MutableLiveData<String> = MutableLiveData()              // 이미지
    private var location: MutableLiveData<String> = MutableLiveData()               // 위치
    private var name: MutableLiveData<String> = MutableLiveData()                   // 품명
    private var manufacturer: MutableLiveData<String> = MutableLiveData()           // 제조사
    private var model: MutableLiveData<String> = MutableLiveData()                  // 모델명

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
    var condition: LiveData<Condition> = Transformations.map(products) { product ->
        product.condition
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

//    private fun getManufactureSize(): String {
//        if(mSizeLength.isEmpty()) {
//            Log.d("bbong", "mSizeLength is null")
//        }
//        if(mSizeWidth.isEmpty()) {
//            Log.d("bbong", "mSizeWidth is null")
//        }
//        if(mSizeHeight.isEmpty()) {
//            Log.d("bbong", "mSizeHight is null")
//        }
//
//        return if (mSizeLength.isEmpty()
//            || mSizeWidth.isEmpty()
//            || mSizeHeight.isEmpty()) ""
//        else buildString {
//            append(mSizeLength).append("x")
//            append(mSizeWidth).append("x")
//            append(mSizeHeight)
//        }
//    }

    fun onLabelChange(text: CharSequence) {
        products.value.label = if (text.isNotEmpty()) text.toString() else ""
    }

    fun onLocationChange(text: CharSequence) {
        products.value.location = if(text.isNotEmpty()) text.toString() else ""
    }

    fun onNameChange(text: CharSequence) {
        products.value.name = if(text.isNotEmpty()) text.toString() else ""
        DKLog.debug("bbong") { "name : ${products.value.name}"}
    }

    fun onManufacturerChange(text: CharSequence) {
        products.value.manufacturer = if (text.isNotEmpty()) text.toString() else ""
    }

    fun onModelChange(text: CharSequence) {
        products.value.model = if (text.isNotEmpty()) text.toString() else ""
    }

    fun onSizeLengthChange(text: CharSequence) {
        onReplaceSize(0, text.toString())
    }

    fun onSizeWidthChange(text: CharSequence) {
        onReplaceSize(1, text.toString())
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


//    class Factory(private val application: Application) : ViewModelProvider.Factory {
//        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
//            return InputViewModel(application) as T
//        }
//    }
}

