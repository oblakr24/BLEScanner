package com.rokoblak.blescanner.data.repo.deviceconnection

import android.annotation.SuppressLint
import com.rokoblak.blescan.device.DeviceSessionManager
import com.rokoblak.blescan.device.model.DeviceSession
import com.rokoblak.blescanner.ui.common.SingleEventFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await
import timber.log.Timber
import javax.inject.Inject

/**
 * Repository for starting and maintaining a connection and the current session corresponding to a device.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DeviceConnectionRepository @Inject constructor()  {

    private lateinit var managerState: MutableStateFlow<ManagerInfo>
    private var deviceScope: CoroutineScope? = null

    private fun deviceSessionsFlow(mgr: DeviceSessionManager, scope: CoroutineScope) = mgr.connectAndObserveEvents().doOnTerminate {
            managerState.update { it.copy(connected = false) }
        }.asFlow().catch {
            errors.send(it)
        }.stateIn(scope, SharingStarted.WhileSubscribed(1000), DeviceSession())

    private var sessionFlow: StateFlow<DeviceSession>? = null

    val errors = SingleEventFlow<Throwable>()

    @SuppressLint("MissingPermission")
    fun state(deviceMgr: DeviceSessionManager, scope: CoroutineScope): Flow<DeviceSession?>  {
        managerState = MutableStateFlow(ManagerInfo(deviceMgr, false))
        return managerState.flatMapLatest { (_, connected) ->
            if (connected) {
                sessionFlow ?: deviceSessionsFlow(deviceMgr, scope).also {
                    deviceScope = scope
                    sessionFlow = it
                }
            } else {
                flowOf(null)
            }
        }
    }

    fun connect() {
        managerState.update { it.copy(connected = true) }
    }

    fun disconnect() = deviceScope?.launch {
        managerState.value.manager.disconnect()
        managerState.update { it.copy(connected = false) }
    }

    fun notifyToCharacteristic(uuid: String) {
        deviceScope?.launch {
            try {
                managerState.value.manager.setNotification(uuid, true).await()
            } catch (e: Throwable) {
                e.printStackTrace()
                Timber.e(e)
                errors.send(e)
            }
        }
    }

    fun readCharacteristic(uuid: String) {
        deviceScope?.launch {
            try {
                managerState.value.manager.readCharacteristic(uuid).await()
            } catch (e: Throwable) {
                e.printStackTrace()
                Timber.e(e)
                errors.send(e)
            }
        }
    }

    fun writeCharacteristic(uuid: String, data: ByteArray) {
        deviceScope?.launch {
            try {
                managerState.value.manager.writeCharacteristic(uuid, data).await()
            } catch (e: Throwable) {
                e.printStackTrace()
                Timber.e(e)
                errors.send(e)
            }
        }
    }

    private data class ManagerInfo(
        val manager: DeviceSessionManager,
        val connected: Boolean,
    )
}
