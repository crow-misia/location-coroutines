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
import com.google.android.gms.location.DeviceOrientation
import com.google.android.gms.location.DeviceOrientationRequest
import com.google.android.gms.location.FusedOrientationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.Flow

interface FusedDeviceOrientationCoroutine {
    companion object {
        fun from(context: Context): FusedDeviceOrientationCoroutine {
            return from(
                orientationProvider = lazy {
                    LocationServices.getFusedOrientationProviderClient(context.applicationContext)
                },
            )
        }

        fun from(
            orientationProvider: FusedOrientationProviderClient,
        ): FusedDeviceOrientationCoroutine = from(
            orientationProvider = lazyOf(orientationProvider),
        )

        fun from(
            orientationProvider: Lazy<FusedOrientationProviderClient>,
        ): FusedDeviceOrientationCoroutine {
            return FusedDeviceOrientationCoroutineImpl(orientationProvider)
        }
    }

    fun getOrientationUpdates(request: DeviceOrientationRequest): Flow<DeviceOrientation>
}

inline fun FusedDeviceOrientationCoroutine.getOrientationUpdates(
    samplingPeriodMicros: Long,
    block: DeviceOrientationRequest.Builder.() -> Unit = { },
): Flow<DeviceOrientation> {
    return getOrientationUpdates(
        request = DeviceOrientationRequest.Builder(samplingPeriodMicros).apply(block).build(),
    )
}
