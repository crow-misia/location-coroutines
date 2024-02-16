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

import com.google.android.gms.location.LocationSettingsStates

fun LocationSettingsStates.toPlainString(): String = buildString {
    append("LocationSettingsStates[usable={")
    sequence {
        if (isGpsUsable) {
            yield("gps")
        }
        if (isNetworkLocationUsable) {
            yield("network")
        }
        if (isBleUsable) {
            yield("ble")
        }
    }.joinTo(this)
    append("}, present={")
    sequence {
        if (isGpsPresent) {
            yield("gps")
        }
        if (isNetworkLocationPresent) {
            yield("network")
        }
        if (isBlePresent) {
            yield("ble")
        }
    }.joinTo(this)
    append("}]")
}
