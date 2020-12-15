package com.deky.productmanager.model

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.deky.productmanager.database.entity.Product
import com.deky.productmanager.database.repository.ProductRepository
import kotlinx.coroutines.launch


/*
* Copyright (C) 2020 Kakao corp. All rights reserved.
*
* Created by Dennis.Seo on 15/05/2020
*
*/
class CategoryListViewModel(application: Application): BaseViewModel(application){

    private var repository: ProductRepository = ProductRepository(application)
    internal val mainCategory : LiveData<List<String>> = repository.getMainCategory()
    internal val subCategory : LiveData<List<String>> = repository.getSubCategory()

    fun delete(product: Product) {
        viewModelScope.launch {
            repository.delete(product)
        }
    }
}