package com.rokoblak.blescan.connection

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService

sealed class GattEvent(open val logValue: String) {
    object StartingConnection: GattEvent("Connecting")
    data class ConnectionStateChanged(val state: ConnectionState) : GattEvent("Connection: ${state.name}")
    class CharacteristicRead(
        val characteristic: BluetoothGattCharacteristic,
        val value: ByteArray,
        val success: Boolean,
    ) : GattEvent("Characteristic read: ${characteristic.uuid}"), CharacteristicEvent {
        override val uuid: String = characteristic.uuid.toString()
    }

    class CharacteristicWritten(
        val characteristic: BluetoothGattCharacteristic,
        val success: Boolean,
    ) : GattEvent("Characteristic written: ${characteristic.uuid}"), CharacteristicEvent {
        override val uuid: String = characteristic.uuid.toString()
    }

    class CharacteristicChanged(
        val characteristic: BluetoothGattCharacteristic,
        val value: ByteArray
    ) : GattEvent("Characteristic changed: ${characteristic.uuid}"), CharacteristicEvent {
        override val uuid: String = characteristic.uuid.toString()
    }

    data class ServicesDiscovered(
        val services: List<BluetoothGattService>,
        val success: Boolean,
    ) : GattEvent("${services.size} services discovered")
}

interface CharacteristicEvent {
    val uuid: String
}

enum class ConnectionState {
    Connecting,
    Connected,
    Disconnecting,
    Disconnected,
}
