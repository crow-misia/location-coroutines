/**
 * Copyright (C) 2024 Zenichi Amano.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.crow_misia.location_coroutines

import android.Manifest
import android.location.GnssMeasurementsEvent
import android.location.Location
import android.location.LocationManager
import android.os.Build.VERSION_CODES
import android.os.CancellationSignal
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.location.GnssStatusCompat
import androidx.core.location.LocationListenerCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.location.LocationRequestCompat
import androidx.core.util.Consumer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.Executors

internal class LocationCoroutineImpl(
    private val locationManager: Lazy<LocationManager>,
) : LocationCoroutine {
    override fun isLocationEnabled(): Boolean {
        return LocationManagerCompat.isLocationEnabled(locationManager.value)
    }

    override fun hasProvider(provider: String): Boolean {
        return LocationManagerCompat.hasProvider(locationManager.value, provider)
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    override suspend fun getCurrentLocation(provider: String): Location? = callbackFlow {
        val executor = Executors.newSingleThreadExecutor()
        val signal = CancellationSignal()
        val consumer = Consumer<Location> {
            trySend(it)
            close()
        }
        LocationManagerCompat.getCurrentLocation(locationManager.value, provider, signal, executor, consumer)
        awaitClose {
            signal.cancel()
            executor.shutdownNow()
        }
    }.firstOrNull()

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    override fun getLocationUpdates(provider: String, request: LocationRequestCompat): Flow<Location> = channelFlow {
        val executor = Executors.newSingleThreadExecutor()
        val listener = LocationListenerCompat {
            trySend(it)
        }

        val manager = locationManager.value
        LocationManagerCompat.requestLocationUpdates(manager, provider, request, executor, listener)
        awaitClose {
            LocationManagerCompat.removeUpdates(manager, listener)
            executor.shutdownNow()
        }
    }

    override fun getGnssHardwareModelName(): String? {
        return LocationManagerCompat.getGnssHardwareModelName(locationManager.value)
    }

    override fun getGnssYearOfHardware(): Int {
        return LocationManagerCompat.getGnssYearOfHardware(locationManager.value)
    }

    @RequiresApi(VERSION_CODES.N)
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun getGnssMeasurements(): Flow<GnssMeasurementsEvent> = channelFlow {
        val executor = Executors.newSingleThreadExecutor()
        val callback = object : GnssMeasurementsEvent.Callback() {
            override fun onGnssMeasurementsReceived(eventArgs: GnssMeasurementsEvent) {
                trySend(eventArgs)
            }
        }

        val manager = locationManager.value
        LocationManagerCompat.registerGnssMeasurementsCallback(manager, executor, callback)
        awaitClose {
            LocationManagerCompat.unregisterGnssMeasurementsCallback(manager, callback)
            executor.shutdownNow()
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun getGnssStatus(): Flow<GnssStatusCompat> = channelFlow {
        val executor = Executors.newSingleThreadExecutor()
        val callback = object : GnssStatusCompat.Callback() {
            override fun onSatelliteStatusChanged(status: GnssStatusCompat) {
                trySend(status)
            }
        }

        val manager = locationManager.value
        LocationManagerCompat.registerGnssStatusCallback(manager, executor, callback)
        awaitClose {
            LocationManagerCompat.unregisterGnssStatusCallback(manager, callback)
            executor.shutdownNow()
        }
    }
}
