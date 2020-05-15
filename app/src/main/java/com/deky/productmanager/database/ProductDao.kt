package com.deky.productmanager.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.deky.productmanager.database.entity.Product


/*
* Copyright (C) 2020 Kakao corp. All rights reserved.
*/

@Dao
interface ProductDao {
    @Query("SELECT * FROM Product")
    fun getAll(): List<Product>

    @Query("SELECT * FROM Product")
    fun getAllProducts(): LiveData<List<Product>>

    @Insert(onConflict = REPLACE)
    fun insert(product: Product)

    @Query("DELETE from Product")
    fun deleteAll()

    @Query("SELECT COUNT(*) FROM Product")
    fun getCount(): Int
}