package com.rokoblak.blescanner.data

import android.content.Context
import com.rokoblak.blescan.device.DeviceConnectionManager
import com.rokoblak.blescan.device.DeviceSessionManager
import com.rokoblak.blescan.device.toWrapper
import com.rokoblak.blescan.scan.DeviceScanner
import com.rokoblak.blescan.model.ScannedDevice
import com.rokoblak.blescanner.ui.common.SingleEventFlow
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.rx3.asFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class ScanningSession @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceScanner: DeviceScanner,
) {
    private var scanningScope = CoroutineScope(Dispatchers.IO + Job())
    private val isScanning = MutableStateFlow(false)
    private val terminated = MutableStateFlow(true)

    private var scanResults: StateFlow<List<ScannedDevice>> = MutableStateFlow(emptyList())

    val errors = SingleEventFlow<Throwable>()

    val sessionState =
        combine(isScanning, terminated) { scanning, terminated -> scanning to terminated }
            .flatMapLatest { (scanning, terminated) ->
                scanResults.map { devices ->
                    CurrentSessionState(devices, scanning && !terminated)
                }
            }

    private fun observeScannedDevices() = deviceScanner.startScanning()
        .doOnTerminate {
            terminated.value = true
        }
        .asFlow()
        .catch {
            Timber.e(it)
            errors.send(it)
        }

    private fun createScanResults() =
        observeScannedDevices().runningFold(emptyList<ScannedDevice>()) { acc, a ->
            (acc + a).distinctBy { it.deviceAddress }.sortedBy { it.deviceName }
        }.stateIn(scanningScope, SharingStarted.WhileSubscribed(500), initialValue = emptyList())

    private fun findDevice(address: String) =
        scanResults.value.firstOrNull { it.deviceAddress == address }

    private val sessionManagers = mutableMapOf<String, DeviceSessionManager>()

    fun getSessionManager(address: String): DeviceSessionManager? {
        val device = findDevice(address) ?: return null
        return sessionManagers.getOrPut(address) {
            val btDevice = device.result.device
            val delegate = DeviceConnectionManager(btDevice.toWrapper(context))
            DeviceSessionManager(delegate)
        }
    }

    fun startScan() {
        scanResults = createScanResults()
        isScanning.value = true
        terminated.value = false
    }

    fun stopScan() {
        deviceScanner.stopScanning()
        scanningScope.cancel()
        scanningScope = CoroutineScope(Dispatchers.IO + Job())
        isScanning.value = false
    }
}

data class CurrentSessionState(
    val devices: List<ScannedDevice>,
    val scanning: Boolean,
)
