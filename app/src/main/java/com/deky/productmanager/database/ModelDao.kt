package com.deky.productmanager.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.deky.productmanager.database.entity.Manufacturer
import com.deky.productmanager.database.entity.Model

/*
* Created by Dennis on 10/05/2022
*/
@Dao
interface ModelDao {
    @Query("SELECT * FROM model WHERE parentCategory = -1")
    fun getMainCategory(): LiveData<List<Model>>

    @Query("SELECT * FROM model WHERE parentCategory = :parentId")
    fun getModelByParentId(parentId: Long): LiveData<List<Model>>

    @Query("SELECT * FROM model WHERE _id = :id")
    fun getModelById(id: Long): Model

    @Insert(onConflict = REPLACE)
    fun insert(model: Model)

    @Delete
    fun delete(model: Model)

    @Query("DELETE from model")
    fun deleteAll()

    @Query("SELECT COUNT(*) FROM model")
    fun getCount(): Int
}