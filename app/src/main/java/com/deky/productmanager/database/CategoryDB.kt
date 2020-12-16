package com.deky.productmanager.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.deky.productmanager.database.entity.*
import com.deky.productmanager.util.DKLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.annotations.TestOnly
import kotlin.random.Random


/*
* Copyright (C) 2020 Kakao corp. All rights reserved.
*/

/**
 * 최초 Database 버전
 */
private const val VERSION_1 = 1
private const val DB_VERSION = VERSION_1

@Database(entities = [(Category::class)], version = DB_VERSION, exportSchema = false)
@TypeConverters(TimeConverter::class, ConditionConverter::class)
abstract class CategoryDB : RoomDatabase() {
    companion object {
        private const val TAG = "ProductDB"

        private lateinit var instance: CategoryDB

        @JvmStatic
        fun getInstance(context: Context): CategoryDB {
            if (!this::instance.isInitialized) {
                synchronized(CategoryDB::class) {
                    if (!this::instance.isInitialized) {
                        instance = Room.databaseBuilder(context.applicationContext,
                            CategoryDB::class.java, "Category.db")
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build()
                    }
                }
            }

            return instance
        }
    }

    abstract fun categoryDao(): CategoryDao

    @TestOnly
    fun setSampleDataSet() {
        DKLog.info(CategoryDB.TAG) { "setSampleDataSet()" }

        CoroutineScope(Dispatchers.Default).launch {
            categoryDao().also { dao ->
                // 전체 삭제
                dao.deleteAll()

                // 가상 데이터 입력
                val limit = Random.nextInt(0, 10)
                for (index in 1..limit) {
                    val category = createCategoryData(index)
                    DKLog.debug(TAG) { "insert sample category : $category"}
                    dao.insert(category)

                    val parentId = category.id
                    DKLog.debug(TAG) { "parentId : $parentId"}
                    val subLimit = Random.nextInt(0, 10)
                    for (index in 1..subLimit) {
                        val subCategory = createSubCategoryData(parentId, index)
                        DKLog.debug(TAG) { "insert sub category : $subCategory"}
                        dao.insert(subCategory)
                    }
                }
            }
        }
    }

    @TestOnly
    fun createCategoryData(index: Int): Category {
        DKLog.debug(TAG) { "createCategoryData()" }
        return Category(-1, "test $index")
    }

    @TestOnly
    fun createSubCategoryData(parentId: Long, index: Int): Category {
        DKLog.debug(TAG) { "createSubCategoryData()" }
        return Category(parentId, "subCategory $parentId - ${100+index}")
    }
}