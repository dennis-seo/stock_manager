package com.deky.productmanager.database.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.deky.productmanager.database.ProductDB
import com.deky.productmanager.database.ProductDao
import com.deky.productmanager.database.entity.Product
import com.deky.productmanager.util.NotNullMutableLiveData


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

    suspend fun insert(product: Product) {
        productDao.insert(product)
    }

    suspend fun delete(product: Product) {
        productDao.delete(product)
    }

    fun getProductById(productId: Long): Product {
        return productDao.getProductById(productId)
    }

    fun findProduct(keyword: String): List<Product>? {
        return productDao.findProduct(keyword)
    }

    fun getAllProducts(): List<Product> {
        return productDao.getAllProducts()
    }

    fun getFavoriteProducts(): List<Product> {
        return productDao.getFavoriteProducts()
    }

    fun findFavoriteProducts(keyword: String): List<Product>? {
        return productDao.findFavoriteProducts(keyword)
    }
}