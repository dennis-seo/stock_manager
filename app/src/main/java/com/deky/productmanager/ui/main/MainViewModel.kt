package com.deky.productmanager.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel

import com.deky.productmanager.database.ProductDB

class MainViewModel : AndroidViewModel {

    private var db: ProductDB? = null


    constructor(application: Application):super(application) {

    }
}
