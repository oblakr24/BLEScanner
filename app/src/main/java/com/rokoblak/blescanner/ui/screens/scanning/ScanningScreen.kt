package com.rokoblak.blescanner.ui.screens.scanning

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import com.rokoblak.blescanner.BuildConfig
import com.rokoblak.blescanner.di.AppScope
import com.rokoblak.blescanner.ui.screens.scanning.composables.ScanningScaffold
import kotlinx.coroutines.launch


@Composable
fun ScanningScreen(viewModel: ScanningViewModel = hiltViewModel()) {
    val state = viewModel.uiState.collectAsState().value

    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val areGranted = permissionsMap.values.reduce { acc, next -> acc && next }
        viewModel.handleAction(ScanningAction.PermissionsOrBTStateUpdated(deniedByUser = !areGranted))
    }

    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            viewModel.handleAction(ScanningAction.PermissionsOrBTStateUpdated())
        })

    val snackbarState = remember { SnackbarHostState() }
    val scaffoldState = rememberScaffoldState(
        snackbarHostState = snackbarState
    )

    Effects(viewModel, snackbarState)

    ScanningScaffold(
        state = state,
        scaffoldState = scaffoldState,
        onLaunchPermissions = {
            permissionsLauncher.launch(AppScope.blePermissions().toTypedArray())
        },
        onLaunchSettings = {
            val packageName = BuildConfig.APPLICATION_ID
            settingsLauncher.launch(
                Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    addCategory(Intent.CATEGORY_DEFAULT)
                    data = Uri.fromParts("package", packageName, null)
                }
            )
        },
        onLaunchSettingsForBTEnable = {
            settingsLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        },
        onAction = { act ->
            viewModel.handleAction(act)
        }
    )
}

@Composable
private fun Effects(viewModel: ScanningViewModel, snackbarState: SnackbarHostState) {
    val scope = rememberCoroutineScope()
    LaunchedEffect(viewModel) {
        viewModel.effects.consumeEvents { effect ->
            scope.launch {
                snackbarState.showSnackbar(effect.message ?: "Error: $effect")
            }
        }
    }
}