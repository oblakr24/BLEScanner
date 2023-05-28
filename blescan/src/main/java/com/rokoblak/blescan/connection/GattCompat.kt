package com.rokoblak.blescan.connection

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothStatusCodes
import android.os.Build

/**
 * Encapsulates GATT interaction and API version differences
 */
@SuppressLint("MissingPermission")
class GattCompat {

    private var gatt: BluetoothGatt? = null

    val isActive: Boolean get() = gatt != null

    fun assignNewGatt(new: BluetoothGatt) {
        gatt = new
    }

    fun read(characteristic: BluetoothGattCharacteristic): Boolean {
        return gatt?.readCharacteristic(characteristic) ?: throw IllegalStateException("Gatt not initialized")
    }

    fun write(characteristic: BluetoothGattCharacteristic, data: ByteArray): Boolean {
        val gatt = gatt ?: throw IllegalStateException("Gatt not initialized")
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val statusCode = gatt.writeCharacteristic(
                characteristic,
                data,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            )
            statusCode == BluetoothStatusCodes.SUCCESS
        } else {
            characteristic.value = data
            gatt.writeCharacteristic(characteristic)
        }
    }

    fun setNotification(characteristic: BluetoothGattCharacteristic, enable: Boolean): Boolean {
        val gatt = gatt ?: throw IllegalStateException("Gatt not initialized")
        return gatt.setCharacteristicNotification(characteristic, enable)
    }

    fun disconnect() {
        gatt?.disconnect()
        gatt?.close()
        gatt = null
    }
}
