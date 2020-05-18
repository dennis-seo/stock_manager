package com.deky.productmanager.database.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.deky.productmanager.database.ProductDB
import com.deky.productmanager.database.ProductDao
import com.deky.productmanager.database.entity.Product


/*
* Copyright (C) 2020 Kakao corp. All rights reserved.
*
* Created by Dennis.Seo on 18/05/2020
*
*/
class ProductRepository(application: Application) {

    private var productDao: ProductDao

    init {
        val database = ProductDB.getInstance(application.applicationContext)
        productDao = database.productDao()
    }

    suspend fun insert(product: Product){
        productDao.insert(product)
    }

    fun delete(product: Product) {
        productDao.delete(product)
    }

    fun getAllProducts(): LiveData<List<Product>> {
        return productDao.getAllProducts()
    }
}