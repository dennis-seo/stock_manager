package com.deky.productmanager.excel

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE
import android.net.Uri
import android.os.AsyncTask
import com.deky.productmanager.database.entity.Product
import com.deky.productmanager.util.DKLog
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.util.IOUtils
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.*


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

        // Row Number
        private const val ROW_NO_TITLE = 1
        private const val ROW_NO_COLUMN = 4
        private const val ROW_NO_ITEM = 5

        private val columnArray = arrayOf(
            CellInfo.CELL_NO,
            CellInfo.CELL_IMAGE_ID,
            CellInfo.CELL_LABEL,
            CellInfo.CELL_IMAGE,
            CellInfo.CELL_LOCATION,
            CellInfo.CELL_NAME,
            CellInfo.CELL_MANUFACTURER,
            CellInfo.CELL_MODEL,
            CellInfo.CELL_SIZE,
            CellInfo.CELL_MANUFACTURE_DATE,
            CellInfo.CELL_CONDITION,
            CellInfo.CELL_AMOUNT,
            CellInfo.CELL_NOTE
        )

        @JvmStatic
        fun convert(context: Context, productList: List<Product>, directory: File, listener: OnTaskListener?): ExcelConverterTask {
            return ExcelConverterTask(context, productList, directory, listener).apply {
                executeOnExecutor(THREAD_POOL_EXECUTOR)
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

        file.outputStream().use { stream ->
            val workBook = XSSFWorkbook()//HSSFWorkbook()
            workBook.createSheet(getNextSheetName(workBook)).let { sheet ->

                sheet.addMergedRegion(CellRangeAddress(1,2,0,columnArray.lastIndex))
                sheet.createRow(ROW_NO_TITLE).apply {
                    createCell(0).apply {
                        (CellInfo.createStyle(workBook, CellInfo.STYLE_TYPE_TITLE) as? XSSFCellStyle)?.let {
                            cellStyle = it
                        }

                        setCellValue("수기조사 목록")
                    }
                }

                sheet.createRow(ROW_NO_COLUMN).apply {
                    for (cellInfo in columnArray) {
                        createCell(columnArray.indexOf(cellInfo), Cell.CELL_TYPE_STRING).apply {
                            (CellInfo.createStyle(workBook, CellInfo.STYLE_TYPE_COLUMN) as? XSSFCellStyle)?.let {
                                cellStyle = it
                            }

                            setCellValue(cellInfo.name)
                        }
                    }
                }

                for (itemIndex in 0..productList.lastIndex) {
                    val rowIndex = ROW_NO_ITEM + itemIndex
                    writeProductData(workBook, productList[itemIndex], sheet.createRow(rowIndex), rowIndex)
                    publishProgress((itemIndex + 1) * 100 / productList.size)
                }
            }

            workBook.write(stream)
        }
    }

    private fun getNextSheetName(workBook: Workbook) = "$DEFAULT_SHEET_NAME-${workBook.numberOfSheets + 1}"

    private fun writeProductData(workBook: XSSFWorkbook, product: Product, row: XSSFRow, index: Int) {
        DKLog.info(TAG) { "writeProductData() - product : $product" }

        val imageFile: File? = try {
            copyImageFile(product.imagePath, product.id)
        } catch (error: Exception) {
            DKLog.warn(TAG) { "${error.message}" }
            null
        }

        val imageId = imageFile?.nameWithoutExtension ?: "-"

        columnArray.forEach { column ->
            row.createCell(columnArray.indexOf(column), column.type).apply {
                (CellInfo.createStyle(workBook, CellInfo.STYLE_TYPE_ITEM) as? XSSFCellStyle)?.let {
                    cellStyle = it
                }

                when (column) {
                    CellInfo.CELL_NO -> setCellValue(index.toDouble())
                    CellInfo.CELL_IMAGE_ID -> setCellValue(imageId)
                    CellInfo.CELL_LABEL -> setCellValue(product.label)
                    CellInfo.CELL_LOCATION -> setCellValue(product.location)
                    CellInfo.CELL_NAME -> setCellValue(product.name)
                    CellInfo.CELL_MANUFACTURER -> setCellValue(product.manufacturer)
                    CellInfo.CELL_MODEL -> setCellValue(product.model)
                    CellInfo.CELL_SIZE -> setCellValue(product.size)
                    CellInfo.CELL_MANUFACTURE_DATE -> {
                        SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                            .format(product.manufactureDate)
                            .toString().let {
                                setCellValue(it)
                            }
                    }
                    CellInfo.CELL_CONDITION -> setCellValue(product.condition.name)
                    CellInfo.CELL_AMOUNT -> setCellValue(product.amount.toString())
                    CellInfo.CELL_NOTE -> setCellValue(product.note)
                    CellInfo.CELL_IMAGE -> setCellValue("")
                }
            }
        }
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

data class CellInfo(
    val type: Int,
    val name: String
) {
    companion object {
        val CELL_NO = CellInfo(Cell.CELL_TYPE_STRING, "No")
        val CELL_IMAGE_ID = CellInfo(Cell.CELL_TYPE_STRING, "이미지 ID")
        val CELL_LABEL = CellInfo(Cell.CELL_TYPE_STRING, "라벨번호")
        val CELL_IMAGE = CellInfo(Cell.CELL_TYPE_STRING, "사진")
        val CELL_LOCATION = CellInfo(Cell.CELL_TYPE_STRING, "위치")
        val CELL_NAME = CellInfo(Cell.CELL_TYPE_STRING, "품명")
        val CELL_MANUFACTURER = CellInfo(Cell.CELL_TYPE_STRING, "제조사")
        val CELL_MODEL = CellInfo(Cell.CELL_TYPE_STRING, "모델명")
        val CELL_SIZE = CellInfo(Cell.CELL_TYPE_STRING, "규격")
        val CELL_MANUFACTURE_DATE = CellInfo(Cell.CELL_TYPE_STRING, "제조일자")
        val CELL_CONDITION = CellInfo(Cell.CELL_TYPE_STRING, "상태")
        val CELL_AMOUNT = CellInfo(Cell.CELL_TYPE_STRING, "수량")
        val CELL_NOTE = CellInfo(Cell.CELL_TYPE_STRING, "비고")

        const val STYLE_TYPE_ITEM = 0
        const val STYLE_TYPE_COLUMN = 1
        const val STYLE_TYPE_TITLE = 2

        fun createStyle(workBook: Workbook, styleType: Int): CellStyle {
            return workBook.createCellStyle().apply {

                // 정렬
                verticalAlignment = CellStyle.VERTICAL_CENTER // 세로 정렬
                alignment = CellStyle.ALIGN_CENTER // 가로 정렬

                when (styleType) {
                    STYLE_TYPE_COLUMN -> {
                        // 보더
                        borderTop = CellStyle.BORDER_MEDIUM
                        borderBottom = CellStyle.BORDER_MEDIUM
                        borderLeft = CellStyle.BORDER_MEDIUM
                        borderRight = CellStyle.BORDER_MEDIUM

                        // 색 채우기
                        fillPattern = CellStyle.SOLID_FOREGROUND // 채우기 적용
                        fillForegroundColor = IndexedColors.SEA_GREEN.index //채우기 선택

                        // 폰트
                        setFont(workBook.createFont().apply {
                            boldweight = Font.BOLDWEIGHT_BOLD
                        })
                    }

                    STYLE_TYPE_ITEM -> {
                        // 보더
                        borderTop = CellStyle.BORDER_MEDIUM
                        borderBottom = CellStyle.BORDER_MEDIUM
                        borderLeft = CellStyle.BORDER_MEDIUM
                        borderRight = CellStyle.BORDER_MEDIUM

                        // 색 채우기
                        fillPattern = CellStyle.SOLID_FOREGROUND // 채우기 적용
                        fillForegroundColor = IndexedColors.GREY_25_PERCENT.index //채우기 선택
                    }

                    STYLE_TYPE_TITLE -> {
                        // 폰트
                        setFont(workBook.createFont().apply {
                            fontHeight = 20 * 22
                            boldweight = Font.BOLDWEIGHT_BOLD
                        })
                    }
                }
            }
        }
    }
}