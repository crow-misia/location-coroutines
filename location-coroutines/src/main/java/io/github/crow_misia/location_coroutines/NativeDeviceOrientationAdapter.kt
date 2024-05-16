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
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import androidx.core.content.getSystemService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlin.time.Duration

class NativeDeviceOrientationAdapter(
    private val manager: SensorManager,
) : DeviceOrientationAdapter<NativeDeviceOrientationRequest, NativeDeviceOrientation> {
    constructor(
        context: Context,
    ) : this(checkNotNull(context.getSystemService<SensorManager>()) {
        "unsupported SensorManager"
    })

    override fun getOrientationUpdates(request: NativeDeviceOrientationRequest) = callbackFlow {
        val accelerometerReading = FloatArray(3)
        val magnetometerReading = FloatArray(3)
        val rotationMatrix = FloatArray(9)
        val orientationAngles = FloatArray(3)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    System.arraycopy(
                        event.values,
                        0,
                        accelerometerReading,
                        0,
                        accelerometerReading.size
                    )
                } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    System.arraycopy(
                        event.values,
                        0,
                        magnetometerReading,
                        0,
                        magnetometerReading.size
                    )
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
        }

        manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { sensor ->
            manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { sensor ->
            manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        var isRunning = true
        while (isRunning) {
            SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                accelerometerReading,
                magnetometerReading
            )
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            val result = NativeDeviceOrientation(
                azimuth = orientationAngles[0],
                pitch = orientationAngles[1],
                roll = orientationAngles[2],
                elapsedRealtimeNs = SystemClock.elapsedRealtimeNanos(),
            )
            val sendResult = trySend(result)
            if (sendResult.isClosed) {
                isRunning = false
            }
            delay(request.samplingPeriod)
        }

        awaitClose {
            manager.unregisterListener(listener)
        }
    }
}

data class NativeDeviceOrientationRequest(val samplingPeriod: Duration)
data class NativeDeviceOrientation(
    val azimuth: Float,
    val pitch: Float,
    val roll: Float,
    val elapsedRealtimeNs: Long,
)
