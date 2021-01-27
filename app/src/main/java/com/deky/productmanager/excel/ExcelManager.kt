package com.deky.productmanager.excel

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFFont
import org.apache.poi.xssf.usermodel.XSSFWorkbook

/**
 * Copyright (C) 2020 Kakao corp. All rights reserved.
 *
 */

private const val PER_FONT_HEIGHT = 20

object ExcelManager {
    private const val TAG = "ExcelManager"

    fun createCellStyle(workbook: XSSFWorkbook, style: Style): XSSFCellStyle {
        return when (style) {
            Style.TITLE -> createTitleStyle(workbook)
            Style.COLUMN -> createColumnStyle(workbook)
            Style.ITEM -> createItemStyle(workbook)
        }
    }

    fun measureWidth(font: XSSFFont, value: String): Int {
        return (value.length + 3) * font.fontHeight
    }

    private fun createTitleStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        return workbook.createCellStyle().apply {
            setDefaultStyle()

            setFont(workbook.createFont().apply {
                boldweight = Font.BOLDWEIGHT_BOLD
                fontHeight = (PER_FONT_HEIGHT * 22).toShort()
            })
        }
    }

    private fun createColumnStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        return workbook.createCellStyle().apply {
            setDefaultStyle()
            setBorder(CellStyle.BORDER_MEDIUM)
            setForegroundColor(IndexedColors.SEA_GREEN)

            setFont(workbook.createFont().apply {
                boldweight = Font.BOLDWEIGHT_BOLD
            })
        }
    }

    private fun createItemStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        return workbook.createCellStyle().apply {
            setDefaultStyle()
            setBorder(CellStyle.BORDER_MEDIUM)
            setForegroundColor(IndexedColors.GREY_25_PERCENT)
        }
    }

    private fun XSSFCellStyle.setDefaultStyle() {
        // 정렬
        alignment = CellStyle.ALIGN_CENTER
        verticalAlignment = CellStyle.VERTICAL_CENTER
    }

    private fun XSSFCellStyle.setBorder(border: Short) {
        borderTop = border
        borderBottom = border
        borderLeft = border
        borderRight = border
    }

    private fun XSSFCellStyle.setForegroundColor(color: IndexedColors) {
        fillPattern = CellStyle.SOLID_FOREGROUND
        fillForegroundColor = color.index
    }
}

enum class Column(val type: Int, val title: String) {
    NO(Cell.CELL_TYPE_STRING, "No"),
//    LABEL(Cell.CELL_TYPE_STRING, "라벨번호"),
    IMAGE_ID(Cell.CELL_TYPE_STRING, "이미지 ID"),
    IMAGE(Cell.CELL_TYPE_STRING, "사진"),
    LOCATION(Cell.CELL_TYPE_STRING, "위치"),
    PRODUCT_NAME(Cell.CELL_TYPE_STRING, "품명"),
    MANUFACTURER(Cell.CELL_TYPE_STRING, "제조사"),
    MODEL(Cell.CELL_TYPE_STRING, "모델명"),
    SIZE(Cell.CELL_TYPE_STRING, "규격"),
    MANUFACTURE_DATE(Cell.CELL_TYPE_STRING, "제조일자"),
    CONDITION(Cell.CELL_TYPE_STRING, "상태"),
    AMOUNT(Cell.CELL_TYPE_STRING, "수량"),
    NOTE(Cell.CELL_TYPE_STRING, "비고")
}

enum class Row(val index: Int) {
    TITLE(1),
    COLUMN(4),
    ITEM(5)
}

enum class Style {
    TITLE, COLUMN, ITEM
}