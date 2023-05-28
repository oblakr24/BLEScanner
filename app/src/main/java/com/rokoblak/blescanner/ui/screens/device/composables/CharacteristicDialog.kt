package com.rokoblak.blescanner.ui.screens.device.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.rokoblak.blescanner.ui.common.AppThemePreviews
import com.rokoblak.blescanner.ui.common.PreviewDataUtils
import com.rokoblak.blescanner.ui.common.composables.ButtonWithIcon
import com.rokoblak.blescanner.ui.screens.device.DeviceAction
import com.rokoblak.blescanner.ui.theme.BLEScannerTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacteristicDialog(
    data: CharacteristicDisplayData,
    onAction: (DeviceAction) -> Unit,
    dismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .shadow(8.dp)
            .wrapContentSize()
            .background(
                MaterialTheme.colorScheme.background,
                RoundedCornerShape(8.dp)
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = data.uuid,
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 12.dp)
                .widthIn(20.dp, 220.dp),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium,
        )

        var input by remember {
            mutableStateOf("Enter value")
        }

        TextField(value = input,
            modifier = Modifier.padding(8.dp),
            textStyle = MaterialTheme.typography.labelSmall,
            onValueChange = {
                input = it
            })
        Spacer(modifier = Modifier.height(8.dp))
        ButtonWithIcon("Write", Icons.Filled.Edit) {
            onAction(DeviceAction.WriteCharacteristic(data.uuid, input))
            dismiss()
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (data.canRead) {
            ButtonWithIcon("Read", Icons.Filled.Send) {
                onAction(DeviceAction.ReadCharacteristic(data.uuid))
                dismiss()
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        if (data.canNotify) {
            ButtonWithIcon("Notify", Icons.Filled.Notifications) {
                onAction(DeviceAction.NotifyToCharacteristic(data.uuid))
                dismiss()
            }
        }
    }
}

@AppThemePreviews
@Composable
private fun CharacteristicDialogPreview() {
    BLEScannerTheme {
        CharacteristicDialog(data = PreviewDataUtils.characteristic, onAction = {}, dismiss = {})
    }
}
