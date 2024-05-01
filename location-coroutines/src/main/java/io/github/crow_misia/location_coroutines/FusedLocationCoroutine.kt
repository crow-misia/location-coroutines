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
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.location.Location
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.DeviceOrientation
import com.google.android.gms.location.DeviceOrientationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LastLocationRequest
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

interface FusedLocationCoroutine {
    companion object {
        fun from(context: Context): FusedLocationCoroutine {
            return from(
                locationProvider = lazy { LocationServices.getFusedLocationProviderClient(context.applicationContext) },
                settings = lazy { LocationServices.getSettingsClient(context.applicationContext) },
                geofencing = lazy { LocationServices.getGeofencingClient(context.applicationContext) },
            )
        }

        fun from(
            locationProvider: FusedLocationProviderClient,
            settings: SettingsClient,
            geofencing: GeofencingClient,
        ): FusedLocationCoroutine = from(
            locationProvider = lazyOf(locationProvider),
            settings = lazyOf(settings),
            geofencing = lazyOf(geofencing),
        )

        fun from(
            locationProvider: Lazy<FusedLocationProviderClient>,
            settings: Lazy<SettingsClient>,
            geofencing: Lazy<GeofencingClient>,
        ): FusedLocationCoroutine {
            return FusedLocationCoroutineImpl(locationProvider, settings, geofencing)
        }
    }

    suspend fun checkLocationSettings(request: LocationSettingsRequest): LocationSettingsResponse

    suspend fun isGoogleLocationAccuracyEnabled(): Boolean

    fun checkLocationSettings(
        request: LocationSettingsRequest,
        activity: Activity,
        requestCode: Int,
    ): Flow<LocationSettingsResponse>

    fun checkLocationSettings(
        request: LocationSettingsRequest,
        launcher: ActivityResultLauncher<IntentSenderRequest>,
    ): Flow<LocationSettingsResponse>

    suspend fun isLocationAvailable(request: LocationSettingsRequest): Boolean

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    suspend fun getCurrentLocation(request: CurrentLocationRequest): Location?

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    suspend fun getLastLocation(): Location? = getLastLocation(null)

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    suspend fun getLastLocation(request: LastLocationRequest?): Location?

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    fun getLocationUpdates(request: LocationRequest): Flow<Location>

    suspend fun flushLocations()

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    suspend fun setMockMode(isMockMode: Boolean)

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    suspend fun setMockLocation(mockLocation: Location)

    suspend fun addGeofences(request: GeofencingRequest, pendingIntent: PendingIntent)

    suspend fun removeGeofences(requestIds: List<String>)

    suspend fun removeGeofences(pendingIntent: PendingIntent)
}

suspend inline fun FusedLocationCoroutine.checkLocationSettings(request: LocationRequest): LocationSettingsResponse {
    return checkLocationSettings { addLocationRequest(request) }
}

fun FusedLocationCoroutine.checkLocationSettings(
    request: LocationRequest,
    activity: Activity,
    requestCode: Int,
): Flow<LocationSettingsResponse> {
    return checkLocationSettings(activity, requestCode) { addLocationRequest(request) }
}

fun FusedLocationCoroutine.checkLocationSettings(
    request: LocationRequest,
    launcher: ActivityResultLauncher<IntentSenderRequest>,
): Flow<LocationSettingsResponse> {
    return checkLocationSettings(launcher) { addLocationRequest(request) }
}

suspend inline fun FusedLocationCoroutine.checkLocationSettings(
    builder: LocationSettingsRequest.Builder.() -> Unit,
): LocationSettingsResponse {
    return checkLocationSettings(LocationSettingsRequest.Builder().apply(builder).build())
}

inline fun FusedLocationCoroutine.checkLocationSettings(
    activity: Activity,
    requestCode: Int,
    builder: LocationSettingsRequest.Builder.() -> Unit,
): Flow<LocationSettingsResponse> {
    return checkLocationSettings(
        LocationSettingsRequest.Builder().apply(builder).build(),
        activity,
        requestCode
    )
}

inline fun FusedLocationCoroutine.checkLocationSettings(
    launcher: ActivityResultLauncher<IntentSenderRequest>,
    builder: LocationSettingsRequest.Builder.() -> Unit,
): Flow<LocationSettingsResponse> {
    return checkLocationSettings(LocationSettingsRequest.Builder().apply(builder).build(), launcher)
}

@RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
inline fun FusedLocationCoroutine.getLocationUpdates(
    priority: Int = Priority.PRIORITY_BALANCED_POWER_ACCURACY,
    interval: Duration,
    block: LocationRequest.Builder.() -> Unit = { },
): Flow<Location> {
    return getLocationUpdates(
        request = LocationRequest.Builder(priority, interval.inWholeMilliseconds).apply(block)
            .build(),
    )
}

@RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
inline fun FusedLocationCoroutine.getLocationUpdates(
    priority: Int = Priority.PRIORITY_BALANCED_POWER_ACCURACY,
    intervalMillis: Long,
    block: LocationRequest.Builder.() -> Unit = { },
): Flow<Location> {
    return getLocationUpdates(
        request = LocationRequest.Builder(priority, intervalMillis).apply(block).build(),
    )
}

@RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
suspend inline fun FusedLocationCoroutine.getCurrentLocation(
    block: CurrentLocationRequest.Builder.() -> Unit = { },
): Location? {
    return getCurrentLocation(
        request = CurrentLocationRequest.Builder().apply(block).build(),
    )
}

@RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
suspend inline fun FusedLocationCoroutine.getLastLocation(
    block: LastLocationRequest.Builder.() -> Unit = { },
): Location? {
    return getLastLocation(
        request = LastLocationRequest.Builder().apply(block).build(),
    )
}

suspend inline fun FusedLocationCoroutine.addGeofences(
    pendingIntent: PendingIntent,
    block: GeofencingRequest.Builder.() -> Unit = { },
) {
    return addGeofences(GeofencingRequest.Builder().apply(block).build(), pendingIntent)
}
