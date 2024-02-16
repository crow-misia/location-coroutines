package com.example.sample.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import io.github.crow_misia.location_coroutines.toPlainString
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val repository = MainRepository(context)
    val composableScope = rememberCoroutineScope()
    var measureState by remember { mutableIntStateOf(0) }
    var state by remember { mutableStateOf("") }
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
        job?.cancel()
        job = composableScope.launch {
            val flow = when (measureState) {
                1 -> repository.checkLocationSettings(locationSettingsLauncher)
                    .take(1)
                    .mapNotNull { it.locationSettingsStates?.toPlainString() }
                    .onCompletion { measureState = 0 }
                2 -> repository.startFusedFetchLocation()
                    .map { it.toString() }
                3 -> repository.startNetworkFetchLocation()
                    .map { it.toString() }
                else -> null
            } ?: return@launch

            flow.onEach {
                state = it
            }.onStart {
                state = ""
            }.launchIn(this)
        }
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(measureState.toString())
        MainContent(
            state = state,
            measurementState = measureState > 0,
            onStart = { measureState = it },
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
    state: String = "xxxx",
    measurementState: Boolean = false,
    onStart: (state: Int) -> Unit = {},
    onStop: () -> Unit = {},
    navigateToSettingsScreen: () -> Unit = {}
) {
    var permissionStateCount by remember { mutableIntStateOf(0) }
    val permissionState = rememberMultiplePermissionsState(listOf(
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    ))

    LaunchedEffect(permissionState, permissionStateCount) {
        if (permissionState.allPermissionsGranted && permissionStateCount > 0) {
            onStart(permissionStateCount)
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
            Text(text = state)
        } else {
            Button(onClick = {
                permissionState.launchMultiplePermissionRequest()
                permissionStateCount = 1
            }) {
                Text(text = "Get Settings")
            }
            Button(onClick = {
                permissionState.launchMultiplePermissionRequest()
                permissionStateCount = 2
            }) {
                Text(text = "Start Fused")
            }
            Button(onClick = {
                permissionState.launchMultiplePermissionRequest()
                permissionStateCount = 3
            }) {
                Text(text = "Start Network")
            }
            Text(text = state)
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