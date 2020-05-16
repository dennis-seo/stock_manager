package com.deky.productmanager.model

import android.app.Application
import androidx.lifecycle.LiveData
import com.deky.productmanager.database.entity.Product


/*
* Copyright (C) 2020 Kakao corp. All rights reserved.
*
* Created by Dennis.Seo on 15/05/2020
*
*/
class DataListViewModel(application: Application): ProductsBaseViewModel(application){

    internal val products : LiveData<List<Product>> = getAllProducts()
}