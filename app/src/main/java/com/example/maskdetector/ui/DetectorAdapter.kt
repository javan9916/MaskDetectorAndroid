package com.example.maskdetector.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.maskdetector.viewmodel.Detector
import com.example.maskdetector.databinding.DetectorItemBinding

class DetectorAdapter(private val ctx: Context) :
    ListAdapter<Detector, DetectorViewHolder>(RecognitionDiffUtil()) {

    /**
     * Inflating the ViewHolder with detector_item layout and data binding
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetectorViewHolder {
        val inflater = LayoutInflater.from(ctx)
        val binding = DetectorItemBinding.inflate(inflater, parent, false)
        return DetectorViewHolder(binding)
    }

    // Binding the data fields to the RecognitionViewHolder
    override fun onBindViewHolder(holder: DetectorViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    private class RecognitionDiffUtil : DiffUtil.ItemCallback<Detector>() {
        override fun areItemsTheSame(oldItem: Detector, newItem: Detector): Boolean {
            return oldItem.label == newItem.label
        }

        override fun areContentsTheSame(oldItem: Detector, newItem: Detector): Boolean {
            return oldItem.confidence == newItem.confidence
        }
    }
}

class DetectorViewHolder(private val binding: DetectorItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    // Binding all the fields to the view - to see which UI element is bind to which field, check
    // out layout/recognition_item.xml
    fun bindTo(recognition: Detector) {
        binding.detectorItem = recognition
        binding.executePendingBindings()
    }
}