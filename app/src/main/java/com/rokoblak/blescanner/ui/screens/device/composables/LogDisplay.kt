package com.rokoblak.blescanner.ui.screens.device.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rokoblak.blescanner.ui.common.AppThemePreviews
import com.rokoblak.blescanner.ui.theme.BLEScannerTheme


data class LogDisplayData(
    val id: String,
    val timestamp: String,
    val content: String,
)

@Composable
fun LogDisplay(data: LogDisplayData, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            modifier = Modifier, text = data.timestamp,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            modifier = Modifier,
            text = data.content,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@AppThemePreviews
@Composable
private fun LogDisplayPreview() {
    BLEScannerTheme {
        LogDisplay(
            data = LogDisplayData(
                id = "id1",
                timestamp = "15:23:45",
                content = "Content of the log",
            ),
        )
    }
}
