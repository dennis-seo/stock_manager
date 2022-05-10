package com.deky.productmanager.database.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.deky.productmanager.database.ModelDB
import com.deky.productmanager.database.ModelDao
import com.deky.productmanager.database.entity.Model


/*
* Created by Dennis.Seo on 10/05/2022
*/
class ModelRepository(application: Application) {

    private var modelDao: ModelDao

    init {
        val database = ModelDB.getInstance(application.applicationContext)
        modelDao = database.modelDao()
    }

    suspend fun insert(model: Model) {
        modelDao.insert(model)
    }

    suspend fun delete(model: Model) {
        modelDao.delete(model)
    }

    fun getModelByParentId(parentId: Long ): LiveData<List<Model>> {
        return modelDao.getModelByParentId(parentId)
    }

    fun getModelById(model: Long): Model {
        return modelDao.getModelById(model)
    }

    fun getMainCategory(): LiveData<List<Model>> {
        return modelDao.getMainCategory()
    }

}