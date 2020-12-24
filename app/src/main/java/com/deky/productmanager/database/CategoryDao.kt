package com.deky.productmanager.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.deky.productmanager.database.entity.Category
import com.deky.productmanager.database.entity.Product


/*
* Copyright (C) 2020 Kakao corp. All rights reserved.
*/

@Dao
interface CategoryDao {
    @Query("SELECT * FROM Category WHERE parentCategory = -1")
    fun getMainCategory(): LiveData<List<Category>>

//    @Query("SELECT * FROM Category WHERE parentCategory = :parentId")
//    fun getCategoryByParentId(parentId: Long): LiveData<List<Category>>

    @Query("SELECT * FROM Category WHERE parentCategory = :parentId")
    fun getCategoryByParentId(parentId: Long): List<Category>

    @Query("SELECT * FROM Category")
    fun getCategoryAll(): List<Category>

//    @Query("SELECT * FROM Category WHERE _id = :id")
//    fun getCategoryById(id: Long): Category

//    @Query("SELECT * FROM Category ORDER BY _id DESC")
//    fun getMainCategory(): LiveData<List<Category>>

    @Insert(onConflict = REPLACE)
    fun insert(category: Category)

    @Delete
    fun delete(category: Category)

    @Query("DELETE from Category")
    fun deleteAll()

    @Query("SELECT COUNT(*) FROM Category")
    fun getCount(): Int
}