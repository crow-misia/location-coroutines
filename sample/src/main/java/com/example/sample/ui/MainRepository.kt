package com.example.sample.ui

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.core.location.LocationRequestCompat
import com.google.android.gms.location.DeviceOrientation
import com.google.android.gms.location.DeviceOrientationRequest
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import io.github.crow_misia.location_coroutines.FusedDeviceOrientationAdapter
import io.github.crow_misia.location_coroutines.FusedLocationAdapter
import io.github.crow_misia.location_coroutines.NativeDeviceOrientation
import io.github.crow_misia.location_coroutines.NativeDeviceOrientationAdapter
import io.github.crow_misia.location_coroutines.NativeDeviceOrientationRequest
import io.github.crow_misia.location_coroutines.NativeLocationAdapter
import io.github.crow_misia.location_coroutines.getLocations
import io.github.crow_misia.location_coroutines.getOrientationUpdates
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class MainRepository(context: Context) {
    private val fusedLocationAdapter = FusedLocationAdapter(context)
    private val nativeLocationAdapter = NativeLocationAdapter(context, LocationManager.GPS_PROVIDER)
    private val fusedOrientationAdapter = FusedDeviceOrientationAdapter(context)
    private val nativeOrientationAdapter = NativeDeviceOrientationAdapter(context)

    @SuppressLint("MissingPermission")
    fun startFusedLocation(): Flow<Location> {
        return fusedLocationAdapter.getLocations {
            LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 1000L)
                .setWaitForAccurateLocation(true)
                .setGranularity(Granularity.GRANULARITY_FINE)
                .build()
        }
    }

    @SuppressLint("MissingPermission")
    fun startNativeLocation(): Flow<Location> {
        return nativeLocationAdapter.getLocations {
            LocationRequestCompat.Builder(1000L)
                .setQuality(LocationRequestCompat.QUALITY_HIGH_ACCURACY)
                .setMinUpdateIntervalMillis(100L)
                .build()
        }
    }

    fun startFusedOrientation(): Flow<DeviceOrientation> {
        return fusedOrientationAdapter.getOrientationUpdates {
            DeviceOrientationRequest.Builder(100.milliseconds.inWholeMicroseconds)
                .build()
        }
    }

    fun startNativeOrientation(): Flow<NativeDeviceOrientation> {
        return nativeOrientationAdapter.getOrientationUpdates {
            NativeDeviceOrientationRequest(
                samplingPeriod = 1.seconds,
            )
        }
    }
}