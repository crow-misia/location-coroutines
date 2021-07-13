package io.github.crow_misia.location_coroutines

import android.location.Location
import android.os.Looper
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

@ExperimentalCoroutinesApi
@RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"])
@JvmOverloads
fun FusedLocationProviderClient.requestLocationUpdates(
    sendLastLocation: Boolean = true,
    block: LocationRequest.() -> Unit,
): Flow<Location> {
    return requestLocationUpdates(LocationRequest.create().apply(block), sendLastLocation)
}

@ExperimentalCoroutinesApi
@RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"])
@JvmOverloads
fun FusedLocationProviderClient.requestLocationUpdates(
    request: LocationRequest,
    sendLastLocation: Boolean = true,
): Flow<Location> = callbackFlow {
    val looper = Looper.myLooper() ?: error("Can't create handler inside thread that has not called Looper.prepare()")

    val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.locations.forEach {
                trySend(it).isSuccess
            }
        }
    }

    // send last location
    if (sendLastLocation) {
        lastLocation.await()?.also { trySend(it) }
    }

    requestLocationUpdates(request, callback, looper).await()
    awaitClose {
        removeLocationUpdates(callback)
    }
}
