package com.deky.productmanager.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


object DateUtils {
    fun convertStringToDate(strDate: String): Date? {
        val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())

        if (strDate.isEmpty())
            return null
        else {
            try {
                return simpleDateFormat.parse(strDate) ?: null
            } catch (e: ParseException) {
                return null
            }
        }
    }

    fun convertDateToString(date: Date): String {
        val pattern = "yyyy.MM.dd"
        val simpleDateFormat = SimpleDateFormat(pattern, Locale.KOREA)
        return simpleDateFormat.format(date)
    }
}