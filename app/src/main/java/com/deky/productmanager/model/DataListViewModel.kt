package com.deky.productmanager.model

import android.app.Application
import androidx.lifecycle.LiveData
import com.deky.productmanager.database.entity.Product
import com.deky.productmanager.database.repository.ProductRepository


/*
* Copyright (C) 2020 Kakao corp. All rights reserved.
*
* Created by Dennis.Seo on 15/05/2020
*
*/
class DataListViewModel(application: Application): BaseViewModel(application){

    private var repository: ProductRepository = ProductRepository(application)
    internal val products : LiveData<List<Product>> = repository.getAllProducts()

    fun delete(product: Product) {
        repository.delete(product)
    }
}