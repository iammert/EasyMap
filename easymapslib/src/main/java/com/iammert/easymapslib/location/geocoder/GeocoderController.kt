package com.iammert.easymapslib.location.geocoder

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import com.google.android.gms.maps.model.LatLng
import android.location.Geocoder
import android.util.Log
import com.google.android.gms.maps.GoogleMap
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.*
import java.util.concurrent.TimeUnit


class GeocoderController(context: Context) {

    private val appContext = context.applicationContext

    /**
     * Search from map (LatLng) subjects
     */
    private val latLongSubject = PublishSubject.create<LatLng>()

    private val addressSubject = PublishSubject.create<Address>()

    private var geocoderDisposable: Disposable? = null

    /**
     * Geocoder
     */
    private val geocoder = Geocoder(appContext, Locale.getDefault())

    init {
        observeLatLong()

    }

    @SuppressLint("CheckResult")
    private fun observeLatLong() {
        geocoderDisposable = latLongSubject
            .debounce(200, TimeUnit.MILLISECONDS)
            .flatMapSingle { getAddress(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ addressSubject.onNext(it) }, { })
    }

    fun updateAddress(latLong: LatLng) {
        latLongSubject.onNext(latLong)
    }

    fun getAddressObservable(): Observable<Address> = addressSubject

    fun destroy() {
        geocoderDisposable?.let {
            if (it.isDisposed.not()) {
                it.dispose()
            }
        }
    }

    fun getAddress(latLong: LatLng): Single<Address> {
        return Single.create {
            val addresses = geocoder.getFromLocation(latLong.latitude, latLong.longitude, 1)

            if (addresses.isNotEmpty()) {
                it.onSuccess(addresses[0])
            } else {
                it.onSuccess(Address(Locale.getDefault()))
            }
        }
    }
}