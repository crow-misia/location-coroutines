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

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

data class LocationRequest(
    val interval: Duration = 1.hours,
    val priority: Priority = Priority.BalancedPowerAccuracy,
    val granularity: Granularity = Granularity.PermissionLevel,
    val waitForAccurateLocation: Boolean = false,
    val duration: Duration? = null,
    val maxUpdates: Int? = null,
    val maxUpdateAge: Duration? = null,
    val maxUpdateDelay: Duration? = null,
    val minUpdateDistanceMeters: Float? = null,
    val minUpdateInterval: Duration? = null,
)

enum class Priority {
    HighAccuracy,
    BalancedPowerAccuracy,
    LowPower,
    Passive,
}

enum class Granularity {
    Fine,
    Coarse,
    PermissionLevel,
}
