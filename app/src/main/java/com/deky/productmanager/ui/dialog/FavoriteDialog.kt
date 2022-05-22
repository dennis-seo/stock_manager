package com.deky.productmanager.ui.dialog

import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.deky.productmanager.R
import com.deky.productmanager.database.entity.Product
import com.deky.productmanager.databinding.DatalistFragmentBinding
import com.deky.productmanager.model.BaseViewModel
import com.deky.productmanager.model.DataListViewModel
import com.deky.productmanager.ui.DataListFragment
import com.deky.productmanager.util.ScreenUtils
import com.deky.productmanager.util.afterTextChanged
import kotlinx.android.synthetic.main.datalist_fragment.*
import kotlinx.android.synthetic.main.datalist_pager_recylerview_layout.view.*


/*
* Copyright (C) 2022 Kakao corp. All rights reserved.
*
* Created by Jeffrey.bbongs on 2022/05/22
*
*/


/**
 * Call this method (in onActivityCreated or later)
 * to make the dialog near-full screen.
 */
fun DialogFragment.setScreenSize(perWidth: Int, perHeight: Int) {
    val perW = perWidth.toFloat() / 100
    val perH = perHeight.toFloat() / 100
    val dm = Resources.getSystem().displayMetrics
    val percentWidth = dm.widthPixels * perW
    val percentHeight = dm.heightPixels * perH
    Log.d("bbong", "width : $percentWidth    height : $percentHeight")
    dialog?.window?.setLayout(percentWidth.toInt(), percentHeight.toInt())
}

class FavoriteDialog : DialogFragment() {
    private lateinit var dataBinding: DatalistFragmentBinding
    private lateinit var dataModel: DataListViewModel
    private lateinit var onItemClickListener: OnFavoriteDialogClickListener

    private val viewPagerAdapter by lazy {
        DataListPagerAdapter()
    }

    interface OnFavoriteDialogClickListener {
        fun onItemClick(product: Product)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dataModel = ViewModelProvider(this, BaseViewModel.Factory(requireActivity().application)).get(
            DataListViewModel::class.java
        )

        dataBinding = DataBindingUtil.inflate<DatalistFragmentBinding>(inflater, R.layout.datalist_fragment, container, false).apply {
            lifecycleOwner = this@FavoriteDialog
        }
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addObserve()

        ed_search_keyword.afterTextChanged { dataModel.keyword.postValue(it) }
    }

    fun setOnItemClickListener(listener: OnFavoriteDialogClickListener) {
        onItemClickListener = listener
    }

    private fun addObserve() {

        dataModel.products.observe(viewLifecycleOwner, Observer { products ->
            datalist_viewpager.adapter = viewPagerAdapter
            viewPagerAdapter.notifyDataSetChanged()
        })

        dataModel.keyword.observe(viewLifecycleOwner, Observer { keyword ->
            if(keyword.isNullOrBlank()) {
                dataModel.getAllProduct()
            } else {
                dataModel.findProduct(keyword)
            }
        })
    }

    inner class DataListPagerAdapter() : PagerAdapter() {
        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun getCount(): Int {
            val size = dataModel.products.value?.size ?: 0
            return (size / 20) + 1
        }

        override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
            if (any is View) container.removeView(any)
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val inflater: LayoutInflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.datalist_pager_recylerview_layout, container, false)
            val products = dataModel.products.value
            val size = dataModel.products.value?.size ?: 0
            val pageCnt = (size / 20) + 1
            view.index_tv.text = "${position + 1}/${pageCnt}"
            val startPosition = position.times(20)
            var endPosition = (position + 1).times(20)
            if (endPosition > products?.size ?: 0) endPosition = products?.size ?: 0
            if (endPosition <= 0) endPosition = 0
            val productsAdapter = DataListFragment.ProductsAdapter(
                    products?.subList(startPosition, endPosition) ?: ArrayList()
                )
            view.product_recycler_view.apply {
                adapter = productsAdapter
                productsAdapter.onItemClick = { product ->
                    onItemClickListener.onItemClick(product)
                }
                productsAdapter.onItemLongClick = { product ->
                    showAlertDelete(product)
                }
                productsAdapter.notifyDataSetChanged()
                layoutManager = LinearLayoutManager(context)
                addItemDecoration(ItemDecoration())
                setHasFixedSize(true)
            }
            container.addView(view)
            return view
        }
    }

    private fun showAlertDelete(product: Product) {
        context?.let {
            val builder = AlertDialog.Builder(it).apply {
                setMessage(R.string.message_alert_delete_data)
                setPositiveButton(
                    R.string.btn_confirm,
                    DialogInterface.OnClickListener { _, _ ->
                        dataModel.delete(product)
                    }
                )
                setNegativeButton(android.R.string.no, null)
            }
            builder.show()
        }
    }

    inner class ItemDecoration : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            outRect.top = ScreenUtils.dipToPixel(context, 5f)
            outRect.bottom = ScreenUtils.dipToPixel(context, 5f)
            outRect.left = ScreenUtils.dipToPixel(context, 5f)
            outRect.right = ScreenUtils.dipToPixel(context, 5f)

            if (parent.getChildAdapterPosition(view) == parent.adapter!!.itemCount - 1) {
                outRect.bottom = ScreenUtils.dipToPixel(context, 7.5f)
            } else if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = ScreenUtils.dipToPixel(context, 7.5f)
            }
        }
    }

}