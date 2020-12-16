package com.deky.productmanager.ui

import android.content.DialogInterface
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.selection.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.deky.productmanager.R
import com.deky.productmanager.database.entity.Category
import com.deky.productmanager.databinding.CategoryFragmentBinding
import com.deky.productmanager.model.BaseViewModel
import com.deky.productmanager.model.CategoryListViewModel
import com.deky.productmanager.util.DKLog
import com.deky.productmanager.util.simpleTag
import kotlinx.android.synthetic.main.category_fragment.*
import kotlinx.android.synthetic.main.datalist_fragment.*
import kotlinx.android.synthetic.main.datalist_item.view.*
import kotlinx.android.synthetic.main.datalist_pager_recylerview_layout.view.*
import java.util.logging.LogManager


/*
* Copyright (C) 2020 Kakao corp. All rights reserved.
*
* Created by Dennis.Seo on 15/05/2020
*
*/
class CategoryListFragment : Fragment() {
    companion object {
        private const val TAG = "CategoryListFragment"
        fun newInstance() = CategoryListFragment()
    }

    private lateinit var dataBinding: CategoryFragmentBinding
    private lateinit var viewModel: CategoryListViewModel

    private var selectionTracker : SelectionTracker<Long>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, BaseViewModel.Factory(requireActivity().application))
            .get(CategoryListViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dataBinding = DataBindingUtil.inflate<CategoryFragmentBinding>(
            inflater, R.layout.category_fragment, container, false).apply {
            lifecycleOwner = this@CategoryListFragment
        }

        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoryAdapter = CategoryAdapter()
        recycler_main_category.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = categoryAdapter
//            (adapter as CategoryAdapter).tracker = selectionTracker
        }

        initTracker()
        (recycler_main_category.adapter as CategoryAdapter).tracker = selectionTracker

        viewModel.mainCategory.observe(this, Observer { mainCategory ->
            categoryAdapter.submitList(mainCategory)
        })

        val a = viewModel.mainCategory.value
        for(category in a) {

        }
    }

    private fun initTracker() {
        selectionTracker = SelectionTracker.Builder<Long>(
            "id",
            recycler_main_category,
            StableIdKeyProvider(recycler_main_category),
            SelectionDetailsLookup(recycler_main_category),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(SelectionPredicates.createSelectSingleAnything()).build()

        selectionTracker?.addObserver(object: SelectionTracker.SelectionObserver<Long>() {
            override fun onItemStateChanged(key: Long, selected: Boolean) {
                // TODO
                DKLog.debug(TAG) {"onItemStateChanged() - key : $key  /   selected : $selected"}
                super.onItemStateChanged(key, selected)
            }
        })
    }


    private fun showAlertDelete(category: Category) {

        context?.let {
            val builder = AlertDialog.Builder(it).apply {
                setMessage(R.string.message_alert_delete_data)
                setPositiveButton(
                    R.string.btn_confirm,
                    DialogInterface.OnClickListener { _, _ ->
                        viewModel.delete(category)
                    }
                )
                setNegativeButton(android.R.string.no, null)
            }
            builder.show()
        }
    }

    private inner class CategoryAdapter :
        ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(diffItemCallback) {

        var tracker: SelectionTracker<Long>? = null
        var onItemClick: ((Category) -> Unit)? = null
        var onItemLongClick: ((Category) -> Unit)? = null

        init {
            setHasStableIds(true)
        }

        override fun getItemId(position: Int): Long {
            return getItem(position).id
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            return CategoryViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.category_list_item, parent, false)
            )
        }


        override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
            tracker?.let {
                holder.bind(getItem(position), it.isSelected(position.toLong()))
            }
        }

        inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val containerItem by lazy { itemView.findViewById(R.id.container_category_item)  as LinearLayout}
            val tvName: TextView by lazy { itemView.findViewById(R.id.tv_category_name) as TextView }
            val btnDelete: ImageButton by lazy { itemView.findViewById(R.id.btn_category_delete) as ImageButton}

            fun bind(category: Category, isSelected: Boolean) {
                tvName.text = category.name
                containerItem.isSelected = isSelected
            }

            fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
                object : ItemDetailsLookup.ItemDetails<Long>() {
                    override fun getPosition(): Int = adapterPosition
                    override fun getSelectionKey(): Long? = itemId
                }
        }
    }

    class SelectionDetailsLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<Long>() {
        override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
            val view = recyclerView.findChildViewUnder(event.x, event.y)
            if (view != null) {
                return (recyclerView.getChildViewHolder(view) as CategoryAdapter.CategoryViewHolder)
                    .getItemDetails()
            }
            return null
        }
    }

    private val diffItemCallback = object : DiffUtil.ItemCallback<Category>() {

        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem
        }
    }

}
