package com.deky.productmanager.model

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.deky.productmanager.database.entity.Category
import com.deky.productmanager.database.repository.CategoryRepository
import com.deky.productmanager.ui.CategoryListFragment
import com.deky.productmanager.util.DKLog
import kotlinx.coroutines.launch


/*
* Copyright (C) 2020 Kakao corp. All rights reserved.
*
* Created by Dennis.Seo on 15/05/2020
*
*/
class CategoryListViewModel(application: Application): BaseViewModel(application){
    companion object {
        private const val TAG = "CategoryListViewModel"
    }

    private var repository: CategoryRepository = CategoryRepository(application)
    internal val mainCategory : LiveData<List<Category>> = repository.getMainCategory()
    internal var subCategory : LiveData<List<Category>>? = null


    fun updateSubCategory(parentId: Long) {
        subCategory = repository.getProductByParentId(parentId)
        DKLog.debug(TAG) { "updateSubCategory() : parent id : $parentId / size : ${subCategory?.value?.size}" }
    }

    fun clearSubCategory() {
        subCategory = null
    }

    fun delete(category: Category) {
        viewModelScope.launch {
            repository.delete(category)
        }
    }
}