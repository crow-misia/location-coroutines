/**
 * Copyright (C) 2021 Zenichi Amano.
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
import android.app.PendingIntent
import android.location.Location
import android.os.HandlerThread
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

internal class FusedLocationCoroutineImpl(
    private val locationProvider: Lazy<FusedLocationProviderClient>,
    private val settings: Lazy<SettingsClient>,
    private val geofencing: Lazy<GeofencingClient>,
) : FusedLocationCoroutine {
    override suspend fun checkLocationSettings(request: LocationSettingsRequest): LocationSettingsResponse {
        return settings.value.checkLocationSettings(request).await()
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    override suspend fun isLocationAvailable(request: LocationSettingsRequest): Boolean {
        return locationProvider.value.locationAvailability.await().isLocationAvailable
    }

    @ExperimentalCoroutinesApi
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    override suspend fun getCurrentLocation(request: CurrentLocationRequest): Location? {
        val cancellationTokenSource = CancellationTokenSource()

        return locationProvider.value.getCurrentLocation(request, cancellationTokenSource.token)
            .await(cancellationTokenSource)
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    override suspend fun getLastLocation(request: LastLocationRequest?): Location? {
        return request?.let {
            locationProvider.value.getLastLocation(it).await()
        } ?: run {
            locationProvider.value.lastLocation.await()
        }
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    override fun getLocationUpdates(request: LocationRequest): Flow<Location> = callbackFlow {
        val thread = HandlerThread("fusedLocationCoroutineImpl")
        thread.start()
        val looper = thread.looper

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    trySend(location).isSuccess
                }
            }
        }

        locationProvider.value.requestLocationUpdates(request, callback, looper).await()
        awaitClose {
            locationProvider.value.removeLocationUpdates(callback)
                .addOnCompleteListener {
                    thread.quit()
                }
        }
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    override suspend fun setMockMode(isMockMode: Boolean) {
        locationProvider.value.setMockMode(isMockMode).await()
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    override suspend fun setMockLocation(mockLocation: Location) {
        locationProvider.value.setMockLocation(mockLocation).await()
    }

    override suspend fun flushLocations() {
        locationProvider.value.flushLocations().await()
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    override suspend fun addGeofences(request: GeofencingRequest, pendingIntent: PendingIntent) {
        geofencing.value.addGeofences(request, pendingIntent).await()
    }

    override suspend fun removeGeofences(requestIds: List<String>) {
        geofencing.value.removeGeofences(requestIds).await()
    }

    override suspend fun removeGeofences(pendingIntent: PendingIntent) {
        geofencing.value.removeGeofences(pendingIntent).await()
    }
}
