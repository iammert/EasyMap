package com.iammert.easymapslib.location.places

import android.app.Application
import android.location.Address
import com.iammert.easymapslib.R
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import android.util.Log
import com.google.android.gms.common.api.Api
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.*
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.SingleOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit


class PlacesController(application: Application) {

    private val placesClient: PlacesClient

    private var searchQueryDisposable: Disposable? = null

    private val searchQuerySubject = PublishSubject.create<String>()

    private val searchQueryAddressResultSubject = PublishSubject.create<SearchResultResource>()

    init {
        Places.initialize(application, application.getString(R.string.maps_api_key))
        placesClient = Places.createClient(application)

        searchQueryDisposable = searchQuerySubject
            .debounce(300, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { loadPredictionResult(it) }
    }

    fun getSearchResultObservable(): Observable<SearchResultResource> = searchQueryAddressResultSubject

    fun searchAddress(query: String) {
        searchQuerySubject.onNext(query)
    }

    private fun loadPredictionResult(query: String) {
        searchQueryAddressResultSubject.onNext(SearchResultResource.loading())

        val token = AutocompleteSessionToken.newInstance()

        val request = FindAutocompletePredictionsRequest.builder()
            .setCountry("tr")
            .setTypeFilter(TypeFilter.ADDRESS)
            .setSessionToken(token)
            .setQuery(query)
            .build()

        placesClient
            .findAutocompletePredictions(request)
            .addOnSuccessListener {
                searchQueryAddressResultSubject.onNext(SearchResultResource.result(it.autocompletePredictions))
            }
            .addOnFailureListener {
                searchQueryAddressResultSubject.onNext(SearchResultResource.error(it))
            }
    }

    fun getAddressDetailObservable(autoCompletePrediction: AutocompletePrediction): Single<AddressInfo> {
        return Single.create { emitter ->
            placesClient
                .fetchPlace(createFetchPlaceRequest(autoCompletePrediction.placeId))
                .addOnSuccessListener {
                    val addressInfo = AddressInfo(
                        addressTitle = autoCompletePrediction.getPrimaryText(null).toString(),
                        addressFullText = autoCompletePrediction.getFullText(null).toString(),
                        latLong = it.place.latLng
                    )
                    emitter.onSuccess(addressInfo)
                }
                .addOnFailureListener {
                    Log.v("TESt", "fetchPlace FAİL: ${it.message}")
                }
        }
    }

    private fun createFetchPlaceRequest(placeId: String): FetchPlaceRequest {
        return FetchPlaceRequest.newInstance(placeId, arrayListOf(Place.Field.ADDRESS, Place.Field.LAT_LNG))
    }

    fun destroy() {
        searchQueryDisposable?.let {
            if (it.isDisposed.not()) {
                it.dispose()
            }
        }
    }
}