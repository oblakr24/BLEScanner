package com.rokoblak.blescanner.ui.screens.scanning.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rokoblak.blescanner.ui.common.AppThemePreviews
import com.rokoblak.blescanner.ui.theme.BLEScannerTheme


data class ScannedDeviceDisplayData(
    val id: String,
    val address: String,
    val deviceName: String,
)

@Composable
fun ScannedDeviceDisplay(data: ScannedDeviceDisplayData, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp)
        ) {
            Text(
                modifier = Modifier, text = data.id,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                modifier = Modifier,
                text = data.deviceName,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = "Show more options",
            modifier = Modifier.height(24.dp)
        )
    }
}

@AppThemePreviews
@Composable
private fun ScannedDeviceDisplayPreview() {
    BLEScannerTheme {
        ScannedDeviceDisplay(
            data = ScannedDeviceDisplayData(
                id = "id1",
                address = "address",
                deviceName = "device name",
            )
        )
    }
}
