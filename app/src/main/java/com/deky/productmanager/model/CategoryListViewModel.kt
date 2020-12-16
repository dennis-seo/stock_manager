package com.deky.productmanager.model

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.deky.productmanager.database.entity.Category
import com.deky.productmanager.database.repository.CategoryRepository
import kotlinx.coroutines.launch


/*
* Copyright (C) 2020 Kakao corp. All rights reserved.
*
* Created by Dennis.Seo on 15/05/2020
*
*/
class CategoryListViewModel(application: Application): BaseViewModel(application){

    private var repository: CategoryRepository = CategoryRepository(application)
    internal val mainCategory : LiveData<List<Category>> = repository.getMainCategory()
//    internal val subCategory : LiveData<List<Category>> = repository.getProductByParentId()

    fun delete(category: Category) {
        viewModelScope.launch {
            repository.delete(category)
        }
    }
}