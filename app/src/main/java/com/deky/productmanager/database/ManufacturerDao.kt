package com.deky.productmanager.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.deky.productmanager.database.entity.Manufacturer

/*
* Copyright (C) 2021 Kakao Corp. All rights reserved.
*
* Created by Diane on 16/01/2021
*
*/
@Dao
interface ManufacturerDao {
    @Query("SELECT * FROM manufacturer WHERE parentCategory = -1")
    fun getMainCategory(): LiveData<List<Manufacturer>>

    @Query("SELECT * FROM manufacturer WHERE parentCategory = :parentId")
    fun getManufacturerByParentId(parentId: Long): LiveData<List<Manufacturer>>

    @Query("SELECT * FROM manufacturer WHERE _id = :id")
    fun getManufacturerById(id: Long): Manufacturer

    @Insert(onConflict = REPLACE)
    fun insert(manufacturer: Manufacturer)

    @Delete
    fun delete(manufacturer: Manufacturer)

    @Query("DELETE from manufacturer")
    fun deleteAll()

    @Query("SELECT COUNT(*) FROM manufacturer")
    fun getCount(): Int
}