package com.rokoblak.blescanner.ui.screens.scanning

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rokoblak.blescanner.data.repo.scanning.ScanningRepository
import com.rokoblak.blescanner.di.AppScope
import com.rokoblak.blescanner.service.PersistedStorage
import com.rokoblak.blescanner.ui.navigation.RouteNavigator
import com.rokoblak.blescanner.ui.screens.device.DeviceRoute
import com.rokoblak.blescanner.ui.screens.scanning.composables.ScaffoldUIState
import com.rokoblak.blescanner.ui.screens.scanning.composables.ScannedDeviceDisplayData
import com.rokoblak.blescanner.ui.screens.scanning.composables.ScanningContentUIState
import com.rokoblak.blescanner.ui.screens.scanning.composables.ScanningDrawerUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ScanningViewModel @Inject constructor(
    private val appScope: AppScope,
    private val routeNavigator: RouteNavigator,
    private val repo: ScanningRepository,
    private val storage: PersistedStorage,
) : ViewModel(), RouteNavigator by routeNavigator {

    private val bStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (BluetoothAdapter.ACTION_STATE_CHANGED == intent.action) {
                repo.onBtStateUpdated()
            }
        }
    }

    init {
        appScope.appContext.registerReceiver(
            bStateReceiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        )
    }

    val effects = repo.errors

    val uiState: StateFlow<ScaffoldUIState> =
        combine(repo.state, storage.prefsFlow()) { state, prefs ->
            val drawerState = ScanningDrawerUIState(
                darkMode = prefs.darkMode,
                btAvailable = state.btState.supported,
                btEnabled = state.btState.enabled,
                permissionsGranted = state.btState.permissionsGranted
            )
            val content = when {
                !state.btState.supported -> ScaffoldUIState.InnerContent.BTNotAvailable
                !state.btState.enabled -> ScaffoldUIState.InnerContent.BTNotEnabled
                state.btState.green() -> {
                    val session = state.session
                    if (session != null) {
                        val foundItems = session.devices.map {
                            ScannedDeviceDisplayData(
                                id = it.deviceAddress,
                                deviceName = it.deviceName,
                                address = it.deviceAddress,
                            )
                        }
                        ScaffoldUIState.InnerContent.Content(
                            ScanningContentUIState(
                                scanning = session.scanning,
                                foundItems = foundItems.toImmutableList()
                            )
                        )
                    } else {
                        ScaffoldUIState.InnerContent.Initial
                    }
                }

                else -> ScaffoldUIState.InnerContent.NoPermissions(shouldShowSettingsBtn = state.btState.permissionDeniedByUser)
            }
            ScaffoldUIState(drawerState, content)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            ScaffoldUIState(
                ScanningDrawerUIState(
                    darkMode = null,
                    btAvailable = false,
                    btEnabled = false,
                    permissionsGranted = false
                ),
                ScaffoldUIState.InnerContent.Initial
            )
        )

    fun handleAction(act: ScanningAction) {
        when (act) {
            is ScanningAction.PermissionsOrBTStateUpdated -> repo.onPermissionsUpdated(act.deniedByUser == true)
            is ScanningAction.SetDarkMode -> setDarkMode(act.enabled)
            ScanningAction.StartScan -> repo.startScanning()
            ScanningAction.StopScan -> repo.stopScanning()
            is ScanningAction.OpenDevice -> {
                navigateToRoute(DeviceRoute.get(DeviceRoute.Input(act.address)))
            }
        }
    }

    private fun setDarkMode(enabled: Boolean) = viewModelScope.launch {
        storage.updateDarkMode(enabled)
    }

    override fun onCleared() {
        appScope.appContext.unregisterReceiver(bStateReceiver)
        super.onCleared()
    }
}
