package com.rokoblak.blescanner.ui.screens.scanning

sealed interface ScanningAction {
    object StartScan: ScanningAction
    object StopScan: ScanningAction
    data class OpenDevice(val address: String): ScanningAction
    data class PermissionsOrBTStateUpdated(val deniedByUser: Boolean? = null) : ScanningAction
    data class SetDarkMode(val enabled: Boolean) : ScanningAction
}