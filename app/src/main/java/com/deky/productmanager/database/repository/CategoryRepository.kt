package com.deky.productmanager.database.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.deky.productmanager.database.CategoryDB
import com.deky.productmanager.database.CategoryDao
import com.deky.productmanager.database.entity.Category


/*
* Copyright (C) 2020 Kakao corp. All rights reserved.
*
* Created by Dennis.Seo on 18/05/2020
*
*/
class CategoryRepository(application: Application) {

    private var categoryDao: CategoryDao

    init {
        val database = CategoryDB.getInstance(application.applicationContext)
        categoryDao = database.categoryDao()
    }

    suspend fun insert(category: Category) {
        categoryDao.insert(category)
    }

    suspend fun delete(category: Category) {
        categoryDao.delete(category)
    }

    fun getProductByParentId(parentId: Long): LiveData<List<Category>> {
        return categoryDao.getCategoryByParentId(parentId)
    }

    fun getMainCategory(): LiveData<List<Category>> {
        return categoryDao.getMainCategory()
    }
}