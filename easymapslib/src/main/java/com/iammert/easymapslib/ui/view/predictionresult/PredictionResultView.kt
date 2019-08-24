package com.iammert.easymapslib.ui.view.predictionresult

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.iammert.easymapslib.location.places.AddressInfo
import com.iammert.easymapslib.util.SimpleAnimatorListener
import androidx.core.content.ContextCompat.getSystemService


class PredictionResultView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val predictionsAdapter = PredictionResultAdapter()

    private val recyclerViewPredictions: RecyclerView = RecyclerView(context)

    private var onShowListener: (() -> Unit)? = null

    private var onHideListener: (() -> Unit)? = null

    private val invisibleAnimator = ObjectAnimator
        .ofFloat(this, "alpha", 1f, 0f)
        .apply {
            addListener(object : SimpleAnimatorListener() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    visibility = View.INVISIBLE
                }
            })
            duration = 300
        }

    private val visibleAnimator = ObjectAnimator
        .ofFloat(this, "alpha", 0f, 1f)
        .apply {
            addListener(object : SimpleAnimatorListener() {
                override fun onAnimationStart(animation: Animator?) {
                    super.onAnimationStart(animation)
                    visibility = View.VISIBLE
                }
            })
            duration = 300
        }

    init {
        recyclerViewPredictions.layoutManager = LinearLayoutManager(context, VERTICAL, false)
        recyclerViewPredictions.adapter = predictionsAdapter

        hideKeyboardOnScroll()

        addView(recyclerViewPredictions)

        visibility = View.INVISIBLE
    }

    fun setPredictions(predictions: List<AutocompletePrediction>) {
        predictionsAdapter.setPredictions(predictions)
    }

    fun setOnItemClicked(onItemClicked: (AutocompletePrediction) -> Unit) {
        predictionsAdapter.onItemClicked = onItemClicked
    }

    fun setOnShowListener(onShowListener: () -> Unit) {
        this.onShowListener = onShowListener
    }

    fun setOnHideListener(onHideListener: () -> Unit) {
        this.onHideListener = onHideListener
    }

    fun show() {
        visibleAnimator.start()
        this.onShowListener?.invoke()
    }

    fun hide() {
        invisibleAnimator.start()
        this.onHideListener?.invoke()
    }

    fun isShowing(): Boolean = visibility == View.VISIBLE

    @SuppressLint("ClickableViewAccessibility")
    private fun hideKeyboardOnScroll() {
        recyclerViewPredictions.setOnTouchListener { v, event ->
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm!!.hideSoftInputFromWindow(v.windowToken, 0)
            false
        }
    }
}
