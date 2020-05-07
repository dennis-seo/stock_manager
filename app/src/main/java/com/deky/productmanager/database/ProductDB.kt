package com.deky.productmanager.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.deky.productmanager.database.entity.Product


/*
* Copyright (C) 2020 Kakao corp. All rights reserved.
*/

/**
 * 최초 Database 버전
 */
private const val VERSION_1 = 1
private const val DB_VERSION = VERSION_1

@Database(entities = [(Product::class)], version = DB_VERSION, exportSchema = false)
abstract class ProductDB : RoomDatabase() {
    companion object {
        private lateinit var instance: ProductDB

        @JvmStatic
        fun getInstance(context: Context): ProductDB {
            if (!this::instance.isInitialized) {
                synchronized(ProductDB::class) {
                    if (!this::instance.isInitialized) {
                        instance = Room.databaseBuilder(context.applicationContext,
                            ProductDB::class.java, "Products.db")
                            .fallbackToDestructiveMigration()
                            .build()
                    }
                }
            }

            return instance
        }
    }

    abstract fun productDao(): ProductDao
}