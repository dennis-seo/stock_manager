package com.deky.productmanager.model

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.*
import com.deky.productmanager.database.ProductDB
import com.deky.productmanager.util.Event


/*
* Copyright (C) 2020 Kakao corp. All rights reserved.
*
* Created by Dennis.Seo on 15/05/2020
*
*/
open class BaseViewModel(application: Application): AndroidViewModel(application){
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

//    class Factory(private val application: Application) : ViewModelProvider.NewInstanceFactory() {
//        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
//            if (AndroidViewModel::class.java.isAssignableFrom(modelClass)) {
//                return modelClass.getConstructor(Application::class.java).newInstance(application)
//            }
//            return super.create(modelClass)
//        }
//    }

    class Factory(private val application: Application, private val params: Map<String, Any>? = null)
        : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (AndroidViewModel::class.java.isAssignableFrom(modelClass)) {
                try {
                    return if(params.isNullOrEmpty()) {
                        modelClass.getConstructor(Application::class.java).newInstance(application)

                    } else {
                        modelClass.getConstructor(Application::class.java, Map::class.java)
                            .newInstance(application, params)
                    }
                } catch (e: Exception) { }
            }
            return super.create(modelClass)
        }
    }
}