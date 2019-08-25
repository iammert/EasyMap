package com.iammert.easymapslib.data

import android.location.Address
import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng

data class SelectedAddressInfo(
    val addressTitle: String,
    val fullAddress: String,
    val buildingNumber: String,
    val floor: String,
    val door: String,
    val description: String,
    val address: Address?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(Address::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(addressTitle)
        parcel.writeString(fullAddress)
        parcel.writeString(buildingNumber)
        parcel.writeString(floor)
        parcel.writeString(door)
        parcel.writeString(description)
        parcel.writeParcelable(address, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun getLatLng(): LatLng {
        address?.let {
            return LatLng(it.latitude, it.longitude)
        }
        return LatLng(0.0, 0.0)
    }

    companion object CREATOR : Parcelable.Creator<SelectedAddressInfo> {
        override fun createFromParcel(parcel: Parcel): SelectedAddressInfo {
            return SelectedAddressInfo(parcel)
        }

        override fun newArray(size: Int): Array<SelectedAddressInfo?> {
            return arrayOfNulls(size)
        }

        fun empty(): SelectedAddressInfo {
            return SelectedAddressInfo(
                addressTitle = "",
                fullAddress = "",
                buildingNumber = "",
                floor = "",
                door = "",
                description = "",
                address = null
            )
        }
    }
}