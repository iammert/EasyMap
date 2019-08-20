package com.iammert.easymapslib.location.map

import android.annotation.SuppressLint
import android.graphics.Point
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng

class GoogleMapController(val mapZoomLevel: Float = 14f) {

    private var googleMap: GoogleMap? = null

    private val cameraMoveStartListeners = arrayListOf<GoogleMap.OnCameraMoveStartedListener>()

    private val cameraIdleListeners = arrayListOf<GoogleMap.OnCameraIdleListener>()

    private val mapClickListeners = arrayListOf<GoogleMap.OnMapClickListener>()

    fun setGoogleMap(googleMap: GoogleMap) {
        this.googleMap = googleMap
        with(this.googleMap!!) {
            setOnCameraIdleListener { notifyCameraIdle() }
            setOnCameraMoveStartedListener { notifyCameraMoveStart(it) }
            setOnMapClickListener { notifyMapClicked(it) }
        }
    }

    fun getMap(): GoogleMap? = googleMap

    fun animateCameraToPoint(screenPoint: Point, onStarted: () -> Unit = {}, onFinished: () -> (Unit) = {}) {
        onStarted.invoke()
        val pointLatLng = googleMap?.projection?.fromScreenLocation(screenPoint)
        val zoomLevel = googleMap?.cameraPosition?.zoom ?: mapZoomLevel
        val updateFactory = CameraUpdateFactory.newLatLngZoom(pointLatLng, zoomLevel)
        googleMap?.animateCamera(updateFactory, 150, object : SimpleCancellableCallback() {
            override fun onFinish() {
                onFinished.invoke()
            }
        })
    }

    fun animateCamera(latitude: Double, longitude: Double) {
        googleMap?.let { map ->
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 14.0f))
        }
    }

    @SuppressLint("MissingPermission")
    fun disableGestures() {
        googleMap?.let {
            it.uiSettings.setAllGesturesEnabled(false)
            it.isMyLocationEnabled = false
        }
    }

    @SuppressLint("MissingPermission")
    fun enableGestures() {
        googleMap?.let {
            it.uiSettings.setAllGesturesEnabled(true)
            it.isMyLocationEnabled = true
        }
    }

    fun addMoveStartListener(moveStartListener: GoogleMap.OnCameraMoveStartedListener) {
        if (cameraMoveStartListeners.contains(moveStartListener).not()) {
            cameraMoveStartListeners.add(moveStartListener)
        }
    }

    fun addIdleListener(idleListener: GoogleMap.OnCameraIdleListener) {
        if (cameraIdleListeners.contains(idleListener).not()) {
            cameraIdleListeners.add(idleListener)
        }
    }

    fun addClickListener(clickListener: GoogleMap.OnMapClickListener) {
        if (mapClickListeners.contains(clickListener).not()) {
            mapClickListeners.add(clickListener)
        }
    }

    private fun notifyCameraMoveStart(reason: Int) {
        cameraMoveStartListeners.forEach { it.onCameraMoveStarted(reason) }
    }

    private fun notifyCameraIdle() {
        cameraIdleListeners.forEach { it.onCameraIdle() }
    }

    private fun notifyMapClicked(latLng: LatLng) {
        mapClickListeners.forEach { it.onMapClick(latLng) }
    }

}