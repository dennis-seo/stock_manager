package com.deky.productmanager.excel

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE
import android.net.Uri
import android.os.AsyncTask
import com.deky.productmanager.R
import android.os.Build
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import androidx.documentfile.provider.DocumentFile
import com.deky.productmanager.database.entity.Condition
import com.deky.productmanager.database.entity.Product
import com.deky.productmanager.util.DKLog
import com.deky.productmanager.util.PreferenceManager
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.util.IOUtils
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
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
    private val directory: Any,        // File or DocumentFile
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
        // picture directory name
        private const val PICTURE_DIRECTORY_NAME = "사진"

        // sheet name
        private const val DEFAULT_SHEET_NAME = "관리품목"

        var imageDir: File? = null

        @JvmStatic
        fun convert(
            context: Context,
            productList: List<Product>,
            folder: Any,
            listener: OnTaskListener?
        ): ExcelConverterTask {
            return ExcelConverterTask(context, productList, folder, listener).apply {
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
        DKLog.info(TAG) { "doInBackground() : ${Build.VERSION.SDK_INT}" }

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            // directory is DocumentFile
            saveExcelFileAfterQ()
        } else {
            // directory is File
            saveExcelFileBeforeQ()
        }

        return null
    }

    override fun onProgressUpdate(vararg progress: Int?) {
        DKLog.info(TAG) { "onProgressUpdate() - progress : ${progress[0]}" }

        listener?.run {
            onProgressTask(progress[0] ?: 0)
        }
    }

    override fun onPostExecute(e: Exception?) {
        DKLog.info(TAG) { "onPostExecute()" }

        listener?.run {
            onCompleteTask(e)
        }
    }

    override fun onCancelled(e: Exception?) {
        DKLog.debug(TAG) { "onCancelled() - exception : ${e?.message ?: ""} " }

        listener?.run {
            onCompleteTask(e)
        }
    }

    @Throws(Exception::class)
    private fun checkDirectory(): Boolean {
        return (directory as File).run {
            if (!exists() && !mkdirs()) {
                throw Exception("Failed to create directory.")
            }

            true
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveExcelFileAfterQ() {

        val file = (directory as DocumentFile).createFile("*/*", EXCEL_FILE_NAME)
        file?.let { file ->
            context.contentResolver.openOutputStream(file.uri)?.let { outputStream ->
                saveExcelFile(outputStream)
            }
        }
//
//        val savedFileUri = resolver.insert(saveExcelFile, fileContent)
//        DKLog.info(TAG) { "saveExcelFileAfterQ() > saveFileUri : $savedFileUri" }
//
//        // savedFileUri 파일 쓰기
//        savedFileUri?.let {
//            //  savedFileUri 위치에 파일을 생성해준다.
//            val pdf = resolver.openFileDescriptor(it, "w", null)
//
//            pdf?.let {
//                val fileOutputStream = FileOutputStream(pdf.fileDescriptor)
//                saveExcelFile(fileOutputStream)
//            }
//        }
//
//        fileContent.put(MediaStore.Downloads.IS_PENDING, 0)
//        resolver.update(saveExcelFile, fileContent, null, null)
    }

    private fun saveExcelFileBeforeQ() {
        try {
            if (checkDirectory()) {
                val targetFile = File((directory as File), EXCEL_FILE_NAME).apply {
                    if (exists()) throw Exception("Exist target file : $absolutePath")
                }

                DKLog.info(TAG) { "saveExcelFileBeforeQ() > saveFileUri : $targetFile" }
                saveExcelFile(targetFile.outputStream())

                context.sendBroadcast(Intent(ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                    data = Uri.fromFile(targetFile)
                })
            }
        } catch (e: Exception) {
            DKLog.error(TAG) { "doInBackground() - error : ${e.message}" }
        }
    }

    @Throws(Exception::class)
    private fun saveExcelFile(outputStream: OutputStream) {

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

        outputStream.use { stream ->
            workBook.write(stream)
        }
    }

    private fun createSheet(sheetName: String = nextSheetName()): XSSFSheet {
        return workBook.createSheet(sheetName)
    }

    private fun nextSheetName(): String = "$DEFAULT_SHEET_NAME-${workBook.numberOfSheets + 1}"

    private fun writeProductData(product: Product, row: XSSFRow, itemIndex: Int) {
        DKLog.info(TAG) { "writeProductData() - product : $product" }
        val imageName = if (PreferenceManager.isImageTagAvailability(context)) (PreferenceManager.getImageTagName(context) + product.id) else product.id.toString()

        val imageFile: Any? = try {
            DKLog.info(TAG) { "image path : ${product.imagePath}"}
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                copyImageFileAfterQ(product.imagePath, imageName)
            } else {
                copyImageFileBeforeQ(product.imagePath, imageName)
            }
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
                    Column.MANUFACTURE_DATE -> setValueWithResize(sheet, column, product.manufactureDate)
                    Column.CONDITION -> setValueWithResize(sheet, column, parseCondition(product.condition))
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

    private fun getImageIdFromFile(imageFile: Any?): String {
        return when (imageFile) {
            is File -> {
                imageFile.nameWithoutExtension
            }
            is DocumentFile -> {
                imageFile.name?.substringBeforeLast(".") ?: "-"
            }
            else -> {
                "-"
            }
        }
    }

    private fun parseManufactureDate(manufactureDate: Date): String {
        return SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
            .format(manufactureDate)
            .toString()
    }

    private fun parseCondition(condition: Condition): String {
        return when (condition) {
            Condition.HIGH -> "상"
            Condition.MIDDLE -> "중"
            Condition.LOW -> "하"
            Condition.NONE -> ""
        }
    }

    @Throws(Exception::class)
    private fun copyImageFileBeforeQ(imagePath: String, id: String): File {
        File(imagePath).let { imageFile ->
            imageFile.takeUnless { it.exists() }?.run {
                throw Exception("Not found image file.")
            }

            val imageDirectory = File((directory as File), "사진").apply {
                takeUnless { exists() }?. run {
                    if (!mkdirs()) throw Exception("Failed to create image directory.")
                }
            }

            imageDir = imageDirectory

            return File(imageDirectory, "${id}.${imageFile.extension}").let { target ->
                imageFile.copyTo(target, true)
            }
        }
    }

    @Throws(Exception::class)
    private fun copyImageFileAfterQ(imagePath: String, id: String): DocumentFile {
        File(imagePath).let { sourceFile ->
            sourceFile.takeUnless { it.exists() }?.run {
                throw Exception("Not found image file.")
            }

            val mainFolder = directory as DocumentFile
//            val files = mainFolder.listFiles()

            // 기존 폴더가 있다면 해당 폴더, 없다면 폴더 생성, 실패시 익셉션
            val pictureFolder: DocumentFile = mainFolder.findFile(PICTURE_DIRECTORY_NAME)
                ?: mainFolder.createDirectory(PICTURE_DIRECTORY_NAME)
                ?: throw Exception("Failed to create image directory.")

            val inStream = FileInputStream(sourceFile)
            val inChannel = inStream.channel

            val mimeType = getMimeType(imagePath)
            val targetFile = pictureFolder.createFile(mimeType, id.toString())
            val outStream = targetFile?.let { targetFile ->
                context.contentResolver.openOutputStream(targetFile.uri)
            }
            val outChannel = (outStream as FileOutputStream).channel
            inChannel.transferTo(0, inChannel.size(), outChannel)
            outChannel.close()
            outStream.close()
            inChannel.close()
            inStream.close()

            return targetFile
        }
    }

    private fun getMimeType(url: String?): String {
        return MimeTypeMap.getFileExtensionFromUrl(url).let {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(it)?: "error"
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