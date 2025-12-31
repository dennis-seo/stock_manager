package com.deky.productmanager.ui

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
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
import com.deky.productmanager.database.entity.Manufacturer
import com.deky.productmanager.databinding.ManufacturerFragmentBinding
import com.deky.productmanager.model.BaseViewModel
import com.deky.productmanager.model.ManufacturerViewModel
import com.deky.productmanager.util.DKLog

/*
* Created by Diane on 16/01/2021
*/
class ManufacturerFragment : Fragment() {
    companion object {
        private const val TAG = "ManufacturerFragment"
        fun newInstance(): ManufacturerFragment = ManufacturerFragment()
    }

    private lateinit var dataBinding: ManufacturerFragmentBinding

    private val viewModel by lazy {
        ViewModelProvider(this, BaseViewModel.Factory(requireActivity().application))
            .get(ManufacturerViewModel::class.java)
    }

    private val mainCategoryAdapter: CategoryAdapter by lazy {
        CategoryAdapter()
    }

    private val subCategoryAdapter: CategoryAdapter by lazy {
        CategoryAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataBinding = DataBindingUtil.inflate<ManufacturerFragmentBinding>(
            inflater, R.layout.manufacturer_fragment, container, false
        ).apply {
            lifecycleOwner = this@ManufacturerFragment
        }

        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataBinding.edMainManufacturer.setOnKeyListener(View.OnKeyListener { v, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (dataBinding.edMainManufacturer.text.isNotEmpty()) {
                    viewModel.insertMainManufacturer(dataBinding.edMainManufacturer.text.toString())
                    (v as EditText).text.clear()
                }
                return@OnKeyListener true
            }
            false
        })

        dataBinding.edSubManufacturer.setOnKeyListener(View.OnKeyListener { v, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (dataBinding.edSubManufacturer.text.isNotEmpty()) {
                    viewModel.insertSubManufacturer(dataBinding.edSubManufacturer.text.toString())
                    (v as EditText).text.clear()
                }
                return@OnKeyListener true
            }
            false
        })

        dataBinding.ivMainCategoryInput.setOnClickListener {
            if (dataBinding.edMainManufacturer.text.isNotEmpty()) {
                viewModel.insertMainManufacturer(dataBinding.edMainManufacturer.text.toString())
                dataBinding.edMainManufacturer.text.clear()
            }
        }

        dataBinding.ivSubCategoryInput.setOnClickListener {
            if (dataBinding.edSubManufacturer.text.isNotEmpty()) {
                viewModel.insertSubManufacturer(dataBinding.edSubManufacturer.text.toString())
                dataBinding.edSubManufacturer.text.clear()
            }
        }

        dataBinding.recyclerMainCategory.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = mainCategoryAdapter
        }

        dataBinding.recyclerSubCategory.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = subCategoryAdapter
        }

        (dataBinding.recyclerMainCategory.adapter as CategoryAdapter).tracker = getTracker()


        // observer

        viewModel.mainCategory.observe(viewLifecycleOwner, Observer {mainCategory ->
            DKLog.debug(TAG) {
                "mainCategory update: ${mainCategory.size}"
            }
            mainCategoryAdapter.submitList(mainCategory)
        })

        viewModel.subCategory.observe(viewLifecycleOwner, Observer { subCategroy ->
            DKLog.debug(TAG) {
                "subCategory update: ${subCategroy?.size}"
            }
            subCategoryAdapter.submitList(subCategroy)
        })

        viewModel.selectedCategory.observe(viewLifecycleOwner, Observer { selectedCategory ->
            DKLog.debug(TAG) {
                "selected category : ${selectedCategory}"
            }
            dataBinding.edMainManufacturer.isEnabled = selectedCategory == null
            dataBinding.ivMainCategoryInput.isEnabled = selectedCategory == null
            dataBinding.edSubManufacturer.isEnabled = selectedCategory != null
            dataBinding.ivSubCategoryInput.isEnabled = selectedCategory != null
        })
    }

    private fun getTracker(): SelectionTracker<Long> {

        val selectionTracker = SelectionTracker.Builder<Long>(
            "id",
            dataBinding.recyclerMainCategory,
            MyItemKeyProvider(dataBinding.recyclerMainCategory),
            SelectionDetailsLookup(dataBinding.recyclerMainCategory),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(SelectionPredicates.createSelectSingleAnything()).build()

        selectionTracker?.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onItemStateChanged(id: Long, selected: Boolean) {
                DKLog.debug(TAG) { "onItemStateChanged() - category id : $id  /   selected : $selected" }
                // TODO 소분류 항목 업데이트
                if (selected) {
                    findCategoryInMain(id).let {
                        viewModel.selectedCategory.postValue(it)
                    }
                    viewModel.parentId.postValue(id)
                } else {
                    viewModel.parentId.postValue(-1)
                    viewModel.selectedCategory.postValue(null)
                }

                super.onItemStateChanged(id, selected)
            }
        })
        return selectionTracker
    }

    private fun findCategoryInMain(id: Long): Manufacturer? {
        viewModel.mainCategory.value.let { mainCategoryList ->
            mainCategoryList?.let {
                for(category in mainCategoryList) {
                    if(category.id == id)
                        return category
                }
            }
        }
        return null
    }

    private inner class CategoryAdapter :
        ListAdapter<Manufacturer, CategoryAdapter.CategoryViewHolder>(diffItemCallback) {

        var tracker: SelectionTracker<Long>? = null

        init {
            setHasStableIds(true)
        }

        override fun getItemId(position: Int): Long {
            return getItem(position).id
        }

        override fun getItemViewType(position: Int): Int {
            return position
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            return CategoryViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.category_list_item,
                    parent,
                    false
                )
            )
        }


        override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
            val category = getItem(position)
            holder.bind(category, tracker?.isSelected(category.id) ?: false)
        }

        inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val containerItem by lazy { itemView.findViewById(R.id.container_category_item)  as LinearLayout}
            val tvName: TextView by lazy { itemView.findViewById(R.id.tv_category_name) as TextView }
            val btnDelete: ImageView by lazy { itemView.findViewById(R.id.btn_category_delete) as ImageView }

            fun bind(category: Manufacturer, isSelected: Boolean) {
                DKLog.debug(TAG) { "bind() category : ${category.parentCategory} / ${category.id} / ${category.name} / $isSelected"}

                tvName.text = category.name
                if(isSelected) {
                    containerItem.setBackgroundColor(Color.BLUE)
                    tvName.setTextColor(Color.WHITE)
                    btnDelete.setImageDrawable(null)
                } else {
                    containerItem.setBackgroundColor(Color.TRANSPARENT)
                    val defaultTextColor = ContextCompat.getColor(itemView.context, android.R.color.tab_indicator_text)
                    tvName.setTextColor(defaultTextColor)
                    btnDelete.setImageResource(R.drawable.ic_round_category_delete_24)
                    btnDelete.setOnClickListener {
                        showAlertDelete(category)
                    }
                }
            }

            private fun showAlertDelete(category: Manufacturer) {
                DKLog.debug(TAG) { "showAlertDelete()" }

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

    private val diffItemCallback = object : DiffUtil.ItemCallback<Manufacturer>() {

        override fun areItemsTheSame(oldItem: Manufacturer, newItem: Manufacturer): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Manufacturer, newItem: Manufacturer): Boolean {
            return oldItem == newItem
        }
    }

    class MyItemKeyProvider(private val recyclerView: RecyclerView) : ItemKeyProvider<Long>(ItemKeyProvider.SCOPE_MAPPED) {

        override fun getKey(position: Int): Long? {
            return recyclerView.adapter?.getItemId(position)
        }

        override fun getPosition(key: Long): Int {
            val viewHolder = recyclerView.findViewHolderForItemId(key)
            return viewHolder?.layoutPosition ?: RecyclerView.NO_POSITION
        }
    }
}
