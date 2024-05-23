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
import com.google.android.gms.location.DeviceOrientation
import com.google.android.gms.location.DeviceOrientationListener
import com.google.android.gms.location.FusedOrientationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.Executors

class FusedDeviceOrientationAdapter(
    private val client: FusedOrientationProviderClient,
) : DeviceOrientationAdapter<DeviceOrientation> {
    constructor(context: Context) : this(LocationServices.getFusedOrientationProviderClient(context))

    constructor(activity: Activity) : this(
        LocationServices.getFusedOrientationProviderClient(activity)
    )

    override fun getOrientationUpdates(request: DeviceOrientationRequest) = callbackFlow {
        val executor = Executors.newSingleThreadExecutor()
        val callback = DeviceOrientationListener {
            trySend(it)
        }

        client.requestOrientationUpdates(request.asFusedRequest(), executor, callback).await()

        awaitClose {
            client.removeOrientationUpdates(callback)
            executor.shutdown()
        }
    }

    private fun DeviceOrientationRequest.asFusedRequest(): com.google.android.gms.location.DeviceOrientationRequest {
        return com.google.android.gms.location.DeviceOrientationRequest.Builder(samplingPeriod.inWholeMicroseconds).build()
    }
}
