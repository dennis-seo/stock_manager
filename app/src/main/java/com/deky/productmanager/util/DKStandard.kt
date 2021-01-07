package com.deky.productmanager.util

import android.content.Context
import android.content.res.Resources
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView

/**
 * Copyright (C) 2020 Kakao corp. All rights reserved.
 *
 */
fun Any.simpleTag(): String {
    return "${javaClass.simpleName}[@${Integer.toHexString(hashCode())}]"
}

fun String.prependIndent(indent: Int) = when (indent) {
    1 -> "    $this"
    2 -> "        $this"
    3 -> "            $this"
    4 -> "                $this"
    else -> this
}

fun dpToPx(dp: Int): Int {
    return (dp * Resources.getSystem().displayMetrics.density).toInt()
}

/**
 * Extension method to show toast for Context.
 */
fun Context?.toast(@StringRes textId: Int, duration: Int = Toast.LENGTH_SHORT)
        = this?.let { Toast.makeText(it, textId, duration).show() }

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


fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}