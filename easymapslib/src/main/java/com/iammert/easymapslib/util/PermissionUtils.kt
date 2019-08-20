package com.iammert.easymapslib.util

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class PermissionUtils {
    companion object {

        val locationPermissions = arrayOf(ACCESS_FINE_LOCATION)

        fun isLocationPermissionsGranted(context: Context) = locationPermissions.isPermissionsGranted(context)

        fun isPermissionResultsGranted(grantResult: IntArray) =
            grantResult.none { it != PackageManager.PERMISSION_GRANTED }
    }
}

private fun Array<String>.isPermissionsGranted(context: Context): Boolean {
    for (i in this.indices) {
        if (ActivityCompat.checkSelfPermission(context, this[i]) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
    }
    return true
}