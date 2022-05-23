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
enum class ListType {
    PRODUCTS,
    FAVORITES
}
class DataListViewModel(application: Application/*, params: Map<String, Any?>*/): BaseViewModel(application){
    companion object {
        private const val TAG = "DataListViewModel"
        const val PARAM_LIST_TYPE = "listType"
    }
    private var repository: ProductRepository = ProductRepository(application)
    internal val products: MutableLiveData<List<Product>> = MutableLiveData()
    internal val keyword: MutableLiveData<String> = MutableLiveData()

    init {
//        when(params[PARAM_LIST_TYPE]) {
//            ListType.PRODUCTS -> getAllProduct()
//            ListType.FAVORITES -> getFavoriteProduct()
//            else -> getAllProduct()
//        }
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

    fun getFavoriteProduct() {
        viewModelScope.launch {
            val product = repository.getFavoriteProducts()
            products.postValue(product)
            DKLog.debug("bbong") {
                "product : $product"
            }
        }
    }

    fun findFavoriteProduct(keyword: String) {
        DKLog.info(TAG) { "findProduct() > keyword : $keyword" }
        viewModelScope.launch {
            val product = repository.findFavoriteProducts(keyword)
            products.postValue(product)
        }
    }

    fun delete(product: Product) {
        viewModelScope.launch {
            repository.delete(product)
            products.postValue(products.value?.minus(product))
        }
    }
}