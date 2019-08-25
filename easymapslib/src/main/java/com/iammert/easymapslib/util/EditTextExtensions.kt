package com.iammert.easymapslib.util

import android.text.Editable
import android.widget.EditText

fun EditText.afterTextChanged(func: (String) -> Unit) {
    addTextChangedListener(object : SimpleTextWatcher() {
        override fun afterTextChanged(s: Editable?) {
            super.afterTextChanged(s)
            func.invoke(s.toString())
        }
    })
}