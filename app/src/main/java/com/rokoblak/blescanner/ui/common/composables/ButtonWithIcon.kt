package com.rokoblak.blescanner.ui.common.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rokoblak.blescanner.ui.theme.BLEScannerTheme

@Composable
fun ButtonWithIcon(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        enabled = enabled, modifier = modifier,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.widthIn(80.dp, 320.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Icon ${icon.name}"
            )
            Text(text = text, maxLines = 1, modifier = Modifier.padding(start = 4.dp))
        }
    }
}

@Preview
@Composable
fun IconButtonPreviewShort() {
    BLEScannerTheme {
        ButtonWithIcon(text = "Short", icon = Icons.Filled.Settings) {}
    }
}

@Preview
@Composable
fun IconButtonPreviewLong() {
    BLEScannerTheme {
        ButtonWithIcon(text = "Long title", icon = Icons.Filled.Settings) {}
    }
}