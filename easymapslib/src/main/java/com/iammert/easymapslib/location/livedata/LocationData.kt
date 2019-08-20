package com.iammert.easymapslib.location.livedata

import android.location.Location
import com.google.android.gms.common.api.ResolvableApiException

class LocationData private constructor(val status: Status,
                                       val location: Location? = null,
                                       val exception: Exception? = null,
                                       val resolvableApiException: ResolvableApiException? = null,
                                       val permissionList: Array<String?> = emptyArray()) {

    enum class Status {
        LOCATION_SUCCESS, ERROR, PERMISSION_REQUIRED, ENABLE_SETTINGS
    }

    companion object {

        fun success(location: Location?): LocationData {
            return LocationData(Status.LOCATION_SUCCESS, location)
        }

        fun error(exception: Exception): LocationData {
            return LocationData(Status.ERROR, exception = exception)
        }

        fun permissionRequired(permissionList: List<String>): LocationData {
            return LocationData(Status.PERMISSION_REQUIRED, permissionList = permissionList.toTypedArray())
        }

        fun settingsRequired(exception: ResolvableApiException): LocationData {
            return LocationData(Status.ENABLE_SETTINGS, resolvableApiException = exception)
        }
    }
}