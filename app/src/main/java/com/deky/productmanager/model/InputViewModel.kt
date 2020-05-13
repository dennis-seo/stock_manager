package com.deky.productmanager.model

import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.deky.productmanager.R
import com.deky.productmanager.database.entity.Condition


class InputViewModel : ViewModel() {
    companion object {
        private const val TAG = "InputViewModel"
    }

    private var label: MutableLiveData<String> = MutableLiveData()                  // 라벨번호
    private var imagePath: MutableLiveData<String> = MutableLiveData()              // 이미지
    private var location: MutableLiveData<String> = MutableLiveData()               // 위치
    private var name: MutableLiveData<String> = MutableLiveData()                   // 품명
    private var manufacturer: MutableLiveData<String> = MutableLiveData()           // 제조사
    private var model: MutableLiveData<String> = MutableLiveData()                  // 모델명

    private var sizeLength: MutableLiveData<String> = MutableLiveData()                   // 규격
    private var sizeWidth: MutableLiveData<String> = MutableLiveData()                   // 규격
    private var sizeHeight: MutableLiveData<String> = MutableLiveData()                   // 규격

    private var manufactureDate: MutableLiveData<String> = MutableLiveData()        // 제조일자
    private var amount: MutableLiveData<Int> = MutableLiveData(1)                    // 수량
    private var condition: MutableLiveData<Condition> = MutableLiveData()           // 상태
    private var note: MutableLiveData<String> = MutableLiveData()

    init {
        amount.value = 1
    }

    fun getLabel() = label
    fun getImagePath() = imagePath
    fun getLocation() = location
    fun getName() = name
    fun getManufacturer() = manufacturer
    fun getModel() = model

//    fun getSize() = size
    fun getSizeLength() = sizeLength
    fun getSizeWidth() = sizeWidth
    fun getSizeHeight() = sizeHeight

    fun getManufactureDate() = manufactureDate
    fun getAmount() = amount
    fun getCondition() = condition
    fun getNote() = note

    fun getSize(): String {
        if(sizeLength.value.isNullOrEmpty()) {
            Log.d("bbong", "a")
        }
        if(sizeHeight.value.isNullOrEmpty()) {
            Log.d("bbong", "b")
        }
        if(sizeWidth.value.isNullOrEmpty()) {
            Log.d("bbong", "c")
        }

        return if (sizeLength.value.isNullOrEmpty()
            || sizeWidth.value.isNullOrEmpty()
            || sizeHeight.value.isNullOrEmpty()) ""
        else buildString {
            append(sizeLength.value).append("x")
            append(sizeWidth.value).append("x")
            append(sizeHeight.value)
        }
    }

//    fun getCondition(): Condition {
//        return condition.value?: Condition.NONE
//    }
}
