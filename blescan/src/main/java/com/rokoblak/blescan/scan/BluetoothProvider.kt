package com.rokoblak.blescan.scan

import android.bluetooth.BluetoothManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class BluetoothProvider @Inject constructor(@ApplicationContext private val context: Context) {

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager

    private val bluetoothAdapter get() = bluetoothManager?.adapter
    val bleScanner get() = bluetoothAdapter?.bluetoothLeScanner

    fun supported(): Boolean {
        return bluetoothAdapter != null
    }

    fun enabled(): Boolean {
        return bluetoothAdapter?.isEnabled ?: false
    }
}
