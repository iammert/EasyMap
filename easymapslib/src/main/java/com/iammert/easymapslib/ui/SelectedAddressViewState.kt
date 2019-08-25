package com.iammert.easymapslib.ui

import com.iammert.easymapslib.data.SelectedAddressInfo

data class SelectedAddressViewState(
    val selectedAddress: SelectedAddressInfo,
    val moveCameraToLatLong: Boolean = false
) {

    fun getFullAddressText(): String {
        return selectedAddress.address?.getAddressLine(0) ?: ""
    }

    fun getLatitude(): Double = selectedAddress.address?.latitude ?: 0.0

    fun getLongitude(): Double = selectedAddress.address?.longitude ?: 0.0
}