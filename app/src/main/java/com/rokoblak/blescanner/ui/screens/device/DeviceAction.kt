package com.rokoblak.blescanner.ui.screens.device

sealed interface DeviceAction {
    object Connect: DeviceAction
    object Disconnect: DeviceAction
    data class ReadCharacteristic(val uuid: String): DeviceAction
    data class WriteCharacteristic(val uuid: String, val data: String): DeviceAction
    data class NotifyToCharacteristic(val uuid: String): DeviceAction
}