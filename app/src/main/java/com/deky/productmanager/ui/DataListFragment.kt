package com.deky.productmanager.ui

import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.deky.productmanager.R
import com.deky.productmanager.database.entity.Condition
import com.deky.productmanager.database.entity.Product
import com.deky.productmanager.databinding.DatalistFragmentBinding
import com.deky.productmanager.model.DataListViewModel
import com.deky.productmanager.model.ProductsBaseViewModel
import com.deky.productmanager.util.DateUtils
import com.deky.productmanager.util.ScreenUtils
import kotlinx.android.synthetic.main.datalist_fragment.*
import kotlinx.android.synthetic.main.datalist_item.view.*


/*
* Copyright (C) 2020 Kakao corp. All rights reserved.
*
* Created by Dennis.Seo on 15/05/2020
*
*/
class DataListFragment : BaseFragment() {
    companion object {
        fun newInstance() = DataListFragment()
    }

    private lateinit var dataBinding: DatalistFragmentBinding
//    private val dataModel: DataListViewModel by lazy {
//        ViewModelProvider(this, ProductsBaseViewModel.Factory(activity!!.application)).get(DataListViewModel::class.java)
//    }
    private lateinit var dataModel: DataListViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataModel = ViewModelProvider(this, ProductsBaseViewModel.Factory(activity!!.application)).get(DataListViewModel::class.java)

        dataBinding = DataBindingUtil.inflate<DatalistFragmentBinding>(
            inflater, R.layout.datalist_fragment, container, false
        ).apply {
            lifecycleOwner = this@DataListFragment
        }

        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getProductList()
    }

    private fun getProductList() {
        dataModel.products.observe(this, Observer { products ->
            prepareRecyclerView(products)
        })
    }

    private fun prepareRecyclerView(products: List<Product>) {
        product_recycler_view.apply {
            adapter = ProductsAdapter(products)
            layoutManager =  LinearLayoutManager(context)
            addItemDecoration(ItemDecoration())
            setHasFixedSize(true)
        }
    }

    class ProductsAdapter(private val products: List<Product>) : RecyclerView.Adapter<ProductsAdapter.ProductViewHolder>() {

         override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
             return ProductViewHolder(
                     LayoutInflater.from(parent.context).inflate(R.layout.datalist_item, parent, false))
        }

        override fun getItemCount(): Int {
            return products.count()
        }

        override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
            products[position].let { product ->
                with(holder.itemView) {
                    val picture = BitmapFactory.decodeFile(product.imagePath)
                    img_picture.setImageBitmap(picture)
                    tv_location_value.text = product.location
                    tv_name_value.text = product.name
                    tv_manufacturer_value.text = product.manufacturer
                    tv_model_value.text = product.model
                    tv_size_value.text = product.size
                    tv_condition_value.text = when(product.condition){
                        Condition.NONE -> ""
                        Condition.HIGH ->
                            resources.getString(R.string.text_condition_high)
                        Condition.MIDDLE ->
                            resources.getString(R.string.text_condition_middle)
                        Condition.LOW ->
                            resources.getString(R.string.text_condition_low)
                    }
                    tv_amount_value.text = product.amount.toString()
                    tv_manufacture_date_value.text =
                        DateUtils.convertDateToString(product.manufactureDate)
                    tv_note_value.text = product.note
                }
            }
        }

        class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

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