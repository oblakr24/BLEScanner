package com.rokoblak.blescanner.ui.screens.device

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.rokoblak.blescanner.ui.screens.device.composables.CharacteristicDisplayData
import com.rokoblak.blescanner.ui.screens.device.composables.DeviceScreenContent
import com.rokoblak.blescanner.ui.screens.device.composables.LogDisplayData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch


sealed interface DeviceScreenUIState {
    data class DeviceNotFound(val deviceAddress: String) : DeviceScreenUIState
    data class Device(
        val deviceName: String,
        val deviceAddress: String,
        val connectionState: String,
        val connecting: Boolean,
        val connected: Boolean,
        val logs: ImmutableList<LogDisplayData>,
        val services: ImmutableList<ServiceItem>,
    ) : DeviceScreenUIState {
        data class ServiceItem(
            val id: String,
            val characteristics: ImmutableList<CharacteristicDisplayData>
        )
    }
}

@Composable
fun DeviceScreen(viewModel: DeviceViewModel = hiltViewModel()) {
    val state = viewModel.uiState.collectAsState().value

    val snackState = remember { SnackbarHostState() }

    Effects(viewModel, snackState)

    Box(modifier = Modifier.fillMaxSize()) {
        DeviceScreenContent(state = state, onNavigateUp = {
            viewModel.navigateUp()
        }, onAction = { act ->
            viewModel.handleAction(act)
        })

        SnackbarHost(hostState = snackState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun Effects(viewModel: DeviceViewModel, snackbarState: SnackbarHostState) {
    val scope = rememberCoroutineScope()
    LaunchedEffect(viewModel) {
        viewModel.effects.consumeEvents { effect ->
            Log.e("rok1", "showing snackbar for $effect")
            scope.launch {
                snackbarState.showSnackbar(effect.message ?: "Error: $effect")
            }
        }
    }
}
