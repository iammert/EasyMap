package com.iammert.easymapslib.ui.view

import android.content.Context
import android.graphics.Point
import android.graphics.RectF
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.annotation.DimenRes
import com.google.android.gms.maps.GoogleMap
import com.iammert.easymapslib.R
import com.iammert.easymapslib.util.*

class LocationMarkerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr),
    GoogleMap.OnCameraIdleListener,
    GoogleMap.OnCameraMoveStartedListener {

    private var isFloatingMode = true

    private var markerTargetYPosition: Float = 0f

    private val parentView = LayoutInflater.from(context).inflate(R.layout.view_location_marker, this, true)

    private val markerView = parentView.findViewById<ImageView>(R.id.markerView)

    private var indicatorView = parentView.findViewById<View>(R.id.markerViewIndicator)

    private var mapViewScreenRectF: RectF = RectF()

    private var bottomSheetExpandedHeight: Int = 0

    private var bottomSheetCollapsedHeight: Int = 0

    override fun onCameraIdle() {
        if (isFloatingMode) {
            settle()
        }
    }

    override fun onCameraMoveStarted(p0: Int) {
        if (isFloatingMode) {
            move()
        }
    }

    fun initialize(mapFragmentView: View?, @DimenRes bottomSheetExpandedHeight: Int, @DimenRes bottomSheetCollapsedHeight: Int) {
        this.bottomSheetExpandedHeight = resources.getDimensionPixelSize(bottomSheetExpandedHeight)
        this.bottomSheetCollapsedHeight = resources.getDimensionPixelSize(bottomSheetCollapsedHeight)

        mapFragmentView?.afterMeasured {
            val mapViewLocation = IntArray(2)
            getLocationOnScreen(mapViewLocation)
            mapViewScreenRectF = RectF(
                mapViewLocation[0].toFloat(),
                mapViewLocation[1].toFloat(),
                mapViewLocation[0].toFloat() + this.measuredWidth,
                mapViewLocation[1].toFloat() + this.measuredHeight
            )

            calculateTargetPosition()

            afterMeasured { updateMarkerViewPosition(0f) }
        }
    }

    fun updateMarkerViewPosition(slideOffset: Float) {
        val mapCenterX = mapViewScreenRectF.width() / 2
        val mapCenterY = mapViewScreenRectF.height() / 2

        val totalDifference = mapCenterY - markerTargetYPosition
        val calculatedDifference = totalDifference * slideOffset
        val markerTopMargin = mapCenterY - calculatedDifference - measuredHeight

        val markerLeftMargin = mapCenterX - (measuredWidth / 2)

        updateMargins(left = markerLeftMargin.toInt(), top = markerTopMargin.toInt())
    }

    fun getMarkerPoint(): Point {
        if (measuredWidth == 0 || measuredHeight == 0) {
            return Point(0, 0)
        }

        val locationInScreen = IntArray(2)
        getLocationOnScreen(locationInScreen)
        val pointX = locationInScreen[0].toFloat() + (measuredWidth / 2f)
        val pointY = (locationInScreen[1].toFloat() + measuredHeight) - mapViewScreenRectF.top
        return Point(pointX.toInt(), pointY.toInt())
    }

    fun getExpandedCameraPoint(): Point {
        val mapCenterY = mapViewScreenRectF.height() / 2
        val totalDifference = mapCenterY - markerTargetYPosition

        val expandedCameraPositionX = (mapViewScreenRectF.width() / 2).toInt()
        val expandedCameraPositionY = (mapCenterY + totalDifference).toInt()

        return Point(expandedCameraPositionX, expandedCameraPositionY)
    }

    fun getCollapsedCameraPoint(): Point {
        val mapCenterY = mapViewScreenRectF.height() / 2
        val totalDifference = mapCenterY - markerTargetYPosition

        val expandedCameraPositionX = (mapViewScreenRectF.width() / 2).toInt()
        val expandedCameraPositionY = (mapCenterY - totalDifference).toInt()

        return Point(expandedCameraPositionX, expandedCameraPositionY)
    }

    fun setFloatingOnMove(isFloatingMode: Boolean) {
        this.isFloatingMode = isFloatingMode
    }

    private fun move() {
        markerView.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(100)
            .translationY(-80f)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        indicatorView.animate()
            .alpha(1f)
            .setDuration(100)
            .setDuration(100)
            .start()
    }

    private fun settle() {
        markerView.animate()
            .scaleX(1f)
            .scaleY(1f)
            .translationY(0f)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setDuration(100)
            .start()

        indicatorView.animate()
            .alpha(0f)
            .setDuration(100)
            .start()
    }

    private fun calculateTargetPosition() {
        markerTargetYPosition =
            (mapViewScreenRectF.height() - (bottomSheetExpandedHeight - bottomSheetCollapsedHeight)) / 2
    }
}