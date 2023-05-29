package com.rokoblak.blescanner.ui.screens.device

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.rokoblak.blescanner.ui.navigation.NavRoute
import com.rokoblak.blescanner.ui.navigation.getOrThrow

private const val KEY_ADDRESS = "key-address"
private const val KEY_NAME = "key-name"

object DeviceRoute : NavRoute<DeviceViewModel> {

    override val route =
        "device/{$KEY_ADDRESS}?$KEY_NAME={$KEY_NAME}"

    fun get(input: Input): String = route
        .replace("{$KEY_ADDRESS}", input.address)
        .replace("{$KEY_NAME}", input.name)

    fun getIdFrom(savedStateHandle: SavedStateHandle): Input {
        val address = savedStateHandle.getOrThrow<String>(KEY_ADDRESS)
        val name = savedStateHandle.getOrThrow<String>(KEY_NAME)
        return Input(address = address, name = name)
    }

    override fun getArguments(): List<NamedNavArgument> = listOf(
        navArgument(KEY_ADDRESS) { type = NavType.StringType },
    )

    @Composable
    override fun viewModel(): DeviceViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: DeviceViewModel) = DeviceScreen(viewModel)

    data class Input(
        val address: String,
        val name: String,
    )
}
