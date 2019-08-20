package com.iammert.easymapslib.util

import android.os.Handler

fun runAfter(durationInMillis: Long, func: () -> Unit) {
    Handler().postDelayed({ func() }, durationInMillis)

}