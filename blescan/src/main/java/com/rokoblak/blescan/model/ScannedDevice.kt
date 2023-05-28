package com.rokoblak.blescan.model

import android.bluetooth.le.ScanResult

class ScannedDevice(
    val deviceAddress: String,
    val deviceName: String,
    val result: ScanResult,
)