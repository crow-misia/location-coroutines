package com.example.sample.ui

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.Priority
import io.github.crow_misia.location_coroutines.FusedLocationCoroutine
import io.github.crow_misia.location_coroutines.getLocationUpdates
import kotlinx.coroutines.flow.*

class MainRepository(context: Context) {
    private val locationProviderClient = FusedLocationCoroutine.from(context)

    @SuppressLint("MissingPermission")
    fun startFetchLocation(): Flow<Location> {
        return locationProviderClient.getLocationUpdates(
            intervalMillis = 1000L,
            priority = Priority.PRIORITY_HIGH_ACCURACY,
        ) {
            setWaitForAccurateLocation(true)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setMinUpdateIntervalMillis(500L)
            setMaxUpdateDelayMillis(5000L)
        }.onStart { locationProviderClient.flushLocations() }
    }
}