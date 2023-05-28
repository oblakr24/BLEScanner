package com.rokoblak.blescanner.ui.common

import com.rokoblak.blescanner.ui.screens.device.DeviceScreenUIState
import com.rokoblak.blescanner.ui.screens.device.composables.CharacteristicDisplayData
import com.rokoblak.blescanner.ui.screens.device.composables.LogDisplayData
import kotlinx.collections.immutable.toImmutableList

object PreviewDataUtils {

    val characteristic = CharacteristicDisplayData(
        uuid = "id-char-$1",
        content = "none",
        canRead = true,
        canNotify = true
    )

    val services = (0..10).map { sIdx ->
        DeviceScreenUIState.Device.ServiceItem("s-id-$sIdx", characteristics = (0..10).map { cIdx ->
            characteristic.copy(uuid = "id-char-$cIdx", content = "none")
        }.toImmutableList())
    }.toImmutableList()

    val logs = (0..10).map {
        LogDisplayData(id = "id-$it", timestamp = "15:45:$it", content = "content: $it")
    }.toImmutableList()

}