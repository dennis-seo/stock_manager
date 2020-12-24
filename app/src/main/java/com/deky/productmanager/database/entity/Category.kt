package com.deky.productmanager.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.TypeConverter
import com.deky.productmanager.util.prependIndent
import java.util.*


/*
* Copyright (C) 2020 Kakao corp. All rights reserved.
*/


@Entity(tableName = "category")
data class Category(
    @ColumnInfo(name = "parentCategory") var parentCategory: Long = -1,
    @ColumnInfo(name = "name") var name: String = ""
) : BaseEntity() {
    override fun toString(): String {
        return buildString {
            append("Category {").append("\n")
            append("id : $id".prependIndent(1)).append("\n")
            append("ParentCategory : $parentCategory".prependIndent(1)).append("\n")
            append("Name : $name".prependIndent(1)).append("\n")
            append("}")
        }
    }
}
