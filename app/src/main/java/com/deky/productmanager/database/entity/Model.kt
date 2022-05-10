package com.deky.productmanager.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.deky.productmanager.util.prependIndent

/*
* Created by Dennis on 10/05/2022
*/

@Entity(tableName = "model")
data class Model(
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