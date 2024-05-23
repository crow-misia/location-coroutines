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

import android.app.Activity
import android.content.Context
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.Executors

class FusedLocationAdapter(
    private val client: FusedLocationProviderClient,
) : LocationAdapter {
    constructor(context: Context) : this(LocationServices.getFusedLocationProviderClient(context))

    constructor(activity: Activity) : this(LocationServices.getFusedLocationProviderClient(activity))

    override suspend fun flushLocations() {
        client.flushLocations().await()
    }

    @RequiresPermission(
        anyOf = [
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_FINE_LOCATION",
        ],
    )
    override fun getLocations(request: LocationRequest) = callbackFlow {
        val executor = Executors.newSingleThreadExecutor()
        val listener = LocationListener {
            trySend(it)
        }
        client.requestLocationUpdates(request.asFusedRequest(), executor, listener).await()

        awaitClose {
            client.removeLocationUpdates(listener)
            executor.shutdownNow()
        }
    }
}

private fun LocationRequest.asFusedRequest(): com.google.android.gms.location.LocationRequest {
    return com.google.android.gms.location.LocationRequest.Builder(interval.inWholeMilliseconds).also {
        it.setPriority(priority.asFusedPriority())
        it.setGranularity(granularity.asFusedGranularity())
        it.setWaitForAccurateLocation(waitForAccurateLocation)
        duration?.apply { it.setDurationMillis(inWholeMilliseconds) }
        maxUpdates?.apply { it.setMaxUpdates(this) }
        maxUpdateAge?.apply { it.setMaxUpdateAgeMillis(inWholeMilliseconds) }
        maxUpdateDelay?.apply { it.setMaxUpdateDelayMillis(inWholeMilliseconds) }
        minUpdateInterval?.apply { it.setMinUpdateIntervalMillis(inWholeMilliseconds) }
        minUpdateDistanceMeters?.apply { it.setMinUpdateDistanceMeters(this) }
    }.build()
}

private fun Priority.asFusedPriority(): Int {
    return when (this) {
        Priority.HighAccuracy -> com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
        Priority.BalancedPowerAccuracy -> com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY
        Priority.LowPower -> com.google.android.gms.location.Priority.PRIORITY_LOW_POWER
        Priority.Passive -> com.google.android.gms.location.Priority.PRIORITY_PASSIVE
    }
}

private fun Granularity.asFusedGranularity(): Int {
    return when (this) {
        Granularity.Fine -> com.google.android.gms.location.Granularity.GRANULARITY_FINE
        Granularity.Coarse -> com.google.android.gms.location.Granularity.GRANULARITY_COARSE
        Granularity.PermissionLevel -> com.google.android.gms.location.Granularity.GRANULARITY_PERMISSION_LEVEL
    }
}