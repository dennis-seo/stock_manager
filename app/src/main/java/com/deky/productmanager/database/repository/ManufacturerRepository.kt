package com.deky.productmanager.database.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.deky.productmanager.database.ManufacturerDB
import com.deky.productmanager.database.ManufacturerDao
import com.deky.productmanager.database.entity.Manufacturer
import com.deky.productmanager.util.DKLog

/*
* Copyright (C) 2021 Kakao Corp. All rights reserved.
*
* Created by Diane on 16/01/2021
*
*/
class ManufacturerRepository(application: Application) {
    private val manufacturerDao: ManufacturerDao by lazy {
        val database = ManufacturerDB.getInstance(application.applicationContext)
        return@lazy database.manufacturerDao()
    }

    private val TAG = "ManufacturerRepository"

    fun insert(manufacturer: Manufacturer) {
        manufacturerDao.insert(manufacturer)
        DKLog.debug(TAG) {
            "Repository > insert: $manufacturer"
        }
    }

    fun delete(manufacturer: Manufacturer) {
        manufacturerDao.delete(manufacturer)
    }

    fun getManufacturerByParentId(parentId: Long ): LiveData<List<Manufacturer>> {
        return manufacturerDao.getManufacturerByParentId(parentId)
    }

    fun getMainCategory(): LiveData<List<Manufacturer>> {
        return manufacturerDao.getMainCategory()
    }

    fun getManufactureryById(id: Long): Manufacturer {
        return manufacturerDao.getManufacturerById(id)
    }
}