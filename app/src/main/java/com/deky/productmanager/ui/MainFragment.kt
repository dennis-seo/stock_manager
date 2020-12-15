package com.deky.productmanager.ui

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.deky.productmanager.R
import com.deky.productmanager.database.ProductDB
import com.deky.productmanager.databinding.MainFragmentBinding
import com.deky.productmanager.excel.ExcelConverterTask
import com.deky.productmanager.util.FileUtils
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainFragment : BaseFragment() {
    companion object {
        private const val TAG = "MainFragment"

        @JvmStatic
        fun newInstance() = MainFragment()
    }

    private lateinit var binding: MainFragmentBinding

    private var excelTask: ExcelConverterTask? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

                R.id.btn_main_category ->
                    transaction.replace(R.id.container, InputFragment.newInstance(InputFragment.DEFAULT_PRODUCT_ID))

                R.id.btn_confirm ->
                    transaction.replace(R.id.container, DataListFragment.newInstance())
            }
            transaction.addToBackStack(null).commitAllowingStateLoss()
        }
    }

    fun onClickDeleteButton() {
        showAlertDelete()
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

            context?.let { context ->
                ProductDB.getInstance(context).run {
                    SimpleDateFormat(
                        "yyyy년 MM월dd일 HH시mm분ss초",
                        Locale.getDefault()
                    ).format(System.currentTimeMillis()).let {
                        val directory = File(FileUtils.getDataDirectory(context), it)
                        log.debug { "direcroty : ${directory.absoluteFile}"}
                        excelTask = ExcelConverterTask.convert(context, productDao().getAll(), directory,
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
                                        Toast.makeText(context, "파일저장 완료", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                }
                            })
                    }
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
}
