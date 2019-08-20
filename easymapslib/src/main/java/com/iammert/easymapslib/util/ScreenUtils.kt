package com.iammert.easymapslib.util

import android.content.Context
import android.content.res.Resources

fun getScreenWidth(): Int = Resources.getSystem().displayMetrics.widthPixels

fun getScreenHeight(): Int = Resources.getSystem().displayMetrics.heightPixels

fun Context.getStatusBarHeight(): Float {
    var result = 0
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
    if (resourceId > 0) {
        result = resources.getDimensionPixelSize(resourceId);
    }
    return result.toFloat()
}