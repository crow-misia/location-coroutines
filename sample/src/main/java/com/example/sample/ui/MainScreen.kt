package com.example.sample.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val repository = MainRepository(context)
    val composableScope = rememberCoroutineScope()
    var location by remember { mutableStateOf("") }
    var job by remember { mutableStateOf<Job?>(null) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        MainContent(
            location = location,
            onStart = {
                job?.cancel()
                job = composableScope.launch {
                    repository.startFetchLocation()
                        .onEach {
                            location = it.toString()
                        }
                        .launchIn(this)
                }
            },
            onStop = {
                location = "xxx"
                job?.cancel()
                job = null
            },
            navigateToSettingsScreen = {
                context.openSettings()
            }
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Preview
@Composable
fun MainContent(
    location: String = "xxxx",
    onStart: () -> Unit = {},
    onStop: () -> Unit = {},
    navigateToSettingsScreen: () -> Unit = {}
) {
    var startState by remember { mutableStateOf(false) }
    var measurementState by remember { mutableStateOf(false) }
    val permissionState = rememberMultiplePermissionsState(listOf(
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    ))

    when {
        permissionState.allPermissionsGranted -> {
            if (startState && !measurementState) {
                measurementState = true
                onStart()
            }
        }
        permissionState.shouldShowRationale -> {
            Column {
                Text("permission is required")
            }
        }
        else -> {
            Column {
                Text("permission denied:" + permissionState.revokedPermissions.size)
            }
            Button(onClick = navigateToSettingsScreen) {
                Text("Open Settings")
            }
        }
    }
    Column {
        if (measurementState) {
            Button(onClick = {
                onStop()
                startState = false
                measurementState = false
            }) {
                Text(text = "Stop")
            }
            Text(text = location)
        } else {
            Button(onClick = {
                startState = true
                permissionState.launchMultiplePermissionRequest()
            }) {
                Text(text = "Start")
            }
            Text(text = location)
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