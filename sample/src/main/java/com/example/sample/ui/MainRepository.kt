package com.example.sample.ui

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import io.github.crow_misia.location_coroutines.FusedLocationCoroutine
import io.github.crow_misia.location_coroutines.checkLocationSettings
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
            setWaitForAccurateLocation(false)
            setGranularity(Granularity.GRANULARITY_FINE)
        }
    }
    fun checkLocationSettings(launcher: ActivityResultLauncher<IntentSenderRequest>): Flow<LocationSettingsResponse> {
        return locationProviderClient.checkLocationSettings(launcher) {
            addLocationRequest(
                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
                    .setWaitForAccurateLocation(false)
                    .build()
            )
        }
    }
}