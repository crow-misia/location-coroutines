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

import android.content.Context
import android.location.LocationManager
import androidx.annotation.RequiresPermission
import androidx.core.content.getSystemService
import androidx.core.location.LocationListenerCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.location.LocationRequestCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.Executors

class NativeLocationAdapter(
    private val manager: LocationManager,
    private val provider: String,
) : LocationAdapter {
    constructor(
        context: Context,
        provider: String,
    ) : this(checkNotNull(context.getSystemService<LocationManager>()) {
        "unsupported LocationManager"
    }, provider)

    override suspend fun flushLocations() {
        throw UnsupportedOperationException()
    }

    @RequiresPermission(
        anyOf = [
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_FINE_LOCATION",
        ],
    )
    override fun getLocations(request: LocationRequest) = callbackFlow {
        val executor = Executors.newSingleThreadExecutor()
        val listener = LocationListenerCompat {
            trySend(it)
        }
        LocationManagerCompat.requestLocationUpdates(
            manager, provider, request.asNativeRequest(), executor, listener,
        )

        awaitClose {
            LocationManagerCompat.removeUpdates(manager, listener)
            executor.shutdownNow()
        }
    }

    private fun LocationRequest.asNativeRequest(): LocationRequestCompat {
        return LocationRequestCompat.Builder(interval.inWholeMilliseconds).also {
            priority.setQuality(it)
            duration?.apply { it.setDurationMillis(inWholeMilliseconds) }
            maxUpdates?.apply { it.setMaxUpdates(this) }
            maxUpdateDelay?.apply { it.setMaxUpdateDelayMillis(inWholeMilliseconds) }
            minUpdateInterval?.apply { it.setMinUpdateIntervalMillis(inWholeMilliseconds) }
            minUpdateDistanceMeters?.apply { it.setMinUpdateDistanceMeters(this) }
        }.build()
    }

    private fun Priority.setQuality(builder: LocationRequestCompat.Builder) {
        when (this) {
            Priority.HighAccuracy -> builder.setQuality(LocationRequestCompat.QUALITY_HIGH_ACCURACY)
            Priority.BalancedPowerAccuracy -> builder.setQuality(LocationRequestCompat.QUALITY_BALANCED_POWER_ACCURACY)
            Priority.LowPower -> builder.setQuality(LocationRequestCompat.QUALITY_LOW_POWER)
            Priority.Passive -> {
                builder.setQuality(LocationRequestCompat.QUALITY_BALANCED_POWER_ACCURACY)
                builder.setIntervalMillis(LocationRequestCompat.PASSIVE_INTERVAL)
            }
        }
    }
}
