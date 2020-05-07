package com.deky.productmanager.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity


/*
* Copyright (C) 2020 Kakao corp. All rights reserved.
*/

@Entity(tableName = "product")
data class Product(
    @ColumnInfo(name = "label") var label: String,
    @ColumnInfo(name = "image_path") var imagePath: String,
    @ColumnInfo(name = "location") var location: String,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "manufacturer") var manufacturer: String,
    @ColumnInfo(name = "model") var model: String
) : BaseEntity()