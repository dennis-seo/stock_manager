package com.deky.productmanager.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Delete
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

    @Query(value = "SELECT * FROM Product WHERE location LIKE '%' || :keyword || '%' OR name LIKE '%' || :keyword || '%' OR manufacturer LIKE '%' || :keyword || '%'")
    fun findProduct(keyword: String): List<Product>?

    @Query("SELECT * FROM Product WHERE _id = :productId")
    fun getProductById(productId: Long): Product

    @Query("SELECT * FROM Product ORDER BY _id DESC")
    fun getAllProducts(): List<Product>

    @Query("SELECT * FROM Product WHERE favorite = 1")
    fun getFavoriteProducts(): List<Product>

    @Query(value = "SELECT * FROM Product WHERE favorite = 1 AND " +
            "location LIKE '%' || :keyword || '%' OR name LIKE '%' || :keyword || '%' OR manufacturer LIKE '%' || :keyword || '%'")
    fun findFavoriteProducts(keyword: String): List<Product>?

    @Insert(onConflict = REPLACE)
    fun insert(product: Product)

    @Delete
    fun delete(product: Product)

    @Query("DELETE from Product")
    fun deleteAll()

    @Query("SELECT COUNT(*) FROM Product")
    fun getCount(): Int
}