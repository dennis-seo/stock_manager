package com.deky.productmanager.ui

import android.content.DialogInterface
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.deky.productmanager.R
import com.deky.productmanager.database.entity.Condition
import com.deky.productmanager.database.entity.DEFAULT_DATE
import com.deky.productmanager.database.entity.Product
import com.deky.productmanager.databinding.DatalistFragmentBinding
import com.deky.productmanager.model.DataListViewModel
import com.deky.productmanager.model.BaseViewModel
import com.deky.productmanager.util.DateUtils
import com.deky.productmanager.util.ScreenUtils
import kotlinx.android.synthetic.main.datalist_fragment.*
import kotlinx.android.synthetic.main.datalist_item.view.*
import java.io.File


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

    private lateinit var dataModel: DataListViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataModel = ViewModelProvider(this, BaseViewModel.Factory(activity!!.application)).get(DataListViewModel::class.java)

        dataBinding = DataBindingUtil.inflate<DatalistFragmentBinding>(
            inflater, R.layout.datalist_fragment, container, false
        ).apply {
            lifecycleOwner = this@DataListFragment
        }

        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        log.debug { "onViewCreated()" }
        getProductList()
    }

    private fun getProductList() {
        log.debug { "getProductList()" }

        dataModel.products.observe(this, Observer { products ->
            log.debug { "getProductList.onChanged()" }
            prepareRecyclerView(products)
        })
    }

    private fun prepareRecyclerView(products: List<Product>) {
        log.debug { "prepareRecyclerView()" }

        product_recycler_view.apply {
            val productsAdapter = ProductsAdapter(products)
            productsAdapter.onItemClick = { product ->
                fragmentManager?.let {
                    val transaction = it.beginTransaction()
                    transaction.replace(R.id.container, InputFragment.newInstance(product.id))
                    transaction.addToBackStack(null).commitAllowingStateLoss()
                }
            }
            productsAdapter.onItemLongClick = { product ->
                showAlertDelete(product)
            }

            adapter = productsAdapter
            layoutManager =  LinearLayoutManager(context)
            addItemDecoration(ItemDecoration())
            setHasFixedSize(true)
        }
    }

    private fun showAlertDelete(product: Product) {
        log.debug { "showAlertDelete()" }

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

    class ProductsAdapter(private val products: List<Product>) : RecyclerView.Adapter<ProductsAdapter.ProductViewHolder>() {

        var onItemClick: ((Product) -> Unit)? = null
        var onItemLongClick: ((Product) -> Unit)? = null

         override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
             return ProductViewHolder(
                     LayoutInflater.from(parent.context).inflate(R.layout.datalist_item, parent, false))
        }

        override fun getItemCount(): Int {
            return products.count()
        }

        override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
            products[position].let { product ->
                holder.initialize()

                with(holder.itemView) {
                    File(product.imagePath).takeIf { it.exists() }?.let { imageFile ->
                        img_picture.load(imageFile)
                    }

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
                    val strDate = if(product.manufactureDateX == DEFAULT_DATE) ""
                                            else DateUtils.convertDateToString(product.manufactureDateX)
                    tv_manufacture_date_value.text = strDate
                    tv_note_value.text = product.note
                }
            }
        }

        inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            init {
                initialize()

                itemView.setOnClickListener {
                    onItemClick?.invoke(products[adapterPosition])
                }
                itemView.setOnLongClickListener {
                    onItemLongClick?.invoke(products[adapterPosition])
                    return@setOnLongClickListener true
                }
            }

            fun initialize() {
                itemView.img_picture.load(R.drawable.ic_camera)
                itemView.tv_location_value.text = ""
                itemView.tv_name_value.text = ""
                itemView.tv_manufacturer_value.text = ""
                itemView.tv_model_value.text = ""
                itemView.tv_size_value.text = ""
                itemView.tv_condition_value.text = ""
                itemView.tv_amount_value.text = ""
                itemView.tv_manufacture_date_value.text = ""
                itemView.tv_note_value.text = ""
            }
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