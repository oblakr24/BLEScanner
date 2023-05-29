package com.rokoblak.blescan.scan

import android.bluetooth.le.ScanSettings
import com.rokoblak.blescan.model.ScannedDevice
import io.reactivex.rxjava3.core.Observable
import java.time.Duration

interface DeviceScanner {

    /**
     * Start scanning for devices. Individual devices are emitted.
     * The scan completes:
     * - when the time provided in the settings passes
     * - when an error occurs
     * - when stopScanning is called
     */
    fun startScanning(settings: BleScanSettings = BleScanSettings()): Observable<ScannedDevice>

    /**
     * Stop the scanning process.
     * Completes the observable returned by the startScanning call.
     */
    fun stopScanning()

    /**
     * Is Bluetooth supported on this device
     */
    fun supported(): Boolean

    /**
     * Is Bluetooth enabled on this device
     */
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
