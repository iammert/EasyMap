package com.iammert.easymapslib.ui.view.predictionresult

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.iammert.easymapslib.R

class PredictionResultAdapter : RecyclerView.Adapter<PredictionResultAdapter.PredictionResultItemViewHolder>() {

    var onItemClicked: ((AutocompletePrediction) -> Unit)? = null

    private val predictions: ArrayList<AutocompletePrediction> = arrayListOf()

    fun setPredictions(predictions: List<AutocompletePrediction>) {
        this.predictions.clear()
        this.predictions.addAll(predictions)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = predictions.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PredictionResultItemViewHolder =
        PredictionResultItemViewHolder.create(parent, onItemClicked)

    override fun onBindViewHolder(holder: PredictionResultItemViewHolder, position: Int) =
        holder.bind(predictions[position])

    class PredictionResultItemViewHolder(private val view: View, onItemClicked: ((AutocompletePrediction) -> Unit)?) :
        RecyclerView.ViewHolder(view) {

        private val textViewAddressTitle: AppCompatTextView = view.findViewById(R.id.textViewTitle)
        private val textViewAddressDetail: AppCompatTextView = view.findViewById(R.id.textViewSubtitle)

        private var prediction: AutocompletePrediction? = null

        init {
            view.setOnClickListener {
                prediction?.let { onItemClicked?.invoke(it) }
            }
        }

        fun bind(prediction: AutocompletePrediction) {
            this.prediction = prediction
            textViewAddressTitle.text = prediction.getPrimaryText(null).toString()
            textViewAddressDetail.text = prediction.getFullText(null).toString()
        }

        companion object {

            fun create(
                parent: ViewGroup,
                onItemClicked: ((AutocompletePrediction) -> Unit)?
            ): PredictionResultItemViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_prediction_result, parent, false)
                return PredictionResultItemViewHolder(view, onItemClicked)
            }
        }

    }

}