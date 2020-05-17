package com.deky.productmanager.excel

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE
import android.net.Uri
import android.os.AsyncTask
import com.deky.productmanager.database.entity.Product
import com.deky.productmanager.util.DKLog
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.util.IOUtils
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min


/**
 * Copyright (C) 2020 Kakao corp. All rights reserved.
 *
 */
class ExcelConverterTask private constructor(
    private val context: Context,
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
        private const val EXCEL_FILE_NAME = "수기조사서.xlsx"

        // sheet name
        private const val DEFAULT_SHEET_NAME = "관리품목"

        @JvmStatic
        fun convert(context: Context, productList: List<Product>, directory: File, listener: OnTaskListener?): ExcelConverterTask {
            return ExcelConverterTask(context, productList, directory, listener).apply {
                executeOnExecutor(THREAD_POOL_EXECUTOR)
            }
        }
    }

    private val workBook by lazy {
        XSSFWorkbook()
    }

    private val columnResizeMap by lazy {
        mutableMapOf<Column, Int>()
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

                context.sendBroadcast(Intent(ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                    data = Uri.fromFile(targetFile)
                })
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

        listener?.run {
            onCompleteTask(e)
        }
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

        createSheet().let { sheet ->
            // 타이틀 셀 병합
            sheet.addMergedRegion(CellRangeAddress(1, 2, 0, Column.values().lastIndex))

            // 타이틀 row
            sheet.createRow(Row.TITLE.index).let { row ->
                row.createCell(0, Cell.CELL_TYPE_STRING).apply {
                    cellStyle = ExcelManager.createCellStyle(workBook, Style.TITLE)
                    setCellValue("수기조사 목록")
                }
            }

            // 컬럼 row
            sheet.createRow(Row.COLUMN.index).let { row ->
                Column.values().forEach { column ->
                    row.createCell(column.ordinal, column.type).apply {
                        cellStyle = ExcelManager.createCellStyle(workBook, Style.COLUMN)
                        setCellValue(column.title)
                    }
                }
            }

            // 아이템 row
            for (itemIndex in 0..productList.lastIndex) {
                val rowIndex = Row.ITEM.index + itemIndex
                writeProductData(productList[itemIndex], sheet.createRow(rowIndex), itemIndex + 1)
                publishProgress((itemIndex + 1) * 100 / productList.size)
            }
        }

        file.outputStream().use { stream ->
            workBook.write(stream)
        }
    }

    private fun createSheet(sheetName: String = nextSheetName()): XSSFSheet {
        return workBook.createSheet(sheetName)
    }

    private fun nextSheetName(): String = "$DEFAULT_SHEET_NAME-${workBook.numberOfSheets + 1}"

    private fun writeProductData(product: Product, row: XSSFRow, itemIndex: Int) {
        DKLog.info(TAG) { "writeProductData() - product : $product" }

        val imageFile: File? = try {
            copyImageFile(product.imagePath, product.id)
        } catch (error: Exception) {
            DKLog.warn(TAG) { "${error.message}" }
            null
        }

        val sheet = row.sheet
        Column.values().forEach { column ->
            row.createCell(column.ordinal, column.type).apply {
                cellStyle = ExcelManager.createCellStyle(workBook, Style.ITEM)

                when (column) {
                    Column.NO -> setValueWithResize(sheet, column, itemIndex.toString())
                    Column.LABEL -> setValueWithResize(sheet, column, product.label)
                    Column.IMAGE_ID -> setValueWithResize(sheet, column, getImageIdFromFile(imageFile))
                    Column.IMAGE -> setValueWithResize(sheet, column, "")
                    Column.LOCATION -> setValueWithResize(sheet, column, product.location)
                    Column.PRODUCT_NAME -> setValueWithResize(sheet, column, product.name)
                    Column.MANUFACTURER -> setValueWithResize(sheet, column, product.manufacturer)
                    Column.MODEL -> setValueWithResize(sheet, column, product.model)
                    Column.SIZE -> setValueWithResize(sheet, column, product.size)
                    Column.MANUFACTURE_DATE -> setValueWithResize(sheet, column, parseManufactureDate(product.manufactureDate))
                    Column.CONDITION -> setValueWithResize(sheet, column, product.condition.name)
                    Column.AMOUNT -> setValueWithResize(sheet, column, product.amount.toString())
                    Column.NOTE -> setValueWithResize(sheet, column, product.note)
                }
            }
        }
    }

    private fun XSSFCell.setValueWithResize(sheet: XSSFSheet, column: Column, value: String) {

        if (!columnResizeMap.containsKey(column)) {
            columnResizeMap[column] = sheet.getColumnWidth(column.ordinal)
        }

        columnResizeMap[column]?.let { columnWidth ->
            min(255 * 256, ExcelManager.measureWidth(cellStyle.font, value)).let {
                if (it > columnWidth) {
                    sheet.setColumnWidth(column.ordinal, it)
                    columnResizeMap[column] = it

                    DKLog.debug(TAG) { "setValueWithResize() - ${column.name} : $columnWidth >> $it" }
                }
            }
        }

        setCellValue(value)
    }

    private fun getImageIdFromFile(imageFile: File?): String = imageFile?.nameWithoutExtension ?: "-"

    private fun parseManufactureDate(manufactureDate: Date): String {
        return SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
            .format(manufactureDate)
            .toString()
    }

    @Throws(Exception::class)
    private fun copyImageFile(imagePath: String, id: Long): File {
        File(imagePath).let { imageFile ->
            imageFile.takeUnless { it.exists() }?.run {
                throw Exception("Not found image file.")
            }

            val imageDirectory = File(directory, "사진").apply {
                takeUnless { exists() }?. run {
                    if (!mkdirs()) throw Exception("Failed to create image directory.")
                }
            }

            return File(imageDirectory, "${id}.${imageFile.extension}").let { target ->
                imageFile.copyTo(target, true)
            }
        }
    }

    @Throws(Exception::class)
    private fun setResizedImage(imagePath: String, row: XSSFRow) {
        DKLog.info(TAG) { "setResizedImage() - imagePath : $imagePath" }

        FileInputStream(File(imagePath)).use {
            val sheet = row.sheet ?: throw Exception("IllegalState : sheet is null")
            val workbook = sheet.workbook ?: throw Exception("IllegalState : workbook is null")

            val bytes = IOUtils.toByteArray(it) ?: throw Exception("Failed to read a image file.")
            val pictureId = workbook.addPicture(bytes, XSSFWorkbook.PICTURE_TYPE_PNG)
            val helper = workbook.creationHelper ?: throw Exception("IllegalState : creationHelper is null")

            val anchor = helper.createClientAnchor()?.apply {
                row1 = row.rowNum
            } ?: throw Exception("IllegalState : anchor is null")

            sheet.createDrawingPatriarch()?.run {
                createPicture(anchor, pictureId)?.run {
//                    resize()
                } ?: throw Exception("Failed to create picture.")
            } ?: throw Exception("IllegalState : drawingPatriarch is null")
        }
    }
}