package com.deky.productmanager.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.deky.productmanager.database.entity.Condition
import com.deky.productmanager.database.entity.ConditionConverter
import com.deky.productmanager.database.entity.Product
import com.deky.productmanager.database.entity.TimeConverter
import com.deky.productmanager.util.DKLog
import org.jetbrains.annotations.TestOnly
import java.util.*


/*
* Copyright (C) 2020 Kakao corp. All rights reserved.
*/

/**
 * 최초 Database 버전
 */
private const val VERSION_1 = 1
private const val DB_VERSION = VERSION_1

@Database(entities = [(Product::class)], version = DB_VERSION, exportSchema = false)
@TypeConverters(TimeConverter::class, ConditionConverter::class)
abstract class ProductDB : RoomDatabase() {
    companion object {
        private const val TAG = "ProductDB"

        private lateinit var instance: ProductDB

        @JvmStatic
        fun getInstance(context: Context): ProductDB {
            if (!this::instance.isInitialized) {
                synchronized(ProductDB::class) {
                    if (!this::instance.isInitialized) {
                        instance = Room.databaseBuilder(context.applicationContext,
                            ProductDB::class.java, "Products.db")
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build()
                    }
                }
            }

            return instance
        }
    }

    abstract fun productDao(): ProductDao

    @TestOnly
    fun setSampleData() {
        DKLog.info(TAG) { "setSampleData()" }

        productDao().also { dao ->
            // 전체 삭제
            dao.deleteAll()

            // 가상 데이터 입력
            val sampleDate = Date(System.currentTimeMillis())
            for (index in 1..10) {
                dao.insert(Product(
                    /* label */ "Label-$index",
                    /* location */ "회의실_$index",
                    /* name */ "노트북",
                    /* imagePath */ "",
                    /* manufacturer */ "Apple",
                    /* manufactureDate */ sampleDate,
                    /* condition */ Condition.getCondition(index % Condition.values().size),
                    /* size */ "15-inch",
                    /* model */ "MacBook Pro (2019)",
                    /* amount */ index)
                )
            }

            // 가상 데이터 로그
            dao.getAll().also { list ->
                if (list.isNotEmpty()) {
                    DKLog.debug(TAG) {
                        buildString {
                            list.forEach { item ->
                                append("item[${item.id}]: ").append(item).append("\n")
                            }
                        }
                    }
                }
            }
        }
    }
}