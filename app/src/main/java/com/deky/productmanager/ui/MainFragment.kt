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
import com.deky.productmanager.util.Logger
import kotlinx.android.synthetic.main.main_fragment.*

class MainFragment : BaseFragment() {
    companion object {
        private const val TAG = "MainFragment"

        @JvmStatic
        fun newInstance() = MainFragment()
    }

    private val viewModel: MainViewModel by lazy {
        ViewModelProviders.of(this)[MainViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false).apply {
            DataBindingUtil.bind<MainFragmentBinding>(this)?.apply {
                lifecycleOwner = this@MainFragment
                model = viewModel
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_picture.setOnClickListener {
            takePictureByIntent { imageFile ->
                if (imageFile.exists()) {
                    Logger.v(TAG, "btn_picture.onClick() - Take image file success.")
                } else {
                    Logger.e(TAG, "btn_picture.onClick() - Not found image file.")
                }
            }
        }
    }
}
