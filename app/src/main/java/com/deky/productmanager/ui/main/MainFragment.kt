package com.deky.productmanager.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.deky.productmanager.R
import com.deky.productmanager.databinding.MainFragmentBinding

class MainFragment : Fragment() {
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
}
