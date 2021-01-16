package com.deky.productmanager.model

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.deky.productmanager.database.entity.Manufacturer
import com.deky.productmanager.database.repository.ManufacturerRepository
import kotlinx.coroutines.launch
import androidx.arch.core.util.Function


/*
* Copyright (C) 2021 Kakao Corp. All rights reserved.
*
* Created by Diane on 16/01/2021
*
*/
class ManufacturerViewModel(application: Application): BaseViewModel(application) {
    private val TAG = "ManufacturerViewModel"

    private val repository by lazy {
        ManufacturerRepository(application)
    }

    internal var mainCategory: LiveData<List<Manufacturer>> = repository.getMainCategory()
    internal var selectedCategory: MutableLiveData<Manufacturer> = MutableLiveData()
    internal val subCategory: LiveData<List<Manufacturer>>

    var parentId = MutableLiveData<Long>()

    init {
        subCategory = Transformations.switchMap(
            parentId,
            Function<Long?, LiveData<List<Manufacturer>>> { parentId ->
                if(parentId == null || parentId == -1L) {
                    return@Function MutableLiveData<List<Manufacturer>>()
                }
                return@Function repository.getManufacturerByParentId(parentId)
            }
        )
    }


    fun insertMainManufacturer(name: String) {
        val manufacturer = Manufacturer(-1, name)
        viewModelScope.launch {
            repository.insert(manufacturer)
        }
    }

    fun insertSubManufacturer(name: String) {
        selectedCategory.value?.let { mainCategory ->
            val manufacturer = Manufacturer(mainCategory.id, name)
            viewModelScope.launch {
                repository.insert(manufacturer)
                parentId.postValue(mainCategory.id)
            }
        }
    }

    fun delete(manufacturer: Manufacturer) {
        viewModelScope.launch {
            if (manufacturer.parentCategory == -1L) {
                val childCategory = repository.getManufacturerByParentId(manufacturer.id)
                childCategory.value?.forEach {
                    repository.delete(it)
                }
                repository.delete(manufacturer)
            } else {
                repository.delete(manufacturer)
                parentId.postValue(manufacturer.parentCategory)
            }
        }
    }

}