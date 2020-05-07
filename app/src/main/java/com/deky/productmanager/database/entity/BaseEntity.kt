package com.deky.productmanager.database.entity

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

/**
 * Copyright (C) 2020 Kakao corp. All rights reserved.
 *
 */
open class BaseEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = 0
}