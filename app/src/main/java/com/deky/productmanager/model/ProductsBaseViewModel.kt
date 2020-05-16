package com.deky.productmanager.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.deky.productmanager.database.ProductDB
import com.deky.productmanager.database.entity.Product


/*
* Copyright (C) 2020 Kakao corp. All rights reserved.
*
* Created by Dennis.Seo on 15/05/2020
*
*/
open class ProductsBaseViewModel(application: Application): AndroidViewModel(application){
    internal val database = ProductDB.getInstance(application)

    fun insert(product: Product){
        database.productDao().insert(product)
    }

    fun delete(product: Product) {
        database.productDao().delete(product)
    }

    fun getAllProducts(): LiveData<List<Product>> {
        return database.productDao().getAllProducts()
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return modelClass.getConstructor(Application::class.java).newInstance(application)
        }
    }
}