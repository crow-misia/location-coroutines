package com.example.sample.ui.main

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import io.github.crow_misia.location_coroutines.requestLocationUpdates
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private var locationFlow: SharedFlow<Location>? = null
    private val locationProviderClient = LocationServices.getFusedLocationProviderClient(application)

    val textView = MutableLiveData("")

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    fun onClick() {
        viewModelScope.launch {
            val flow = locationFlow ?: run {
                locationProviderClient.requestLocationUpdates {
                    interval = 1000
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                }.shareIn(this, SharingStarted.WhileSubscribed(), 1)
                 .also {
                     locationFlow = it
                 }
            }

            flow.collect {
                textView.postValue(it.toString())
            }
        }
    }

}