package com.rokoblak.blescan.connection.model

import android.bluetooth.BluetoothGattCharacteristic
import com.rokoblak.blescan.connection.ConnectionState
import com.rokoblak.blescan.connection.GattEvent
import java.time.Instant

data class DeviceSession(
    val connecting: Boolean = false,
    val connected: Boolean = false,
    val connectionState: String = "-",
    val events: List<GattEvent> = emptyList(),
    val logs: List<DeviceLog> = emptyList(),
    val services: List<Service> = emptyList(),
) {
    val allCharacteristics by lazy {
        services.flatMap { it.characteristics }
    }
}

data class DeviceLog(val timestamp: Instant, val content: String)
data class Service(val id: String, val characteristics: List<CharacteristicWrapper>)
data class CharacteristicWrapper(val uuid: String, val characteristic: BluetoothGattCharacteristic, val read: Boolean, val notify: Boolean, val indicate: Boolean)

fun DeviceSession.accumulate(event: GattEvent): DeviceSession {
    val connectionEvent =
        event as? GattEvent.ConnectionStateChanged

    var connect = false
    var disconnect = false
    var newServices: List<Service>? = null
    var startingConnection = false
    when (event) {
        is GattEvent.CharacteristicChanged -> Unit
        is GattEvent.CharacteristicRead -> Unit
        is GattEvent.CharacteristicWritten -> Unit
        is GattEvent.ConnectionStateChanged -> {
            connect = event.state == ConnectionState.Connected
            disconnect = event.state == ConnectionState.Disconnected
        }
        is GattEvent.ServicesDiscovered -> {
            newServices = event.services.map { svc ->
                Service(svc.uuid.toString(), svc.characteristics.map { it.wrap() })
            }
        }

        GattEvent.StartingConnection -> {
            startingConnection = true
        }
    }

    return copy(
        connecting = if (connected) false else startingConnection,
        connected = connect || (connected && !disconnect),
        connectionState = connectionEvent?.toString() ?: connectionState,
        logs = logs + DeviceLog(Instant.now(), content = event.logValue),
        services = newServices ?: services,
        events = events + event,
    )
}

private fun BluetoothGattCharacteristic.wrap(): CharacteristicWrapper {
    val props = properties
    val read = props and BluetoothGattCharacteristic.PROPERTY_READ != 0
    val notify = props and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0
    val indicate = props and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0
    return CharacteristicWrapper(
        uuid = uuid.toString(),
        characteristic = this,
        read = read,
        notify = notify,
        indicate = indicate,
    )
}
