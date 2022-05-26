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
import kotlinx.coroutines.launch

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val repository = MainRepository(context)
    val composableScope = rememberCoroutineScope()
    var location by remember { mutableStateOf("") }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        MainContent(
            location = location,
            onClick = {
                composableScope.launch {
                    repository.startFetchLocation(this)
                        .collect {
                            location = it.toString()
                        }
                }
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
    onClick: () -> Unit = {},
    navigateToSettingsScreen: () -> Unit = {}
) {
    val permissionState = rememberMultiplePermissionsState(listOf(
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    ))

    when {
        permissionState.allPermissionsGranted -> onClick()
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
        Button(onClick = { permissionState.launchMultiplePermissionRequest() }) {
            Text(text = "Start")
        }
        Text(text = location)
    }
}

fun Context.openSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val uri = Uri.fromParts("package",packageName,null)
    intent.data = uri
    startActivity(intent)
}