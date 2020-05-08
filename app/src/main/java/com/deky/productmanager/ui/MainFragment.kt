package com.deky.productmanager.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.deky.productmanager.R
import com.deky.productmanager.databinding.MainFragmentBinding
import com.deky.productmanager.model.MainViewModel
import com.deky.productmanager.util.DKLog
import kotlinx.android.synthetic.main.main_fragment.*

class MainFragment : BaseFragment() {
    companion object {
        private const val TAG = "MainFragment"

        @JvmStatic
        fun newInstance() = MainFragment()
    }

    private lateinit var dataBinding: MainFragmentBinding

    private val viewModel: MainViewModel by lazy {
        ViewModelProviders.of(this@MainFragment)[MainViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        dataBinding = DataBindingUtil.inflate<MainFragmentBinding>(
            inflater, R.layout.main_fragment, container, false).apply {
            lifecycleOwner = this@MainFragment
            listener = this@MainFragment
            model = viewModel
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_picture.setOnClickListener {
            takePictureByIntent { imageFile ->
                if (imageFile.exists()) {
                    DKLog.v(TAG, "btn_picture.onClick() - Take image file success.")
                } else {
                    DKLog.e(TAG, "btn_picture.onClick() - Not found image file.")
                }
            }
        }
    }
}
