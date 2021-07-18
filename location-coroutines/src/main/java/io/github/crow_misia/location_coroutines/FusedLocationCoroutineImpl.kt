package io.github.crow_misia.location_coroutines

import android.Manifest
import android.app.PendingIntent
import android.location.Location
import android.os.Looper
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
    @ExperimentalCoroutinesApi
    override suspend fun checkLocationSettings(request: LocationSettingsRequest): LocationSettingsResponse {
        return settings.value.checkLocationSettings(request).await()
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    override suspend fun isLocationAvailable(request: LocationSettingsRequest): Boolean {
        return locationProvider.value.locationAvailability.await().isLocationAvailable
    }

    @ExperimentalCoroutinesApi
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    override suspend fun getCurrentLocation(priority: Int): Location? {
        val cancellationTokenSource = CancellationTokenSource()

        return locationProvider.value.getCurrentLocation(priority, cancellationTokenSource.token).await(cancellationTokenSource)
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    override suspend fun getLastLocation(): Location? {
        return locationProvider.value.lastLocation.await()
    }

    @ExperimentalCoroutinesApi
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    override fun getLocationUpdates(request: LocationRequest): Flow<Location> = callbackFlow {
        val looper = Looper.myLooper() ?: error("Can't create handler inside thread that has not called Looper.prepare()")

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach {
                    trySend(it).isSuccess
                }
            }
        }

        locationProvider.value.requestLocationUpdates(request, callback, looper).await()
        awaitClose {
            locationProvider.value.removeLocationUpdates(callback)
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
