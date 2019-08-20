package com.iammert.easymapslib.ui

import android.location.Address

data class SelectedAddressViewState(val selectedAddress: Address, val moveCameraToLatLong: Boolean = false)