package com.example.sample.ui

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationRequest
import io.github.crow_misia.location_coroutines.FusedLocationCoroutine
import io.github.crow_misia.location_coroutines.getLocationUpdates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

class MainRepository(context: Context) {
    private var locationFlow: SharedFlow<Location>? = null
    private val locationProviderClient = FusedLocationCoroutine.from(context)

    @SuppressLint("MissingPermission")
    fun startFetchLocation(scope: CoroutineScope): Flow<Location> {
        val flow = locationFlow ?: run {
            locationProviderClient.getLocationUpdates {
                interval = 1000L
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }.shareIn(scope, SharingStarted.WhileSubscribed(), 1).also {
                locationFlow = it
            }
        }
        return flow
    }

}