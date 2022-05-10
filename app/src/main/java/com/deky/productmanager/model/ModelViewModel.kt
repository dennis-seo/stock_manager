package com.deky.productmanager.model

import android.app.Application
import androidx.arch.core.util.Function
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.deky.productmanager.database.entity.Model
import com.deky.productmanager.database.repository.ModelRepository
import kotlinx.coroutines.launch


/*
* Created by dennis on 10/05/2022
*
*/
class ModelViewModel(application: Application): BaseViewModel(application) {
    private val TAG = "ManufacturerViewModel"

    private val repository by lazy {
        ModelRepository(application)
    }

    internal var mainCategory: LiveData<List<Model>> = repository.getMainCategory()
    internal var selectedCategory: MutableLiveData<Model> = MutableLiveData()
    internal val subCategory: LiveData<List<Model>>

    var parentId = MutableLiveData<Long>()

    init {
        subCategory = Transformations.switchMap(
            parentId,
            Function<Long?, LiveData<List<Model>>> { parentId ->
                if(parentId == null || parentId == -1L) {
                    return@Function MutableLiveData<List<Model>>()
                }
                return@Function repository.getModelByParentId(parentId)
            }
        )
    }


    fun insertMainModel(name: String) {
        val model = Model(-1, name)
        viewModelScope.launch {
            repository.insert(model)
        }
    }

    fun insertSubModel(name: String) {
        selectedCategory.value?.let { mainCategory ->
            val model = Model(mainCategory.id, name)
            viewModelScope.launch {
                repository.insert(model)
                parentId.postValue(mainCategory.id)
            }
        }
    }

    fun delete(model: Model) {
        viewModelScope.launch {
            if (model.parentCategory == -1L) {
                val childCategory = repository.getModelByParentId(model.id)
                childCategory.value?.forEach {
                    repository.delete(it)
                }
                repository.delete(model)
            } else {
                repository.delete(model)
                parentId.postValue(model.parentCategory)
            }
        }
    }

}