package com.deky.productmanager.ui.main

import android.util.Log
import android.view.View
import android.widget.Toast


class MainViewHandler {

    fun onClickInput(view: View) {
        Toast.makeText(view.context, "onClickInput()", Toast.LENGTH_SHORT).show()
        Log.d("AAA", "onClickInput")
    }

    fun onClickModify(view: View) {
        Toast.makeText(view.context, "onClickModify()", Toast.LENGTH_SHORT).show()
        Log.d("AAA", "onClickModify")
    }

    fun onClickConfirm(view: View) {
        Toast.makeText(view.context, "onClickConfirm()", Toast.LENGTH_SHORT).show()
        Log.d("AAA", "onClickConfirm")
    }
}
