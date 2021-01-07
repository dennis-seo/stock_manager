package com.deky.productmanager.model

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.deky.productmanager.database.entity.Product
import com.deky.productmanager.database.repository.ProductRepository
import com.deky.productmanager.util.DKLog
import kotlinx.coroutines.launch


/*
* Copyright (C) 2020 Kakao corp. All rights reserved.
*
* Created by Dennis.Seo on 15/05/2020
*
*/
class DataListViewModel(application: Application): BaseViewModel(application){
    companion object {
        private const val TAG = "DataListViewModel"
    }
    private var repository: ProductRepository = ProductRepository(application)
    internal val products: MutableLiveData<List<Product>> = MutableLiveData()
    internal val keyword: MutableLiveData<String> = MutableLiveData()

    init {
        getAllProduct()
    }

    fun getAllProduct() {
        viewModelScope.launch {
            val product = repository.getAllProducts()
            products.postValue(product)
        }
    }

    fun findProduct(keyword: String) {
        DKLog.info(TAG) { "findProduct() > keyword : $keyword" }
        viewModelScope.launch {
            val product = repository.findProduct(keyword)
            products.postValue(product)
        }
    }

    fun delete(product: Product) {
        viewModelScope.launch {
            repository.delete(product)
        }
    }
}