package com.example.sample.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val repository = MainRepository(context)
    val composableScope = rememberCoroutineScope()
    var fusedLocation by remember { mutableStateOf("") }
    var nativeLocation by remember { mutableStateOf("") }
    var fusedOrientation by remember { mutableStateOf("") }
    var nativeOrientation by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Row(Modifier.fillMaxWidth()) {
            Text("fusedLocation:")
            Text(fusedLocation)
        }
        Row(Modifier.fillMaxWidth()) {
            Text("nativeLocation:")
            Text(nativeLocation)
        }
        Row(Modifier.fillMaxWidth()) {
            Text("fusedOrientation:")
            Text(fusedOrientation)
        }
        Row(Modifier.fillMaxWidth()) {
            Text("nativeOrientation:")
            Text(nativeOrientation)
        }
        MainContent(
            onStartFusedLocation = {
                composableScope.launch(Dispatchers.Default) {
                    repository.startFusedLocation().onEach {
                        fusedLocation = it.toString()
                    }.launchIn(this)
                }
            },
            onStartNativeLocation = {
                composableScope.launch(Dispatchers.Default) {
                    repository.startNativeLocation().onEach {
                        nativeLocation = it.toString()
                    }.launchIn(this)
                }
            },
            onStartFusedOrientation = {
                composableScope.launch(Dispatchers.Default) {
                    repository.startFusedOrientation().onEach {
                        fusedOrientation = it.toString()
                    }.launchIn(this)
                }
            },
            onStartNativeOrientation = {
                composableScope.launch(Dispatchers.Default) {
                    repository.startNativeOrientation().onEach {
                        nativeOrientation = it.toString()
                    }.launchIn(this)
                }
            },
            navigateToSettingsScreen = {
                context.openSettings()
            }
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainContent(
    onStartFusedLocation: () -> Job,
    onStartNativeLocation: () -> Job,
    onStartFusedOrientation: () -> Job,
    onStartNativeOrientation: () -> Job,
    navigateToSettingsScreen: () -> Unit,
) {
    var fusedLocationJob by remember { mutableStateOf<Job?>(null) }
    var fusedOrientationJob by remember { mutableStateOf<Job?>(null) }
    var nativeLocationJob by remember { mutableStateOf<Job?>(null) }
    var nativeOrientationJob by remember { mutableStateOf<Job?>(null) }

    var locationStartType by remember { mutableIntStateOf(0) }
    val permissionState = rememberMultiplePermissionsState(listOf(
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    ))

    LaunchedEffect(permissionState, locationStartType) {
        if (permissionState.allPermissionsGranted && locationStartType > 0) {
            when (locationStartType) {
                1 -> {
                    fusedLocationJob = onStartFusedLocation()
                }
                2 -> {
                    nativeLocationJob = onStartNativeLocation()
                }
            }
        }
    }

    when {
        permissionState.allPermissionsGranted -> Unit
        permissionState.shouldShowRationale -> {
            Text("permission is required")
        }
        else -> {
            Row {
                Text("permission denied:" + permissionState.revokedPermissions.size)
                Button(onClick = navigateToSettingsScreen) {
                    Text("Open Settings")
                }
            }
        }
    }

    Row {
        fusedLocationJob?.also {
            Button(onClick = {
                fusedLocationJob = null
                locationStartType = 0
                it.cancel()
            }) {
                Text(text = "Stop Fused Location")
            }
        } ?: run {
            Button(onClick = {
                permissionState.launchMultiplePermissionRequest()
                locationStartType = 1
            }) {
                Text(text = "Start Fused Location")
            }
        }
    }
    Row {
        nativeLocationJob?.also {
            Button(onClick = {
                nativeLocationJob = null
                locationStartType = 0
                it.cancel()
            }) {
                Text(text = "Stop Native Location")
            }
        } ?: run {
            Button(onClick = {
                permissionState.launchMultiplePermissionRequest()
                locationStartType = 2
            }) {
                Text(text = "Start Native Location")
            }
        }
    }

    Row {
        fusedOrientationJob?.also {
            Button(onClick = {
                fusedOrientationJob = null
                it.cancel()
            }) {
                Text(text = "Stop Fused Orientation")
            }
        } ?: run {
            Button(onClick = {
                fusedOrientationJob = onStartFusedOrientation()
            }) {
                Text(text = "Start Fused Orientation")
            }
        }
    }
    Row {
        nativeOrientationJob?.also {
            Button(onClick = {
                nativeOrientationJob = null
                it.cancel()
            }) {
                Text(text = "Stop Native Orientation")
            }
        } ?: run {
            Button(onClick = {
                nativeOrientationJob = onStartNativeOrientation()
            }) {
                Text(text = "Start Native Orientation")
            }
        }
    }
}

fun Context.openSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val uri = Uri.fromParts("package",packageName,null)
    intent.data = uri
    startActivity(intent)
}