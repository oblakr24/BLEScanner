package com.rokoblak.blescanner.ui.screens.device.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rokoblak.blescanner.R
import com.rokoblak.blescanner.ui.common.AppThemePreviews
import com.rokoblak.blescanner.ui.common.PreviewDataUtils
import com.rokoblak.blescanner.ui.common.composables.ButtonWithIcon
import com.rokoblak.blescanner.ui.common.composables.DetailsContent
import com.rokoblak.blescanner.ui.screens.device.DeviceAction
import com.rokoblak.blescanner.ui.screens.device.DeviceScreenUIState
import com.rokoblak.blescanner.ui.theme.BLEScannerTheme
import com.rokoblak.blescanner.ui.theme.alpha


@Composable
fun DeviceScreenContent(
    state: DeviceScreenUIState,
    onNavigateUp: () -> Unit,
    onAction: (DeviceAction) -> Unit
) {
    DetailsContent(title = "Device Status", onBackPressed = {
        onNavigateUp()
    }) {
        when (state) {
            is DeviceScreenUIState.DeviceNotFound -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.background),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Device not found: ${state.deviceAddress}. Please re-scan BLE devices.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.displayMedium,
                    )
                }
            }

            is DeviceScreenUIState.Device -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .background(MaterialTheme.colorScheme.background),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Connected: ${state.connected}",
                        modifier = Modifier.padding(16.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    if (state.connecting) {
                        Text(
                            text = "Connecting...",
                            modifier = Modifier.padding(16.dp)
                        )
                    } else if (state.connected) {
                        ButtonWithIcon(
                            stringResource(R.string.disconnect),
                            icon = Icons.Filled.Stop
                        ) {
                            onAction(DeviceAction.Disconnect)
                        }
                    } else {
                        ButtonWithIcon(
                            stringResource(R.string.connect),
                            icon = Icons.Filled.Bluetooth
                        ) {
                            onAction(DeviceAction.Connect)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    DeviceSessionStateContent(logs = state.logs, services = state.services, onAction = onAction)
                }
            }
        }
    }
}

@AppThemePreviews
@Composable
private fun DevicesScreenContentPreview() {
    BLEScannerTheme {
        val state = DeviceScreenUIState.Device(
            deviceName = "device name",
            deviceAddress = "device address",
            connectionState = "connected",
            logs = PreviewDataUtils.logs,
            services = PreviewDataUtils.services,
            connected = true,
            connecting = false,
        )
        DeviceScreenContent(state = state, onNavigateUp = {}, onAction = {})
    }
}
