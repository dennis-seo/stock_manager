package com.deky.productmanager.database

import android.content.Context
import android.os.Build
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.deky.productmanager.database.entity.Category
import com.deky.productmanager.database.entity.ConditionConverter
import com.deky.productmanager.database.entity.Manufacturer
import com.deky.productmanager.database.entity.TimeConverter

/*
* Copyright (C) 2021 Kakao Corp. All rights reserved.
*
* Created by Diane on 16/01/2021
*
*/
private const val VERSION_1 = 1
private const val DB_VERSION = VERSION_1
@Database(entities = [(Manufacturer::class)], version = DB_VERSION, exportSchema = false)
@TypeConverters(TimeConverter::class, ConditionConverter::class)
abstract class ManufacturerDB : RoomDatabase() {
    companion object {
        private const val TAG = "ManufacturerDB"
        private lateinit var instance: ManufacturerDB

        fun getInstance(context: Context): ManufacturerDB {
            if (!this::instance.isInitialized) {
                synchronized(ManufacturerDB::class) {
                    if (!this::instance.isInitialized) {
                        instance = Room.databaseBuilder(context.applicationContext,
                        ManufacturerDB::class.java, "Manufacturer.db")
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build()
                    }
                }
            }
            return instance
        }
    }

    abstract fun manufacturerDao(): ManufacturerDao
}