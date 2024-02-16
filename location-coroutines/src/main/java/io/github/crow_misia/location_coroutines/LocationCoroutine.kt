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
import android.content.Context
import android.location.GnssMeasurementsEvent
import android.location.Location
import android.location.LocationManager
import android.os.Build.VERSION_CODES
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.content.getSystemService
import androidx.core.location.GnssStatusCompat
import androidx.core.location.LocationRequestCompat
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

interface LocationCoroutine {
    companion object {
        fun from(context: Context): LocationCoroutine {
            return LocationCoroutineImpl(
                locationManager = lazy { requireNotNull(context.getSystemService<LocationManager>()) },
            )
        }

        fun from(manager: LocationManager): LocationCoroutine {
            return LocationCoroutineImpl(
                locationManager = lazyOf(manager),
            )
        }

        fun from(locationManager: Lazy<LocationManager>): LocationCoroutine {
            return LocationCoroutineImpl(locationManager)
        }
    }

    fun isLocationEnabled(): Boolean

    fun hasProvider(provider: String): Boolean

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    suspend fun getCurrentLocation(provider: String): Location?

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    fun getLocationUpdates(provider: String, request: LocationRequestCompat): Flow<Location>

    fun getGnssHardwareModelName(): String?

    fun getGnssYearOfHardware(): Int

    @RequiresApi(VERSION_CODES.N)
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun getGnssMeasurements(): Flow<GnssMeasurementsEvent>

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun getGnssStatus(): Flow<GnssStatusCompat>
}


@RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
inline fun LocationCoroutine.getLocationUpdates(
    provider: String,
    interval: Duration,
    block: LocationRequestCompat.Builder.() -> Unit = { },
): Flow<Location> {
    return getLocationUpdates(
        provider = provider,
        request = LocationRequestCompat.Builder(interval.inWholeMilliseconds).apply(block)
            .build(),
    )
}

@RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
inline fun LocationCoroutine.getLocationUpdates(
    provider: String,
    intervalMillis: Long,
    block: LocationRequestCompat.Builder.() -> Unit = { },
): Flow<Location> {
    return getLocationUpdates(
        provider = provider,
        request = LocationRequestCompat.Builder(intervalMillis).apply(block).build(),
    )
}
