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

import com.google.android.gms.location.DeviceOrientation
import com.google.android.gms.location.DeviceOrientationListener
import com.google.android.gms.location.DeviceOrientationRequest
import com.google.android.gms.location.FusedOrientationProviderClient
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.Executors

internal class FusedDeviceOrientationCoroutineImpl(
    private val orientationProvider: Lazy<FusedOrientationProviderClient>,
) : FusedDeviceOrientationCoroutine {
    override suspend fun getOrientationUpdates(
        request: DeviceOrientationRequest,
    ): Flow<DeviceOrientation> = channelFlow {
        val executor = Executors.newSingleThreadExecutor()
        val listener = DeviceOrientationListener {
            trySend(it)
        }

        val provider = orientationProvider.value
        provider.requestOrientationUpdates(request, executor, listener).await()
        awaitClose {
            provider.removeOrientationUpdates(listener)
                .addOnCompleteListener {
                    executor.shutdownNow()
                }
        }
    }
}
