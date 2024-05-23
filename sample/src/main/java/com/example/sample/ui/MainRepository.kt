package com.example.sample.ui

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import com.google.android.gms.location.DeviceOrientation
import io.github.crow_misia.location_coroutines.DeviceOrientationRequest
import io.github.crow_misia.location_coroutines.FusedDeviceOrientationAdapter
import io.github.crow_misia.location_coroutines.FusedLocationAdapter
import io.github.crow_misia.location_coroutines.Granularity
import io.github.crow_misia.location_coroutines.LocationRequest
import io.github.crow_misia.location_coroutines.NativeDeviceOrientation
import io.github.crow_misia.location_coroutines.NativeDeviceOrientationAdapter
import io.github.crow_misia.location_coroutines.NativeLocationAdapter
import io.github.crow_misia.location_coroutines.Priority
import io.github.crow_misia.location_coroutines.getLocations
import io.github.crow_misia.location_coroutines.getOrientationUpdates
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration
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
            LocationRequest(
                priority = Priority.HighAccuracy,
                interval = 1.seconds,
                minUpdateInterval = 100.milliseconds,
                waitForAccurateLocation = true,
                maxUpdateAge = Duration.ZERO,
                granularity = Granularity.PermissionLevel,
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun startNativeLocation(): Flow<Location> {
        return nativeLocationAdapter.getLocations {
            LocationRequest(
                priority = Priority.HighAccuracy,
                interval = 1.seconds,
                minUpdateInterval = 100.milliseconds,
                maxUpdateAge = Duration.ZERO,
            )
        }
    }

    fun startFusedOrientation(): Flow<DeviceOrientation> {
        return fusedOrientationAdapter.getOrientationUpdates {
            DeviceOrientationRequest()
        }
    }

    fun startNativeOrientation(): Flow<NativeDeviceOrientation> {
        return nativeOrientationAdapter.getOrientationUpdates {
            DeviceOrientationRequest()
        }
    }
}