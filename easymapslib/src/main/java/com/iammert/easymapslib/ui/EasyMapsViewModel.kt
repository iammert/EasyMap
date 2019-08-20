package com.iammert.easymapslib.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.iammert.easymapslib.location.geocoder.GeocoderController
import com.iammert.easymapslib.location.places.AddressInfo
import com.iammert.easymapslib.location.places.PlacesController
import com.iammert.easymapslib.location.places.SearchResultResource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

class EasyMapsViewModel(val app: Application) : AndroidViewModel(app) {

    private val selectedAddressDisposable: Disposable

    private val searchAddressResultDisposable: Disposable

    private var addressDetailDisposable: Disposable? = null

    private val geocoderController = GeocoderController(app)

    private val placesController = PlacesController(app)

    private val selectedAddressViewStateLiveData = MutableLiveData<SelectedAddressViewState>()

    private val searchQueryResultLiveData = MutableLiveData<SearchResultResource>()

    init {
        selectedAddressDisposable = geocoderController
            .getAddressObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { selectedAddressViewStateLiveData.value = SelectedAddressViewState(it) }

        searchAddressResultDisposable = placesController
            .getSearchResultObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                searchQueryResultLiveData.value = it
            }
    }

    fun updateAddress(latLong: LatLng) {
        geocoderController.updateAddress(latLong)
    }

    fun updateAddress(autocompletePrediction: AutocompletePrediction) {
        addressDetailDisposable = placesController.getAddressDetailObservable(autocompletePrediction)
            .flatMap { geocoderController.getAddress(it.latLong!!) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    selectedAddressViewStateLiveData.value = SelectedAddressViewState(
                        selectedAddress = it,
                        moveCameraToLatLong = true
                    )
                },
                { throwable -> Log.v("TEST", "Error")})
    }

    fun searchAddress(searchQuery: String) {
        placesController.searchAddress(searchQuery)
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