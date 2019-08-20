package com.iammert.easymapslib.util

import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver

inline fun <T : View> T.afterMeasured(crossinline f: T.() -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (measuredWidth > 0 && measuredHeight > 0) {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                f()
            }
        }
    })
}

fun View.updateMargins(left: Int? = null, top: Int? = null, right: Int? = null, bottom: Int? = null) {
    val currentLayoutParams = (layoutParams as ViewGroup.MarginLayoutParams)
    currentLayoutParams.setMargins(
        left ?: currentLayoutParams.leftMargin,
        top ?: currentLayoutParams.topMargin,
        right ?: currentLayoutParams.rightMargin,
        bottom ?: currentLayoutParams.bottomMargin
    )
    layoutParams = currentLayoutParams
    invalidate()
}