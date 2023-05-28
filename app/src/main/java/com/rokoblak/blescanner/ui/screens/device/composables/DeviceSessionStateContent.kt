package com.rokoblak.blescanner.ui.screens.device.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rokoblak.blescanner.ui.common.AppThemePreviews
import com.rokoblak.blescanner.ui.common.PreviewDataUtils
import com.rokoblak.blescanner.ui.screens.device.DeviceAction
import com.rokoblak.blescanner.ui.screens.device.DeviceScreenUIState
import com.rokoblak.blescanner.ui.theme.BLEScannerTheme
import com.rokoblak.blescanner.ui.theme.alpha
import kotlinx.collections.immutable.ImmutableList


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeviceSessionStateContent(
    logs: ImmutableList<LogDisplayData>,
    services: ImmutableList<DeviceScreenUIState.Device.ServiceItem>,
    onAction: (DeviceAction) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),

        ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.5f)
        ) {
            items(count = logs.size, key = { logs[it].id }, itemContent = { idx ->
                LogDisplay(data = logs[idx])
                if (idx < logs.lastIndex) {
                    Divider(
                        color = MaterialTheme.colorScheme.onSurface.alpha(0.5f),
                        thickness = 1.dp
                    )
                }
            })
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(1f)
        ) {
            services.forEach { service ->
                stickyHeader {
                    Text(
                        text = "Service: ${service.id}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(8.dp)
                    )
                }
                val characteristics = service.characteristics
                items(
                    count = characteristics.size,
                    key = { characteristics[it].uuid },
                    itemContent = { idx ->
                        val item = characteristics[idx]
                        ChatacteristicDisplay(
                            item,
                            modifier = Modifier
                                .fillMaxWidth(),
                            onAction = onAction
                        )
                        if (idx < characteristics.lastIndex) {
                            Divider(
                                color = MaterialTheme.colorScheme.onSurface.alpha(0.5f),
                                thickness = 1.dp
                            )
                        }
                    }
                )
            }
        }
    }
}


@AppThemePreviews
@Composable
private fun DeviceSessionStateContentPreview() {
    BLEScannerTheme {
        DeviceSessionStateContent(
            logs = PreviewDataUtils.logs,
            services = PreviewDataUtils.services,
            onAction = {})
    }
}
