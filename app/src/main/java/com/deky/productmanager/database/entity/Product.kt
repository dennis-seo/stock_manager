package com.deky.productmanager.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.TypeConverter
import com.deky.productmanager.util.prependIndent
import java.util.*


/*
* Copyright (C) 2020 Kakao corp. All rights reserved.
*/

val DEFAULT_DATE = Date(0)
val DEFAULT_SIZE = "0x0x0"

@Entity(tableName = "product")
data class Product(
    @ColumnInfo(name = "label") var label: String = "",
    @ColumnInfo(name = "image_path") var imagePath: String = "",
    @ColumnInfo(name = "location") var location: String = "",
    @ColumnInfo(name = "name") var name: String = "",
    @ColumnInfo(name = "manufacturer") var manufacturer: String = "",
    @ColumnInfo(name = "manufacture_date") var manufactureDate: String = "",
    @Deprecated(message = "The property will be removed")
    @ColumnInfo(name = "manufacture_date_x") var manufactureDateX: Date = DEFAULT_DATE,
    @ColumnInfo(name = "condition") var condition: Condition = Condition.NONE,
    @ColumnInfo(name = "size") var size: String = DEFAULT_SIZE,
    @ColumnInfo(name = "model") var model: String = "",
    @ColumnInfo(name = "amount") var amount: Int = 1,
    @ColumnInfo(name = "note") var note: String = "",
    @ColumnInfo(name = "favorite") var favorite: Boolean = false
) : BaseEntity() {

    companion object Factory {
        fun create(product: Product): Product {
            return Product(product.label, product.imagePath, product.location, product.name, product.manufacturer,
            product.manufactureDate, DEFAULT_DATE, product.condition, product.size, product.model,
            product.amount, product.note, product.favorite)
        }
    }
    override fun toString(): String {
        return buildString {
            append("Product {").append("\n")
            append("label : $label".prependIndent(1)).append("\n")
            append("imagePath : $imagePath".prependIndent(1)).append("\n")
            append("location : $location".prependIndent(1)).append("\n")
            append("name : $name".prependIndent(1)).append("\n")
            append("manufacturer : $manufacturer".prependIndent(1)).append("\n")
            append("manufactureDate : $manufactureDate".prependIndent(1)).append("\n")
            append("manufactureDateX : $manufactureDateX".prependIndent(1)).append("\n")
            append("condition : $condition".prependIndent(1)).append("\n")
            append("size : $size".prependIndent(1)).append("\n")
            append("model : $model".prependIndent(1)).append("\n")
            append("amount : $amount".prependIndent(1)).append("\n")
            append("note : $note".prependIndent(1)).append("\n")
            append("favorite : $favorite".prependIndent(1)).append("\n")
            append("}")
        }
    }
}

enum class Condition(val level: Int) {
    NONE(0),
    HIGH(1),
    MIDDLE(2),
    LOW(3);

    companion object {
        fun getCondition(level: Int): Condition {
            values().forEach {
                if (it.level == level) return it
            }

            return NONE
        }
    }
}

class TimeConverter {

    @TypeConverter
    fun toDate(value: Long): Date {
        return Date(value)
    }

    @TypeConverter
    fun fromDate(date: Date): Long {
        return date.time
    }
}

class ConditionConverter {

    @TypeConverter
    fun toCondition(level: Int): Condition {
        return Condition.getCondition(level)
    }

    @TypeConverter
    fun fromCondition(condition: Condition): Int {
        return condition.level
    }
}