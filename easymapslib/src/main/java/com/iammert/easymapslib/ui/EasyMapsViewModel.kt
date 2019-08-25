package com.iammert.easymapslib.ui

import android.app.Application
import android.location.Address
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.iammert.easymapslib.data.AddressType
import com.iammert.easymapslib.data.SelectedAddressInfo
import com.iammert.easymapslib.location.geocoder.GeocoderController
import com.iammert.easymapslib.location.places.AddressInfo
import com.iammert.easymapslib.location.places.PlacesController
import com.iammert.easymapslib.location.places.SearchResultResource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_address_form.*

class EasyMapsViewModel(val app: Application) : AndroidViewModel(app) {

    private val selectedAddressDisposable: Disposable

    private val searchAddressResultDisposable: Disposable

    private var addressDetailDisposable: Disposable? = null

    private val geocoderController = GeocoderController(app)

    private val placesController = PlacesController(app)

    private val selectedAddressViewStateLiveData = MutableLiveData<SelectedAddressViewState>()

    private val searchQueryResultLiveData = MutableLiveData<SearchResultResource>()

    private var selectedAddressInfo = SelectedAddressInfo.empty()

    private var isInitializedWithAddress = false

    init {
        selectedAddressViewStateLiveData.value = SelectedAddressViewState(selectedAddressInfo)

        selectedAddressDisposable = geocoderController
            .getAddressObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { updateAddress(it, false) }

        searchAddressResultDisposable = placesController
            .getSearchResultObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                searchQueryResultLiveData.value = it
            }
    }

    fun initializeWithAddress(selectedAddressInfo: SelectedAddressInfo) {
        isInitializedWithAddress = true
        this.selectedAddressInfo = selectedAddressInfo
        selectedAddressViewStateLiveData.value =
            SelectedAddressViewState(
                selectedAddress = this.selectedAddressInfo,
                moveCameraToLatLong = true
            )
    }

    fun isInitializedWithAddress() = isInitializedWithAddress

    fun updateLatLong(latLong: LatLng) {
        geocoderController.updateAddress(latLong)
    }

    fun updateAutoCompletePrediction(autocompletePrediction: AutocompletePrediction) {
        addressDetailDisposable = placesController.getAddressDetailObservable(autocompletePrediction)
            .flatMap { geocoderController.getAddress(it.latLong!!) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { address -> updateAddress(address, true) },
                { throwable -> Log.v("TEST", "Error") })
    }

    fun updateAddress(address: Address, moveCamera: Boolean) {
        selectedAddressInfo = this.selectedAddressInfo.copy(address = address, fullAddress = address.getAddressLine(0))
        selectedAddressViewStateLiveData.value = SelectedAddressViewState(
            selectedAddress = selectedAddressInfo,
            moveCameraToLatLong = moveCamera
        )
    }

    fun updateBuildingNumber(buildingNumber: String) {
        selectedAddressInfo = selectedAddressInfo.copy(buildingNumber = buildingNumber)
    }

    fun updateFloorNumber(floor: String) {
        selectedAddressInfo = selectedAddressInfo.copy(floor = floor)
    }

    fun updateType(addressType: AddressType) {
        selectedAddressInfo = selectedAddressInfo.copy(addressType = addressType)
    }

    fun updateDoorNumber(doorNumber: String) {
        selectedAddressInfo = selectedAddressInfo.copy(door = doorNumber)
    }

    fun updateAddressTitle(addressTitle: String) {
        selectedAddressInfo = selectedAddressInfo.copy(addressTitle = addressTitle)
    }

    fun updateDescription(description: String) {
        selectedAddressInfo = selectedAddressInfo.copy(description = description)
    }

    fun searchAddress(searchQuery: String) {
        placesController.searchAddress(searchQuery)
    }

    fun getSelectedAddressInfo(): SelectedAddressInfo {
        return selectedAddressInfo
    }

    fun getSelectedAddressViewStateLiveData(): LiveData<SelectedAddressViewState> = selectedAddressViewStateLiveData

    fun getSearchQueryResultLiveData(): LiveData<SearchResultResource> = searchQueryResultLiveData

    override fun onCleared() {
        super.onCleared()
        if (selectedAddressDisposable.isDisposed.not()) {
            selectedAddressDisposable.dispose()
        }

        if (searchAddressResultDisposable.isDisposed.not()) {
            searchAddressResultDisposable.dispose()
        }

        addressDetailDisposable?.let {
            if (it.isDisposed.not()) {
                it.dispose()
            }
        }

        placesController.destroy()
        geocoderController.destroy()
    }

}