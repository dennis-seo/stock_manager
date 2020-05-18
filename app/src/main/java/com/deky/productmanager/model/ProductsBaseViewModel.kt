package com.deky.productmanager.model

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.*
import com.deky.productmanager.database.ProductDB
import com.deky.productmanager.database.entity.Product
import com.deky.productmanager.util.Event


/*
* Copyright (C) 2020 Kakao corp. All rights reserved.
*
* Created by Dennis.Seo on 15/05/2020
*
*/
open class ProductsBaseViewModel(application: Application): AndroidViewModel(application){
    internal val database = ProductDB.getInstance(application)

    // Mutable/LiveData of String resource reference Event
    private val _toastMessage = MutableLiveData<Event<Int>>()
    val toastMessage : LiveData<Event<Int>>
        get() = _toastMessage

    // Post in background thread
    fun showToastMessage(@StringRes message: Int) {
        _toastMessage.postValue(Event(message))
    }

    // Post in main thread
    fun setToastMessage(@StringRes message: Int) {
        _toastMessage.value = Event(message)
    }


    @Deprecated (message = "구조변경중")
    fun insert(product: Product){
        database.productDao().insert(product)
    }

    @Deprecated (message = "구조변경중")
    fun delete(product: Product) {
        database.productDao().delete(product)
    }

    @Deprecated (message = "구조변경중")
    fun getAllProducts(): LiveData<List<Product>> {
        return database.productDao().getAllProducts()
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return modelClass.getConstructor(Application::class.java).newInstance(application)
        }
    }
}