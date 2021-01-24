package com.example.maskdetector.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.tensorflow.lite.support.label.Category

class DetectorListViewModel : ViewModel() {

    // This is a LiveData field. Choosing this structure because the whole list tend to be updated
    // at once in ML and not individual elements. Updating this once for the entire list makes
    // sense.
    private val _detectionList = MutableLiveData<List<Detector>>()
    val detectionList: LiveData<List<Detector>> = _detectionList

    fun updateData(detections: List<Detector>){
        _detectionList.postValue(detections)
    }

    fun updateStaticData(detections: MutableList<Category>) {
        val detection = Detector(detections[0].label, detections[0].score)
        val detectionList: List<Detector> = listOf(detection)

        _detectionList.postValue(detectionList)
    }

    fun clearData() {
        _detectionList.value = null
    }
}

/**
 * Simple Data object with two fields for the label and probability
 */
data class Detector(val label:String, val confidence:Float) {

    // For easy logging
    override fun toString():String{
        return "$label / $probabilityString"
    }

    // Output probability as a string to enable easy data binding
    val probabilityString = String.format("%.1f%%", confidence * 100.0f)

}