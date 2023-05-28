package com.rokoblak.blescanner.ui.screens.scanning.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rokoblak.blescanner.ui.common.AppThemePreviews
import com.rokoblak.blescanner.ui.common.composables.ButtonWithIcon
import com.rokoblak.blescanner.ui.screens.scanning.ScanningAction
import com.rokoblak.blescanner.ui.theme.BLEScannerTheme
import kotlinx.coroutines.launch

data class ScaffoldUIState(
    val drawer: ScanningDrawerUIState,
    val innerContent: InnerContent,
) {
    sealed interface InnerContent {
        object BTNotAvailable : InnerContent
        object BTNotEnabled : InnerContent
        object Initial : InnerContent
        data class NoPermissions(val shouldShowSettingsBtn: Boolean) : InnerContent
        data class Content(val content: ScanningContentUIState) : InnerContent
    }
}


@Composable
fun ScanningScaffold(
    state: ScaffoldUIState,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    onLaunchPermissions: () -> Unit,
    onLaunchSettings: () -> Unit,
    onLaunchSettingsForBTEnable: () -> Unit,
    onAction: (ScanningAction) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        topBar = {
            ScanningTopAppbar {
                coroutineScope.launch {
                    scaffoldState.drawerState.open()
                }
            }
        },
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                ScanningDrawer(state.drawer) {
                    coroutineScope.launch {
                        scaffoldState.drawerState.close()
                        onAction(it)
                    }
                }
            }
        }, content = {
            it.calculateBottomPadding()

            when (state.innerContent) {
                ScaffoldUIState.InnerContent.BTNotAvailable -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Bluetooth is not available on this device",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                ScaffoldUIState.InnerContent.BTNotEnabled -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Bluetooth is not enabled. Enable?",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        ButtonWithIcon("Enable Bluetooth", icon = Icons.Filled.Bluetooth) {
                            onLaunchSettingsForBTEnable()
                        }
                    }
                }

                is ScaffoldUIState.InnerContent.NoPermissions -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        val text =
                            "The app cannot list the bluetooth devices without the necessary permissions. Please grant the necessary permissions in order to enable this functionality."
                        Text(text, style = MaterialTheme.typography.labelMedium)

                        Spacer(modifier = Modifier.height(24.dp))

                        if (state.innerContent.shouldShowSettingsBtn) {
                            ButtonWithIcon("Open settings", icon = Icons.Filled.Settings) {
                                onLaunchSettings()
                            }
                        } else {
                            ButtonWithIcon("Grant permissions", icon = Icons.Filled.Bluetooth) {
                                onLaunchPermissions()
                            }
                        }
                    }
                }

                is ScaffoldUIState.InnerContent.Content -> {
                    ScanningContent(state = state.innerContent.content, onAction = onAction)
                }

                ScaffoldUIState.InnerContent.Initial -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    )
                }
            }
        })
}

@AppThemePreviews
@Composable
private fun ScanningScaffoldPreview() {
    val drawerOpenState = DrawerValue.Closed
    val drawerState = ScanningDrawerUIState(
        darkMode = true,
        btEnabled = true,
        btAvailable = true,
        permissionsGranted = true,
    )
    BLEScannerTheme {
        val scaffoldState = ScaffoldState(DrawerState(drawerOpenState), SnackbarHostState())
        ScanningScaffold(
            scaffoldState = scaffoldState,
            state = ScaffoldUIState(
                drawerState,
                innerContent = ScaffoldUIState.InnerContent.BTNotEnabled
            ),
            onLaunchPermissions = {},
            onLaunchSettings = {},
            onLaunchSettingsForBTEnable = {},
            onAction = {}
        )
    }
}

@AppThemePreviews
@Composable
private fun ListingScaffoldWithDrawerPreview() {
    val drawerOpenState = DrawerValue.Open
    val drawerState = ScanningDrawerUIState(
        darkMode = true,
        btEnabled = true,
        btAvailable = true,
        permissionsGranted = true,
    )
    BLEScannerTheme {
        val scaffoldState = ScaffoldState(DrawerState(drawerOpenState), SnackbarHostState())
        ScanningScaffold(
            scaffoldState = scaffoldState,
            state = ScaffoldUIState(
                drawerState,
                innerContent = ScaffoldUIState.InnerContent.BTNotEnabled
            ),
            onLaunchPermissions = {},
            onLaunchSettings = {},
            onLaunchSettingsForBTEnable = {},
            onAction = {}
        )
    }
}
