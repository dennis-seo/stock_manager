package com.deky.productmanager.util

import android.content.Context
import android.os.AsyncTask
import com.deky.productmanager.database.entity.Product
import java.io.File
import java.lang.RuntimeException

/**
 * Copyright (C) 2020 Kakao corp. All rights reserved.
 *
 */
object ExcelUtils {
    private const val TAG = "ExcelUtils"

    fun convertExcelFile(directory: File, productList: List<Product>, listener: TaskListener?): ExcelConverterTask {
        return ExcelConverterTask(directory, productList, listener).apply {
            executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }
    }
}

interface TaskListener {
    fun onStartTask()
    fun onProgressTask(progress: Int)
    fun onFinishTask(e: Exception?)
}

class ExcelConverterTask(
    private val directory: File,
    private val productList: List<Product>,
    private var listener: TaskListener?
) : AsyncTask<Void, Int, Exception>() {
    companion object {
        private const val TAG = "ExcelConverterTask"
    }

    override fun onPreExecute() {
        DKLog.debug(TAG) { "onPreExecute()" }
        listener?.run {
            onStartTask()
        }
    }

    override fun doInBackground(vararg params: Void?): Exception? {
        DKLog.debug(TAG) { "doInBackground()" }

        var error: Exception? = null

        try {
            checkDirectory()

            productList.forEach { item ->

            }
        } catch (e: Exception) {
            error = e
        }

        return error
    }

    override fun onProgressUpdate(vararg progress: Int?) {
        DKLog.debug(TAG) { "onProgressUpdate() - progress : $progress" }

        listener?.run {
            onProgressTask(progress[0] ?: 0)
        }
    }

    override fun onPostExecute(e:Exception?) {
        DKLog.debug(TAG) { "onPostExecute()" }

        listener?.run {
            onFinishTask(e)
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
    private fun checkDirectory() {
        directory.run {
            if (!exists() && !mkdirs()) {
                throw Exception("Failed to create directory.")
            }
        }
    }
}