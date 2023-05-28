package com.rokoblak.blescanner.ui.screens.scanning.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rokoblak.blescanner.R
import com.rokoblak.blescanner.ui.common.composables.ButtonWithIcon
import com.rokoblak.blescanner.ui.common.verticalScrollbar
import com.rokoblak.blescanner.ui.screens.scanning.ScanningAction
import com.rokoblak.blescanner.ui.theme.BLEScannerTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf


data class ScanningContentUIState(
    val scanning: Boolean,
    val foundItems: ImmutableList<ScannedDeviceDisplayData>,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScanningContent(
    state: ScanningContentUIState,
    onAction: (ScanningAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "Scanning: ${state.scanning}", modifier = Modifier.padding(16.dp))
        Spacer(modifier = Modifier.height(16.dp))
        if (state.scanning) {
            ButtonWithIcon(stringResource(R.string.scan_stop), icon = Icons.Filled.Stop) {
                onAction(ScanningAction.StopScan)
            }
        } else {
            ButtonWithIcon(stringResource(R.string.scan), icon = Icons.Filled.Bluetooth) {
                onAction(ScanningAction.StartScan)
            }
        }
        val lazyListState = rememberLazyListState()
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.verticalScrollbar(lazyListState)
        ) {
            items(
                count = state.foundItems.size,
                key = { state.foundItems[it].id },
                itemContent = { idx ->
                    val item = state.foundItems[idx]

                    ScannedDeviceDisplay(
                        data = item,
                        modifier = Modifier
                            .clickable {
                                onAction(ScanningAction.OpenDevice(address = item.address))
                            }
                            .animateItemPlacement(),
                    )
                    if (idx < state.foundItems.lastIndex) {
                        Divider(color = MaterialTheme.colorScheme.primary, thickness = 1.dp)
                    }
                }
            )
        }
    }
}

@Preview
@Composable
private fun ScanningContentPreview() {
    BLEScannerTheme {
        val state = ScanningContentUIState(scanning = true, foundItems = persistentListOf())
        ScanningContent(state = state, onAction = {})
    }
}
