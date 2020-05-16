package com.deky.productmanager.util

import androidx.recyclerview.widget.RecyclerView


/*
* Copyright (C) 2020 Kakao corp. All rights reserved.
*
* Created by Dennis.Seo on 16/05/2020
*
**/
/**
* example)
*   override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
*       val view = LayoutInflater.from(parent.context).inflate(R.layout.datalist_item, parent, false)
*       return ProductViewHolder(view).listen { position, type ->
*           val product = products[position]
*       }
*   }
**/
fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
    itemView.setOnClickListener {
        event.invoke(adapterPosition, itemViewType)
    }
    return this
}