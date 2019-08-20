package com.iammert.easymapslib.location.livedata

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.location.Location
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.iammert.easymapslib.util.PermissionUtils

class LocationLiveData(private val activity: Activity) : MediatorLiveData<LocationData>() {

    /**
     * LiveData
     */
    private val coarseLocationLiveData = MutableLiveData<Location>()
    private val fineLocationLiveData = MutableLiveData<Location>()

    /**
     * Location Request and Client
     */
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
    private val locationRequest = createLocationRequest()

    /**
     * Location Settings
     */
    private val locationSettingsBuilder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
    private val settingsClient = LocationServices.getSettingsClient(activity)
    private var isStarted: Boolean = false

    /**
     * Callback
     */
    private val fineLocationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            removeSource(coarseLocationLiveData)
            fineLocationLiveData.value = result?.lastLocation
        }
    }

    /**
     * Initialization
     */
    init {
        addSource(coarseLocationLiveData) { value = LocationData.success(it) }
        addSource(fineLocationLiveData) { value = LocationData.success(it) }
    }

    override fun onActive() {
        super.onActive()
        if (isStarted) {
            startLocationUpdates()
        }
    }

    override fun onInactive() {
        super.onInactive()
        stopLocationUpdates()
    }

    fun start() {
        isStarted = true
        startLocationUpdates()
    }

    /**
     * Checks runtime permission first.
     * Then check if GPS settings is enabled by user
     * If all good, then start listening user location
     * and update livedata
     */
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (PermissionUtils.isLocationPermissionsGranted(activity).not()) {
            value = LocationData.permissionRequired(listOf(Manifest.permission.ACCESS_FINE_LOCATION))
            return
        }

        val settingsTask = settingsClient.checkLocationSettings(locationSettingsBuilder.build())

        settingsTask.addOnSuccessListener {
            fusedLocationClient.lastLocation.addOnSuccessListener { it?.let { coarseLocationLiveData.value = it } }
            fusedLocationClient.requestLocationUpdates(locationRequest, fineLocationCallback, null)
        }

        settingsTask.addOnFailureListener {
            value = if (it is ResolvableApiException) LocationData.settingsRequired(it) else LocationData.error(it)
        }
    }

    /**
     * Removes listener onInactive
     */
    private fun stopLocationUpdates() = fusedLocationClient.removeLocationUpdates(fineLocationCallback)

    /**
     * Creates a LocationRequest model
     */
    private fun createLocationRequest(): LocationRequest = LocationRequest().apply {
        interval = 10000
        fastestInterval = 5000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
}