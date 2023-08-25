package com.example.sample.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val repository = MainRepository(context)
    val composableScope = rememberCoroutineScope()
    var measureState by remember { mutableIntStateOf(0) }
    var location by remember { mutableStateOf("") }
    var job by remember { mutableStateOf<Job?>(null) }

    val locationSettingsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            // 測位処理を開始する
            measureState++
        } else {
            // 使用しないを選択されても、再度ダイアログを出す場合は、インクリメント、出さない場合は0をセットする
            measureState++
        }
    }

    LaunchedEffect(measureState) {
        if (measureState > 0) {
            job?.cancel()
            job = composableScope.launch {
                repository.checkLocationSettings(locationSettingsLauncher)
                    .onEach {
                        location = it.locationSettingsStates.toString()
                    }
                    .launchIn(this@launch)

                repository.startFetchLocation()
                    .onEach {
                        location = it.toString()
                    }
                    .launchIn(this)
            }
        } else if (measureState == 0) {
            location = "xxx"
            job?.cancel()
            job = null
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(measureState.toString())
        MainContent(
            location = location,
            measurementState = measureState > 0,
            onStart = { measureState++ },
            onStop = { measureState = 0 },
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
    measurementState: Boolean = false,
    onStart: () -> Unit = {},
    onStop: () -> Unit = {},
    navigateToSettingsScreen: () -> Unit = {}
) {
    var permissionStateCount by remember { mutableLongStateOf(0L) }
    val permissionState = rememberMultiplePermissionsState(listOf(
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    ))

    LaunchedEffect(permissionState, permissionStateCount) {
        if (permissionState.allPermissionsGranted && permissionStateCount > 0) {
            onStart()
        }
    }

    when {
        permissionState.allPermissionsGranted -> Unit
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
                permissionStateCount = 0
                onStop()
            }) {
                Text(text = "Stop")
            }
            Text(text = location)
        } else {
            Button(onClick = {
                permissionState.launchMultiplePermissionRequest()
                permissionStateCount++
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