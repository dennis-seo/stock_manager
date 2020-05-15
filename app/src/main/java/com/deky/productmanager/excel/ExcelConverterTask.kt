package com.deky.productmanager.excel

import android.os.AsyncTask
import com.deky.productmanager.database.entity.Product
import com.deky.productmanager.excel.ColumnInfo.Companion.CELL_INDEX_AMOUNT
import com.deky.productmanager.excel.ColumnInfo.Companion.CELL_INDEX_CONDITION
import com.deky.productmanager.excel.ColumnInfo.Companion.CELL_INDEX_IMAGE
import com.deky.productmanager.excel.ColumnInfo.Companion.CELL_INDEX_LABEL
import com.deky.productmanager.excel.ColumnInfo.Companion.CELL_INDEX_LOCATION
import com.deky.productmanager.excel.ColumnInfo.Companion.CELL_INDEX_MANUFACTURER
import com.deky.productmanager.excel.ColumnInfo.Companion.CELL_INDEX_MANUFACTURE_DATE
import com.deky.productmanager.excel.ColumnInfo.Companion.CELL_INDEX_MODEL
import com.deky.productmanager.excel.ColumnInfo.Companion.CELL_INDEX_NAME
import com.deky.productmanager.excel.ColumnInfo.Companion.CELL_INDEX_NO
import com.deky.productmanager.excel.ColumnInfo.Companion.CELL_INDEX_SIZE
import com.deky.productmanager.util.DKLog
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File

/**
 * Copyright (C) 2020 Kakao corp. All rights reserved.
 *
 */
class ExcelConverterTask private constructor(
    private val productList: List<Product>,
    private val directory: File,
    private var listener: OnTaskListener?
) : AsyncTask<Void, Int, Exception>() {

    interface OnTaskListener {
        fun onStartTask()
        fun onProgressTask(progress: Int)
        fun onCompleteTask(error: Exception?)
    }

    companion object {
        private const val TAG = "ExcelConverterTask"

        // file name
        private const val EXCEL_FILE_NAME = "output.xlsx"

        // sheet name
        private const val DEFAULT_SHEET_NAME = "관리품목"

        private val columnArray = arrayOf(
            ColumnInfo(CELL_INDEX_NO, Cell.CELL_TYPE_STRING, "No"),
            ColumnInfo(CELL_INDEX_LABEL, Cell.CELL_TYPE_STRING, "라벨번호"),
            ColumnInfo(CELL_INDEX_IMAGE, Cell.CELL_TYPE_STRING, "사진"),
            ColumnInfo(CELL_INDEX_LOCATION, Cell.CELL_TYPE_STRING, "위치"),
            ColumnInfo(CELL_INDEX_NAME, Cell.CELL_TYPE_STRING, "품명"),
            ColumnInfo(CELL_INDEX_MANUFACTURER, Cell.CELL_TYPE_STRING, "제조사"),
            ColumnInfo(CELL_INDEX_MODEL, Cell.CELL_TYPE_STRING, "모델명"),
            ColumnInfo(CELL_INDEX_MANUFACTURE_DATE, Cell.CELL_TYPE_STRING, "제조일자"),
            ColumnInfo(CELL_INDEX_CONDITION, Cell.CELL_TYPE_STRING, "상태"),
            ColumnInfo(CELL_INDEX_SIZE, Cell.CELL_TYPE_STRING, "규격"),
            ColumnInfo(CELL_INDEX_AMOUNT, Cell.CELL_TYPE_STRING, "수량")
        )

        @JvmStatic
        fun convert(productList: List<Product>, directory: File, listener: OnTaskListener?): ExcelConverterTask {
            return ExcelConverterTask(productList, directory, listener).apply {
                executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
        }
    }

    override fun onPreExecute() {
        DKLog.info(TAG) { "onPreExecute()" }
        listener?.run {
            onStartTask()
        }
    }

    override fun doInBackground(vararg params: Void?): Exception? {
        DKLog.info(TAG) { "doInBackground()" }

        try {
            if (checkDirectory()) {
                val targetFile = File(directory, EXCEL_FILE_NAME).apply {
                    if (exists()) throw Exception("Exist target file : $absolutePath")
                }

                saveExcelFile(targetFile)
            }
        } catch (e: Exception) {
            DKLog.error(TAG) { "doInBackground() - error : ${e.message}" }
            return e
        }

        return null
    }

    override fun onProgressUpdate(vararg progress: Int?) {
        DKLog.info(TAG) { "onProgressUpdate() - progress : ${progress[0]}" }

        listener?.run {
            onProgressTask(progress[0] ?: 0)
        }
    }

    override fun onPostExecute(e:Exception?) {
        DKLog.info(TAG) { "onPostExecute()" }

        listener?.run {
            onCompleteTask(e)
        }
    }

    override fun onCancelled(e:Exception?) {
        DKLog.debug(TAG) { "onCancelled() - exception : ${e?.message ?: ""} " }

        super.onCancelled(e)
    }

    override fun onCancelled() {
        DKLog.debug(TAG) { "onCancelled(" }

        super.onCancelled()
    }

    @Throws(Exception::class)
    private fun checkDirectory(): Boolean {
        return directory.run {
            if (!exists() && !mkdirs()) {
                throw Exception("Failed to create directory.")
            }

            true
        }
    }

    @Throws(Exception::class)
    private fun saveExcelFile(file: File) {
        DKLog.info(TAG) { "saveExcelFile() - file : ${file.absolutePath}" }

        file.outputStream().use {
            val workBook = XSSFWorkbook()//HSSFWorkbook()
            workBook.createSheet(getNextSheetName(workBook)).let { sheet ->
                sheet.createRow(0).apply {
                    columnArray.forEach { column ->
                        createCell(column.index, column.type).apply {
                            setCellValue(column.name)
                        }
                    }
                }

                for (itemIndex in 0..productList.lastIndex) {
                    writeProductData(productList[itemIndex], sheet.createRow(itemIndex + 1))
                    publishProgress((itemIndex + 1) * 100 / productList.size)
                }
            }

            workBook.write(it)
        }
    }

    private fun getNextSheetName(workBook: Workbook) = "$DEFAULT_SHEET_NAME-${workBook.numberOfSheets + 1}"

    private fun writeProductData(product: Product, row: Row) {
        DKLog.info(TAG) { "writeProductData() - product : $product" }

        columnArray.forEach { column ->
            row.createCell(column.index, column.type).apply {
                when (column.index) {
                    CELL_INDEX_NO -> setCellValue(product.id.toString())
                    CELL_INDEX_LABEL -> setCellValue(product.label)
                    CELL_INDEX_IMAGE -> setCellValue(product.imagePath)
                    CELL_INDEX_LOCATION -> setCellValue(product.location)
                    CELL_INDEX_NAME -> setCellValue(product.name)
                    CELL_INDEX_MANUFACTURER -> setCellValue(product.manufacturer)
                    CELL_INDEX_MODEL -> setCellValue(product.model)
                    CELL_INDEX_MANUFACTURE_DATE -> setCellValue(product.manufactureDate)
                    CELL_INDEX_CONDITION -> setCellValue(product.condition.name)
                    CELL_INDEX_SIZE -> setCellValue(product.size)
                    CELL_INDEX_AMOUNT -> setCellValue(product.amount.toString())
                }
            }
        }
    }
}

data class ColumnInfo(
    val index: Int,
    val type: Int,
    val name: String
) {
    companion object {

        // cell index
        internal const val CELL_INDEX_NO = 0
        internal const val CELL_INDEX_LABEL = 1
        internal const val CELL_INDEX_IMAGE = 2
        internal const val CELL_INDEX_LOCATION = 3
        internal const val CELL_INDEX_NAME = 4
        internal const val CELL_INDEX_MANUFACTURER = 5
        internal const val CELL_INDEX_MODEL = 6
        internal const val CELL_INDEX_MANUFACTURE_DATE = 7
        internal const val CELL_INDEX_CONDITION = 8
        internal const val CELL_INDEX_SIZE = 9
        internal const val CELL_INDEX_AMOUNT = 10
    }
}