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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.annotations.TestOnly
import java.io.File
import java.util.*
import kotlin.random.Random


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
    fun setSampleDataSet() {
        DKLog.info(TAG) { "setSampleDataSet()" }

        CoroutineScope(Dispatchers.Default).launch {
            productDao().also { dao ->
                // 전체 삭제
                dao.deleteAll()

                // 가상 데이터 입력
                for (index in 1..10) {
                    dao.insert(createSampleData(dao.getCount() + 1, null))
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

    @TestOnly
    fun setSampleData(imageFile: File?) {
        DKLog.info(TAG) { "setSampleData()" }

        CoroutineScope(Dispatchers.Default).launch {
            productDao().let { dao ->
                dao.insert(createSampleData(dao.getCount() + 1, imageFile))
            }
        }
    }

    @TestOnly
    fun createSampleData(labelIndex: Int, imageFile: File?): Product {
        DKLog.debug(TAG) { "setSampleDataSet() - imageFile : ${imageFile?.name ?: "No Image"}" }

        val currentDate = Date(System.currentTimeMillis())
        return Product(
            /* label */ "Label-$labelIndex",
            /* imagePath */ imageFile?.absolutePath ?: "",
            /* location */ "회의실_$labelIndex",
            /* name */ "노트북",
            /* manufacturer */ "Apple",
            /* manufactureDate */ currentDate,
            /* condition */ Condition.getCondition(labelIndex % Condition.values().size),
            /* size */ "15-inch",
            /* model */ "MacBook Pro (2019)",
            /* amount */ Random.nextInt(1, 10)
        )
    }
}