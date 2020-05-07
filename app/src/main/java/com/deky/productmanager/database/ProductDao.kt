package com.deky.productmanager.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query


/*
* Copyright (C) 2020 Kakao corp. All rights reserved.
*/

@Dao
interface ProductDao {
    @Query("SELECT * FROM Products")
    fun getAll(): List<Product>

    @Insert(onConflict = REPLACE)
    fun insert(cat: Product)

    @Query("DELETE from Products")
    fun deleteAll()
}