package com.deky.productmanager.ui

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.documentfile.provider.DocumentFile
import com.deky.productmanager.R
import com.deky.productmanager.database.CategoryDB
import com.deky.productmanager.database.ProductDB
import com.deky.productmanager.databinding.MainFragmentBinding
import com.deky.productmanager.excel.ExcelConverterTask
import com.deky.productmanager.util.FileUtils
import com.deky.productmanager.util.PreferenceManager
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainFragment : BaseFragment() {
    companion object {
        private const val TAG = "MainFragment"
        private const val DIRECTORY_PATTERN = "yyyy년 MM월dd일 HH시mm분ss초"
        private const val SAF_REQUEST_CODE: Int = 43

        @JvmStatic
        fun newInstance() = MainFragment()
    }

    private lateinit var binding: MainFragmentBinding

    private var excelTask: ExcelConverterTask? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<MainFragmentBinding>(
            inflater, R.layout.main_fragment, container, false
        ).apply {
            lifecycleOwner = this@MainFragment
            listener = this@MainFragment
        }

        return binding.root
    }

    fun onClickButton(view: View?) {
        fragmentManager?.let {
            val transaction = it.beginTransaction()
            when(view?.id) {
                R.id.btn_input ->
                    transaction.replace(R.id.container, InputFragment.newInstance(InputFragment.DEFAULT_PRODUCT_ID))
                R.id.btn_confirm ->
                    transaction.replace(R.id.container, DataListFragment.newInstance())
                R.id.btn_main_category ->
                    transaction.replace(R.id.container, CategoryListFragment.newInstance())
                R.id.btn_manage_manufacturer ->
                    transaction.replace(R.id.container, ManufacturerFragment.newInstance())
                R.id.btn_manage_model ->
                    transaction.replace(R.id.container, ModelFragment.newInstance())
                R.id.btn_manage_size ->
                    transaction.replace(R.id.container, ManufacturerFragment.newInstance())

            }
            transaction.addToBackStack(null).commitAllowingStateLoss()
        }
    }

    fun onClickDeleteButton() {
        showAlertDelete()
    }

    fun onClickInsertDataButton() {
        context?.let { context ->
            CategoryDB.getInstance(context).run {
                setSampleDataSet()
            }
            val count = CategoryDB.getInstance(context).categoryDao().getCount()
            log.debug { "count : $count " }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* [테스트] 사진찍고 DB 저장
        btn_picture.setOnClickListener {
            takePictureByIntent { imageFile ->
                if (imageFile.exists()) {
                    log.debug { "btn_picture.onClick() - Take image file success." }
                    ProductDB.getInstance(context!!).setSampleData(imageFile)
                } else {
                    log.debug { "btn_picture.onClick() - Not found image file." }
                }
            }
        }
        */

        btn_test.setOnClickListener {
            if (excelTask != null) return@setOnClickListener

            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                saveExcelFileAfterQ()
            } else {
                saveExcelFileBeforeQ()
            }
        }
    }

    private fun saveExcelFileBeforeQ() {

        context?.let { context ->
            val directoryName = SimpleDateFormat(DIRECTORY_PATTERN, Locale.getDefault())
                .format(System.currentTimeMillis())

            val directory = File(FileUtils.getDataDirectory(context), directoryName)
            log.debug { "direcroty : ${directory.absoluteFile}"}

            val productDao = ProductDB.getInstance(context).productDao()

            excelTask = ExcelConverterTask.convert(context, productDao.getAll(), directory,
                object : ExcelConverterTask.OnTaskListener {
                    override fun onStartTask() {
                        Toast.makeText(context, "Start", Toast.LENGTH_SHORT).show()
                        log.debug { "ExcelConverterTask.onStartTask()" }
                    }

                    override fun onProgressTask(progress: Int) {
                        Toast.makeText(context, "Progress : $progress", Toast.LENGTH_SHORT).show()
                        log.debug { "ExcelConverterTask.onProgressTask() - progress : $progress" }
                    }

                    override fun onCompleteTask(error: Exception?) {
                        log.debug { "ExcelConverterTask.onCompleteTask()" }

                        excelTask = null
                        if (error != null) {
                            log.error(true) { "Error : ${error.message}" }
                        } else {
                            Toast.makeText(context, "파일저장 완료", Toast.LENGTH_SHORT).show()
                        }

                        ExcelConverterTask.imageDir?.let { imageDir ->
                            val imageFile = File(imageDir, "-.png")
                            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.minus)
                            val fos = FileOutputStream(imageFile)
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 99, fos)
                        }
                    }
                })
        }
    }


    private fun saveExcelFileAfterQ() {
        context?.let {
            val strUri = PreferenceManager.getSaveDirectoryUri(it)

            if(strUri.isNullOrEmpty()) {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                startActivityForResult(intent, SAF_REQUEST_CODE)
            } else {
                executeExcelConverterTask(Uri.parse(strUri))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (requestCode == SAF_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val uri = resultData?.data
            //uri ex) content://com.android.externalstorage.documents/tree/primary%3A

            context?.run {
                uri?.let {
                    // 다음에 퍼미션 받지 않기 위해 저장
                    PreferenceManager.setSaveDirectoryUri(this, it.toString())
                    executeExcelConverterTask(uri)
                }
            }
        }
    }

    private fun executeExcelConverterTask(uri: Uri) {
        val takeFlags = (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

        context?.run {

            //Uri 에 대한 접근 권한허용
            contentResolver?.takePersistableUriPermission(uri, takeFlags)

            val folderDoc = DocumentFile.fromTreeUri(this, uri)
            folderDoc?.let { doc ->
                val folderName = SimpleDateFormat(DIRECTORY_PATTERN, Locale.getDefault())
                    .format(System.currentTimeMillis())

                val directory = doc.createDirectory(folderName)
                val productDao = ProductDB.getInstance(this).productDao()

                directory?.let {
                    excelTask = ExcelConverterTask.convert(this, productDao.getAll(), directory,
                        object : ExcelConverterTask.OnTaskListener {
                            override fun onStartTask() {
                                log.debug { "ExcelConverterTask.onStartTask()" }
                            }

                            override fun onProgressTask(progress: Int) {
                                log.debug { "ExcelConverterTask.onProgressTask() - progress : $progress" }
                            }

                            override fun onCompleteTask(error: Exception?) {
                                log.debug { "ExcelConverterTask.onCompleteTask()" }

                                excelTask = null
                                if (error != null) {
                                    log.error(true) { "Error : ${error.message}" }
                                } else {
                                    Toast.makeText(context, "파일저장 완료", Toast.LENGTH_SHORT).show()
                                }
                            }
                        })
                }
            }
        }
    }

    private fun showAlertDelete() {
        context?.let {
            val builder = AlertDialog.Builder(it).apply {
                setMessage(R.string.message_alert_delete_all)
                setPositiveButton(
                    R.string.btn_confirm,
                    DialogInterface.OnClickListener(function = removeButtonClick)
                )
                setNegativeButton(android.R.string.no, null)
            }
            builder.show()
        }

    }

    private val removeButtonClick = { _: DialogInterface, _: Int ->
        removeDb()
        removePictures()
    }

    private fun removeDb() {
        log.debug { "removeDB()" }

        context?.let { context ->
            ProductDB.getInstance(context).run {
                CoroutineScope(Dispatchers.Default).launch {
                    productDao().deleteAll()
                }

                Toast.makeText(context, "Database 삭제 완료", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun removePictures() {
        val picturesFile = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (picturesFile?.exists() == true) {
            picturesFile.delete()
            val childFileList = picturesFile.listFiles()
            childFileList?.forEach {
                it.delete()
            }
        }
    }
}
