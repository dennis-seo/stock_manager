package com.deky.productmanager.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase


/*
* Copyright (C) 2020 Kakao corp. All rights reserved.
*/
abstract class ProductDB : RoomDatabase() {
    abstract fun ProductDao(): ProductDao

    companion object {
        private var INSTANCE: ProductDB? = null

        fun getInstance(context: Context): ProductDB? {
            if (INSTANCE == null) {
                synchronized(ProductDB::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        ProductDB::class.java, "Products.db")
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }

}