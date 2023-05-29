package com.rokoblak.blescanner.data.repo.scanning

import com.rokoblak.blescan.scan.DeviceScanner
import com.rokoblak.blescanner.data.CurrentSessionState
import com.rokoblak.blescanner.data.ScanningSession
import com.rokoblak.blescanner.di.AppScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Repository for starting and stopping a scanning session, depending on all the bluetooth permissions and checks.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ScanningRepository @Inject constructor(
    private val deviceScanner: DeviceScanner,
    private val session: ScanningSession,
    private val appScope: AppScope,
) {
    private var permDeniedByUser: Boolean = false

    private val btState = MutableStateFlow(resolveBTState())

    val state: Flow<ScanningState> = btState.flatMapLatest { btState ->
        if (btState.green()) {
            session.sessionState.map { sessionState ->
                ScanningState(btState, sessionState)
            }
        } else {
            flowOf(ScanningState(btState, session = null))
        }
    }

    val errors = session.errors

    fun onBtStateUpdated() {
        btState.value = resolveBTState()
    }

    fun onPermissionsUpdated(deniedByUser: Boolean) {
        permDeniedByUser = deniedByUser
        btState.value = resolveBTState()
    }

    fun startScanning() {
        session.startScan()
    }

    fun stopScanning() {
        session.stopScan()
    }

    private fun resolveBTState() = BluetoothState(
        supported = deviceScanner.supported(),
        enabled = deviceScanner.enabled(),
        permissionsGranted = appScope.hasScanningPermissions(),
        permissionDeniedByUser = permDeniedByUser,
    )
}

data class ScanningState(
    val btState: BluetoothState,
    val session: CurrentSessionState?,
)

data class BluetoothState(
    val supported: Boolean,
    val enabled: Boolean,
    val permissionsGranted: Boolean,
    val permissionDeniedByUser: Boolean,
) {
    fun green() = supported && enabled && permissionsGranted
}
