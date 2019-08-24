package com.iammert.easymapslib.data

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng

data class SelectedAddressInfo(
    val addressType: AddressType,
    val addressTitle: String,
    val fullAddress: String,
    val buildingNumber: String,
    val floor: String,
    val door: String,
    val description: String,
    val latLng: LatLng?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        AddressType.valueOf(parcel.readString()),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(LatLng::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(this.addressType.name)
        parcel.writeString(addressTitle)
        parcel.writeString(fullAddress)
        parcel.writeString(buildingNumber)
        parcel.writeString(floor)
        parcel.writeString(door)
        parcel.writeString(description)
        parcel.writeParcelable(latLng, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SelectedAddressInfo> {
        override fun createFromParcel(parcel: Parcel): SelectedAddressInfo {
            return SelectedAddressInfo(parcel)
        }

        override fun newArray(size: Int): Array<SelectedAddressInfo?> {
            return arrayOfNulls(size)
        }
    }
}