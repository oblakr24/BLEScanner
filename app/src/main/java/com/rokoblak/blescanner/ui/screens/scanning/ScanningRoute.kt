package com.rokoblak.blescanner.ui.screens.scanning

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.rokoblak.blescanner.ui.navigation.NavRoute

object ScanningRoute : NavRoute<ScanningViewModel> {

    override val route = "scan/"

    @Composable
    override fun viewModel(): ScanningViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: ScanningViewModel) = ScanningScreen(viewModel)
}