package com.rokoblak.blescanner.ui.screens.device.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.rokoblak.blescanner.ui.common.AppThemePreviews
import com.rokoblak.blescanner.ui.common.PreviewDataUtils
import com.rokoblak.blescanner.ui.screens.device.DeviceAction
import com.rokoblak.blescanner.ui.theme.BLEScannerTheme


data class CharacteristicDisplayData(
    val uuid: String,
    val content: String,
    val canRead: Boolean,
    val canNotify: Boolean,
)

@Composable
fun ChatacteristicDisplay(
    data: CharacteristicDisplayData,
    onAction: (DeviceAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var openDialog by remember {
        mutableStateOf(false)
    }
    Column(
        modifier = modifier
            .clickable {
                openDialog = true
            }
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            modifier = Modifier, text = data.uuid,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            modifier = Modifier,
            text = data.content,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelSmall,
        )

        if (openDialog) {
            Dialog(onDismissRequest = {
                openDialog = false
            }) {
                CharacteristicDialog(data = data, onAction = onAction, dismiss = {
                    openDialog = false
                })
            }
        }
    }
}

@AppThemePreviews
@Composable
private fun ChatacteristicDisplayPreview() {
    BLEScannerTheme {
        ChatacteristicDisplay(
            data = PreviewDataUtils.characteristic,
            onAction = {},
        )
    }
}
