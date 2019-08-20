package com.iammert.easymapslib.location.places

import com.google.android.libraries.places.api.model.AutocompletePrediction

data class SearchResultResource(
    val searchResultState: SearchResultState,
    val searchResult: List<AutocompletePrediction>,
    val throwable: Throwable?
) {

    companion object {

        fun loading() = SearchResultResource(
            searchResultState = SearchResultState.LOADING,
            searchResult = arrayListOf(),
            throwable = null
        )

        fun result(searchResult: List<AutocompletePrediction>) =
            SearchResultResource(
                searchResultState = SearchResultState.COMPLETE,
                searchResult = searchResult,
                throwable = null
            )

        fun error(throwable: Throwable?) =
            SearchResultResource(
                searchResultState = SearchResultState.ERROR,
                searchResult = arrayListOf(),
                throwable = throwable
            )
    }
}