package com.rokoblak.blescan.scan

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import com.rokoblak.blescan.exceptions.BTUnavailableException
import com.rokoblak.blescan.exceptions.PermissionNotGrantedException
import com.rokoblak.blescan.exceptions.ScanFailedException
import com.rokoblak.blescan.model.ScannedDevice
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DeviceScannerImpl @Inject constructor(
    private val btProvider: BluetoothProvider,
    private val permissionsChecker: PermissionsChecker,
    private val timeScheduler: Scheduler = Schedulers.computation(),
) : DeviceScanner {

    private lateinit var settings: BleScanSettings

    private val bleScanner get() = btProvider.bleScanner

    private var stopSignal = PublishSubject.create<Unit>()

    override fun supported(): Boolean = btProvider.supported()

    override fun enabled(): Boolean = btProvider.enabled()

    private var scanningObservable: Observable<ScannedDevice> = createScanningObservable()

    private fun createSharedObservable() = createScanningObservable()
        .subscribeOn(Schedulers.io())
        .takeUntil(stopSignal)
        .publish()
        .autoConnect()

    @SuppressLint("MissingPermission")
    private fun createScanningObservable(): Observable<ScannedDevice> = Observable.defer {
        Observable.create { emitter ->
            permissionsChecker.checkMissingPermissions()?.let {
                emitter.onError(PermissionNotGrantedException(Manifest.permission.ACCESS_FINE_LOCATION))
                return@create
            }

            val scanner = bleScanner ?: kotlin.run {
                emitter.onError(BTUnavailableException())
                return@create
            }

            val scanCallback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    emitter.onNext(result.wrap())
                }

                override fun onScanFailed(errorCode: Int) {
                    emitter.onError(ScanFailedException(errorCode))
                }
            }

            scanner.startScan(null, settings.resolve(), scanCallback)

            emitter.setCancellable {
                scanner.stopScan(scanCallback)
                resetObservable()
            }
        }.takeUntil(Observable.timer(settings.timeout.seconds, TimeUnit.SECONDS, timeScheduler))
            .takeUntil(stopSignal)
            .timeout(settings.timeout.seconds, TimeUnit.SECONDS, timeScheduler)
    }

    override fun startScanning(settings: BleScanSettings): Observable<ScannedDevice> {
        this.settings = settings
        return scanningObservable
    }

    override fun stopScanning() {
        stopSignal.onNext(Unit)
        resetObservable()
    }

    private fun resetObservable() {
        stopSignal = PublishSubject.create()
        scanningObservable = createSharedObservable()
    }

    @SuppressLint("MissingPermission")
    private fun ScanResult.wrap(): ScannedDevice {
        return ScannedDevice(
            deviceAddress = this.device.address,
            deviceName = this.device.name ?: "Unknown device",
            result = this
        )
    }
}
