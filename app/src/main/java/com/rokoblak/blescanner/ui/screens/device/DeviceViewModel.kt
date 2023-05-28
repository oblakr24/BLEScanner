package com.rokoblak.blescanner.ui.screens.device

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rokoblak.blescan.connection.model.DeviceLog
import com.rokoblak.blescan.connection.model.Service
import com.rokoblak.blescanner.data.ScanningSession
import com.rokoblak.blescanner.data.repo.deviceconnection.DeviceConnectionRepository
import com.rokoblak.blescanner.ui.navigation.RouteNavigator
import com.rokoblak.blescanner.ui.screens.device.composables.CharacteristicDisplayData
import com.rokoblak.blescanner.ui.screens.device.composables.LogDisplayData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.nio.charset.StandardCharsets
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject


@HiltViewModel
class DeviceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val routeNavigator: RouteNavigator,
    session: ScanningSession,
    private val repo: DeviceConnectionRepository,
) : ViewModel(), RouteNavigator by routeNavigator {

    private val routeInput = DeviceRoute.getIdFrom(savedStateHandle)

    private val deviceMgr = session.getSessionManager(routeInput.address)

    val uiState = if (deviceMgr != null) {
        repo.state(deviceMgr, viewModelScope).map { session ->
            DeviceScreenUIState.Device(
                deviceName = deviceMgr.device.name(),
                deviceAddress = routeInput.address,
                connectionState = session?.connectionState ?: "-",
                connected = session?.connected ?: false,
                connecting = session?.connecting ?: false,
                logs = session?.logs?.mapLogsToUI() ?: persistentListOf(),
                services = session?.services?.mapServicesToUI() ?: persistentListOf(),
            )
        }
    } else {
        flowOf(DeviceScreenUIState.DeviceNotFound(routeInput.address))
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(1000),
        initialValue = DeviceScreenUIState.DeviceNotFound("")
    )

    val effects = repo.errors

    fun handleAction(act: DeviceAction) {
        when (act) {
            DeviceAction.Connect -> repo.connect()
            DeviceAction.Disconnect -> repo.disconnect()
            is DeviceAction.NotifyToCharacteristic -> repo.notifyToCharacteristic(act.uuid)
            is DeviceAction.ReadCharacteristic -> repo.readCharacteristic(act.uuid)
            is DeviceAction.WriteCharacteristic -> repo.writeCharacteristic(
                act.uuid,
                act.data.toByteArray(StandardCharsets.UTF_8)
            )
        }
    }

    private fun List<Service>.mapServicesToUI() = map { service ->
        DeviceScreenUIState.Device.ServiceItem(
            id = service.id,
            characteristics = service.characteristics.map {
                CharacteristicDisplayData(
                    uuid = it.characteristic.uuid.toString(),
                    content = "Read: ${it.read} | Notify: ${it.notify} | Indicate: ${it.indicate}",
                    canRead = it.read,
                    canNotify = it.notify,
                )
            }.toImmutableList()
        )
    }.toImmutableList()

    private fun List<DeviceLog>.mapLogsToUI() = map {
        LogDisplayData(
            id = it.timestamp.toEpochMilli().toString(),
            timestamp = timestampFormatter.format(
                it.timestamp.atZone(ZoneOffset.systemDefault())
            ),
            content = it.content,
        )
    }.toImmutableList()

    companion object {
        private val timestampFormatter = DateTimeFormatter.ofPattern("hh:mm:ss")
    }
}
