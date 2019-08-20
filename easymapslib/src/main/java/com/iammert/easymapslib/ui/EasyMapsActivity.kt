package com.iammert.easymapslib.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.iammert.easymapslib.location.livedata.LocationData
import com.iammert.easymapslib.location.livedata.LocationLiveData
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.CameraUpdateFactory
import android.location.Location
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import kotlinx.android.synthetic.main.activity_easy_maps.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.iammert.easymapslib.R
import com.iammert.easymapslib.location.geocoder.GeocoderController
import com.iammert.easymapslib.location.map.GoogleMapController
import com.iammert.easymapslib.location.places.SearchResultState
import com.iammert.easymapslib.location.places.SearchResultState.*
import com.iammert.easymapslib.ui.view.LocationMarkerView
import com.iammert.easymapslib.ui.view.predictionresult.PredictionResultView
import kotlinx.android.synthetic.main.activity_easy_maps.layoutBottomSheetForm
import kotlinx.android.synthetic.main.layout_address_form.*
import com.iammert.easymapslib.util.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class EasyMapsActivity : AppCompatActivity() {

    private lateinit var googleMapController: GoogleMapController

    private lateinit var locationLiveData: LocationLiveData

    private lateinit var easyMapsViewModel: EasyMapsViewModel

    private var isMapsInitialized = false

    private var bottomSheetState = STATE_COLLAPSED

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_easy_maps)

        easyMapsViewModel = ViewModelProviders.of(this).get(EasyMapsViewModel::class.java)

        observeFormBottomSheeet()

        observeAddressSearch()

        googleMapController = GoogleMapController()
        googleMapController.addIdleListener(GoogleMap.OnCameraIdleListener {
            googleMapController.getMap()?.let { map ->
                val markerPoint = locationMarkerView.getMarkerPoint()
                val markerLatLong = map.projection.fromScreenLocation(markerPoint)
                easyMapsViewModel.updateAddress(markerLatLong)
            }
        })

        googleMapController.addClickListener(GoogleMap.OnMapClickListener {
            if (isExpanded()) {
                collapseBottomSheet()
            }
        })

        googleMapController.addIdleListener(locationMarkerView)
        googleMapController.addMoveStartListener(locationMarkerView)

        locationLiveData = LocationLiveData(this@EasyMapsActivity)

        locationLiveData.observe(this, Observer {
            when (it?.status) {
                LocationData.Status.PERMISSION_REQUIRED -> askLocationPermission(it.permissionList)
                LocationData.Status.ENABLE_SETTINGS -> enableLocationSettings(it.resolvableApiException)
                LocationData.Status.LOCATION_SUCCESS -> updateUserLocation(it.location!!)
            }
        })

        with(supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment) {
            findViewById<LocationMarkerView>(R.id.locationMarkerView)
                .initialize(
                    mapFragmentView = this.view,
                    bottomSheetExpandedHeight = R.dimen.size_form_full_height,
                    bottomSheetCollapsedHeight = R.dimen.size_form_peek_height
                )

            getMapAsync { map ->
                googleMapController.setGoogleMap(map)
                locationLiveData.start()
            }
        }

        easyMapsViewModel.getSelectedAddressViewStateLiveData().observe(this, Observer {
            val addressLine = it.selectedAddress.getAddressLine(0)
            editTextFullAddress.setText(addressLine)
            textViewFullAddress.text = addressLine

            if (it.moveCameraToLatLong) {
                googleMapController.animateCamera(it.selectedAddress.latitude, it.selectedAddress.longitude)
            }
        })

        easyMapsViewModel.getSearchQueryResultLiveData().observe(this, Observer {
            predictionResultView.setPredictions(it.searchResult)

            when (it.searchResultState) {
                LOADING -> {
                    imageViewSearch.visibility = View.GONE
                    progressBarSearchLoading.visibility = View.VISIBLE
                }
                COMPLETE -> {
                    imageViewSearch.visibility = View.VISIBLE
                    progressBarSearchLoading.visibility = View.GONE
                }
                ERROR -> {
                    imageViewSearch.visibility = View.VISIBLE
                    progressBarSearchLoading.visibility = View.GONE
                }
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (PermissionUtils.isPermissionResultsGranted(grantResults)) {
            locationLiveData.start()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_LOCATION_SETTINGS && resultCode == Activity.RESULT_OK) {
            locationLiveData.start()
        }
    }

    override fun onBackPressed() {
        if (predictionResultView.isShowing()) {
            predictionResultView.hide()
            editTextSearchAddress.clearFocus()
            collapseBottomSheet()
            return
        }

        if (isExpanded()) {
            collapseBottomSheet()
            return
        }
        super.onBackPressed()
    }

    private fun askLocationPermission(permissionList: Array<String?>) {
        ActivityCompat.requestPermissions(this, permissionList, REQUEST_CODE_LOCATION_PERMISSION)
    }

    private fun enableLocationSettings(exception: ResolvableApiException?) {
        exception?.startResolutionForResult(this, REQUEST_CODE_LOCATION_SETTINGS)
    }

    @SuppressLint("MissingPermission")
    private fun updateUserLocation(location: Location) {
        googleMapController.getMap()?.let { map ->
            if (isMapsInitialized.not()) {
                isMapsInitialized = true
                map.isMyLocationEnabled = true
                map.uiSettings.isMyLocationButtonEnabled = true
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 14.0f))
            }
        }
    }

    private fun observeAddressSearch() {
        editTextSearchAddress.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                easyMapsViewModel.searchAddress(s.toString())
            }
        })

        editTextSearchAddress.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus && predictionResultView.isShowing().not()) {
                predictionResultView.show()
                hideBottomSheet()
            }
        }

        predictionResultView.setOnItemClicked {
            hideKeyboard()
            runAfter(500) {
                editTextSearchAddress.clearFocus()
                collapseBottomSheet()
                predictionResultView.hide()
                easyMapsViewModel.updateAddress(it)
            }
        }

        imageViewClear.setOnClickListener { editTextSearchAddress.setText("") }
    }

    private fun observeFormBottomSheeet() {
        val bottomSheetBehavior = from(layoutBottomSheetForm)

        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                updateFormFieldVisibilityOnScroll(slideOffset)
                locationMarkerView.updateMarkerViewPosition(slideOffset)
            }

            @SuppressLint("SwitchIntDef")
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (bottomSheetState == newState) {
                    return
                }

                when (newState) {
                    STATE_EXPANDED -> {
                        googleMapController.disableGestures()
                        moveCameraOnBottomStateChanged(newState)
                        bottomSheetState = STATE_EXPANDED
                    }
                    STATE_COLLAPSED -> {
                        googleMapController.enableGestures()
                        bottomSheetState = STATE_COLLAPSED
                        moveCameraOnBottomStateChanged(newState)
                    }
                }
            }
        })

        layoutAddressCollapsed.setOnClickListener { bottomSheetBehavior.state = STATE_EXPANDED }
    }

    private fun updateFormFieldVisibilityOnScroll(slideOffset: Float) {
        layoutAddressCollapsed.alpha = 1.0f - slideOffset
        layoutAddressFormExpanded.alpha = slideOffset

        when (slideOffset) {
            0f -> {
                layoutAddressCollapsed.visibility = View.VISIBLE
                layoutAddressFormExpanded.visibility = View.INVISIBLE
            }
            1f -> {
                layoutAddressCollapsed.visibility = View.INVISIBLE
                layoutAddressFormExpanded.visibility = View.VISIBLE
            }
            else -> {
                layoutAddressCollapsed.visibility = View.VISIBLE
                layoutAddressFormExpanded.visibility = View.VISIBLE
            }
        }
    }

    private fun moveCameraOnBottomStateChanged(bottomSheetState: Int) {
        val point = when (bottomSheetState) {
            STATE_COLLAPSED -> locationMarkerView.getCollapsedCameraPoint()
            STATE_EXPANDED -> locationMarkerView.getExpandedCameraPoint()
            else -> null
        }

        point?.let {
            googleMapController
                .animateCameraToPoint(
                    screenPoint = point,
                    onStarted = { locationMarkerView.setFloatingOnMove(false) },
                    onFinished = { locationMarkerView.setFloatingOnMove(true) })
        }
    }

    private fun expandBottomSheet() {
        val formBehavior = from(layoutBottomSheetForm)
        formBehavior.isHideable = false
        formBehavior.state = STATE_EXPANDED
    }

    private fun collapseBottomSheet() {
        val formBehavior = from(layoutBottomSheetForm)
        formBehavior.isHideable = false
        formBehavior.state = STATE_COLLAPSED
    }

    private fun hideBottomSheet() {
        val formBehavior = from(layoutBottomSheetForm)
        formBehavior.isHideable = true
        formBehavior.state = STATE_HIDDEN
    }

    private fun isExpanded() = from(layoutBottomSheetForm).state == STATE_EXPANDED

    private fun isCollapsed() = from(layoutBottomSheetForm).state == STATE_COLLAPSED

    companion object {

        const val REQUEST_CODE_LOCATION_PERMISSION = 12
        const val REQUEST_CODE_LOCATION_SETTINGS = 13

        fun newIntent(context: Context): Intent {
            return Intent(context, EasyMapsActivity::class.java)
        }
    }
}