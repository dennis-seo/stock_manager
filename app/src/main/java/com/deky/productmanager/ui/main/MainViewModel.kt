package com.deky.productmanager.ui.main

import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModel


class MainViewModel : ViewModel() {
    companion object {
        private const val TAG = "MainViewModel"
    }

    fun onClickInput(view: View) {
        Toast.makeText(view.context, "onClickInput()", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "onClickInput()")
    }

    fun onClickModify(view: View) {
        Toast.makeText(view.context, "onClickModify()", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "onClickModify()")
    }

    fun onClickConfirm(view: View) {
        Toast.makeText(view.context, "onClickConfirm()", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "onClickConfirm()")
    }
}
