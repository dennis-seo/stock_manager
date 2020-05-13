package com.deky.productmanager.util

import java.text.SimpleDateFormat
import java.util.*


object DateUtils {
    fun convertStringToDate(strDate: String): Date {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        if(strDate.isEmpty())
            return Date(0)
        else
            return simpleDateFormat.parse(strDate)?: Date(0)
    }
}