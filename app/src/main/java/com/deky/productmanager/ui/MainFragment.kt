package com.deky.productmanager.ui

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.deky.productmanager.R
import com.deky.productmanager.database.ProductDB
import com.deky.productmanager.databinding.MainFragmentBinding
import com.deky.productmanager.excel.ExcelConverterTask
import com.deky.productmanager.util.FileUtils
import kotlinx.android.synthetic.main.main_fragment.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainFragment : BaseFragment() {
    companion object {
        private const val TAG = "MainFragment"

        @JvmStatic
        fun newInstance() = MainFragment()
    }

    private lateinit var dataBinding: MainFragmentBinding

//    private val viewModel: InputViewModel by lazy {
//        ViewModelProviders.of(this@MainFragment)[InputViewModel::class.java]
//    }
    private var excelTask: ExcelConverterTask? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        dataBinding = DataBindingUtil.inflate<MainFragmentBinding>(
            inflater, R.layout.main_fragment, container, false).apply {
            lifecycleOwner = this@MainFragment
            listener = this@MainFragment
//            model = viewModel
        }

        return dataBinding.root
    }

    fun onClickButton(view: View?) {
        val transaction = parentFragmentManager.beginTransaction()
        when(view?.id) {
            R.id.btn_input ->
                transaction.replace(R.id.container, InputFragment.newInstance())

            R.id.btn_modify ->
                transaction.replace(R.id.container, InputFragment.newInstance())

            R.id.btn_confirm ->
                transaction.replace(R.id.container, InputFragment.newInstance())
        }
        transaction.addToBackStack(null).commitAllowingStateLoss()
    }

    fun onClickDeleteButton(view: View?) {
        showAlertDelete()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_picture.setOnClickListener {
            takePictureByIntent { imageFile ->
                if (imageFile.exists()) {
                    log.debug { "btn_picture.onClick() - Take image file success." }
                } else {
                    log.debug { "btn_picture.onClick() - Not found image file." }
                }
            }
        }

        btn_test.setOnClickListener {
            if (excelTask != null) return@setOnClickListener

            context?.let { context ->
                ProductDB.getInstance(context).run {
                    setSampleData()

                    SimpleDateFormat("yyyy년 MM월dd일 HH시mm분ss초", Locale.getDefault()).format(System.currentTimeMillis()).let {
                        val directory = File(FileUtils.getDataDirectory(context), it)
                        excelTask = ExcelConverterTask.convert(productDao().getAll(), directory, object: ExcelConverterTask.OnTaskListener{
                            override fun onStartTask() {
                                log.debug { "ExcelConverterTask.onStartTask()" }
                            }

                            override fun onProgressTask(progress: Int) {
                                log.debug { "ExcelConverterTask.onProgressTask() - progress : $progress" }
                            }

                            override fun onCompleteTask(e: Exception?) {
                                log.debug { "ExcelConverterTask.onCompleteTask()" }
                                e?.let {
                                    log.error(true) { "Error : ${e.message}" }
                                }

                                excelTask = null
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
                setMessage(R.string.message_alert_delete_data)
                setPositiveButton(R.string.btn_confirm, DialogInterface.OnClickListener(function = removeButtonClick))
                setNegativeButton(android.R.string.no, null)
            }
            builder.show()
        }

    }

    private val removeButtonClick = { dialog: DialogInterface, which: Int ->
        removeDb()
    }

    private fun removeDb() {
        log.debug { "removeDB()" }
    }
}
