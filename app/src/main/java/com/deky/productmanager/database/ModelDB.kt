package com.deky.productmanager.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.deky.productmanager.database.entity.*

/*
* Created by Dennis on 10/05/2022
*/
private const val VERSION_1 = 1
private const val DB_VERSION = VERSION_1
@Database(entities = [(Model::class)], version = DB_VERSION, exportSchema = false)
@TypeConverters(TimeConverter::class, ConditionConverter::class)
abstract class ModelDB : RoomDatabase() {
    companion object {
        private const val TAG = "ModelDB"
        private lateinit var instance: ModelDB

        fun getInstance(context: Context): ModelDB {
            if (!this::instance.isInitialized) {
                synchronized(ManufacturerDB::class) {
                    if (!this::instance.isInitialized) {
                        instance = Room.databaseBuilder(context.applicationContext,
                            ModelDB::class.java, "Model.db")
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build()
                    }
                }
            }
            return instance
        }
    }

    abstract fun modelDao(): ModelDao
}