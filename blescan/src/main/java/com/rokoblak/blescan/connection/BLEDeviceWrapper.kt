package com.rokoblak.blescan.connection

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.content.Context

interface BLEDeviceWrapper {
    fun connect(callback: BluetoothGattCallback): BluetoothGatt
    fun name(): String
}

fun BluetoothDevice.toWrapper(context: Context) = object : BLEDeviceWrapper {
    @SuppressLint("MissingPermission")
    override fun connect(callback: BluetoothGattCallback): BluetoothGatt {
        return connectGatt(context, false, callback)
    }

    @SuppressLint("MissingPermission")
    override fun name(): String = name ?: "Unknown device"
}