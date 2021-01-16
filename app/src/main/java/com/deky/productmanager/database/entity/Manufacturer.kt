package com.deky.productmanager.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.deky.productmanager.util.prependIndent

/*
* Copyright (C) 2021 Kakao Corp. All rights reserved.
*
* Created by Diane on 16/01/2021
*
*/

@Entity(tableName = "manufacturer")
data class Manufacturer(
    @ColumnInfo(name = "parentCategory") var parentCategory: Long = -1,
    @ColumnInfo(name = "name") var name: String = ""
) : BaseEntity() {
    override fun toString(): String {
        return buildString {
            append("Category{").append("\n")
            append("id: $id".prependIndent(1)).append("\n")
            append("ParentCategory : $parentCategory".prependIndent(1)).append("\n")
            append("Name : $name".prependIndent(1)).append("\n")
            append("}")
        }
    }
}