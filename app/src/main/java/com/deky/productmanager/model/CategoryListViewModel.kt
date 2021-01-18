package com.deky.productmanager.model

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.deky.productmanager.database.entity.Category
import com.deky.productmanager.database.repository.CategoryRepository
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
    internal val mainCategory: LiveData<List<Category>> = repository.getMainCategory()
    internal var subCategory: MutableLiveData<List<Category>?> = MutableLiveData()

    internal var selectedCategory: MutableLiveData<Category> = MutableLiveData()

    init {
        val allCategory = repository.getCategoryAll()
        DKLog.info(TAG) { "all Category : $allCategory"}
    }


    fun insertMainCategory(categoryName: String) {
        val category = Category(-1, categoryName)
        viewModelScope.launch {
            repository.insert(category)
        }
    }

    fun insertSubCategory(categoryName: String) {
        selectedCategory.value?.let { parentCategory ->
            val category = Category(parentCategory.id, categoryName)
            viewModelScope.launch {
                repository.insert(category)
                updateSubCategory(parentCategory.id)
            }
        }
    }


    fun updateSubCategory(parentId: Long) {
        subCategory.postValue(repository.getCategoryByParentId(parentId))
        DKLog.debug(TAG) { "updateSubCategory() - parent id : $parentId / size : ${subCategory.value?.size}" }
    }

    fun clearSubCategory() {
        subCategory.postValue(null)
    }

    fun delete(category: Category) {
        viewModelScope.launch {
            // 카테고리라면
            if(category.parentCategory == -1L) {
                val childCategory = repository.getCategoryByParentId(category.id)
                // 품명 아이템 삭제
                for(item in childCategory) {
                    repository.delete(item)
                }
                // 카테고리 삭제
                repository.delete(category)

            } else {
                // 품명 삭제
                repository.delete(category)
                updateSubCategory(category.parentCategory)
            }
        }
    }
}