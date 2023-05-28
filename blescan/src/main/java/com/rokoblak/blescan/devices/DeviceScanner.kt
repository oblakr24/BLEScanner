package com.rokoblak.blescan.devices

import android.bluetooth.le.ScanSettings
import com.rokoblak.blescan.model.ScannedDevice
import io.reactivex.rxjava3.core.Observable
import java.time.Duration

interface DeviceScanner {

    fun startScanning(settings: BleScanSettings = BleScanSettings()): Observable<ScannedDevice>
    fun stopScanning()

    fun supported(): Boolean
    fun enabled(): Boolean
}

class BleScanSettings(
    val timeout: Duration = Duration.ofSeconds(8),
    private val customSettings: ((ScanSettings.Builder) -> ScanSettings.Builder)? = null,
) {
    fun resolve(): ScanSettings {
        return ScanSettings.Builder()
            .let { builder ->
                customSettings?.invoke(builder) ?: builder
            }
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()
    }
}
