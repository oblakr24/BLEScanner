package com.rokoblak.blescanner.ui.screens.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.rokoblak.blescanner.ui.screens.device.DeviceRoute
import com.rokoblak.blescanner.ui.screens.scanning.ScanningRoute
import com.rokoblak.blescanner.ui.theme.BLEScannerTheme

data class MainScreenUIState(
    val isDarkTheme: Boolean?,
)

@Composable
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
    val state = viewModel.uiState.collectAsState(MainScreenUIState(isDarkTheme = null)).value

    val navController = rememberNavController()

    BLEScannerTheme(overrideDarkMode = state.isDarkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            MainNavHostContainer(navController)
        }
    }
}

@Composable
private fun MainNavHostContainer(navController: NavHostController) {
    NavHost(navController = navController, startDestination = ScanningRoute.route) {
        ScanningRoute.register(this, navController)
        DeviceRoute.register(this, navController)
    }
}
