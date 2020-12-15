package com.deky.productmanager.database.repository

import android.app.Application
import android.preference.PreferenceManager
import androidx.lifecycle.LiveData
import com.deky.productmanager.database.ProductDB
import com.deky.productmanager.database.ProductDao
import com.deky.productmanager.database.entity.Product
import org.json.JSONArray
import org.json.JSONException


/*
* Copyright (C) 2020 Kakao corp. All rights reserved.
*
* Created by Dennis.Seo on 18/05/2020
*
*/
class CategoryRepository(application: Application) {

    private var productDao: ProductDao

    init {
        val database = ProductDB.getInstance(application.applicationContext)
        productDao = database.productDao()
    }

    suspend fun insert(product: Product) {
        productDao.insert(product)
    }

    suspend fun delete(product: Product) {
        productDao.delete(product)
    }

    fun getProductById(productId: Long): Product {
        return productDao.getProductById(productId)
    }

    fun getAllProducts(): LiveData<List<Product>> {
        return productDao.getAllProducts()
    }
}