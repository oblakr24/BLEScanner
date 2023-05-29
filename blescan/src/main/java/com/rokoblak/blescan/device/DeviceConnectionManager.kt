package com.rokoblak.blescan.device

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import com.rokoblak.blescan.exceptions.BluetoothException
import com.rokoblak.blescan.exceptions.CharacteristicOperationFailed
import com.rokoblak.blescan.exceptions.DeviceNotConnectedException
import com.rokoblak.blescan.exceptions.ServiceDiscoveryStartFailed
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.PublishSubject
import timber.log.Timber

/**
 * Manages a connection with a single BLE device in the form of a stream of events from connection start to disconnection.
 * The connection is shared for a single session.
 */
@SuppressLint("MissingPermission")
class DeviceConnectionManager(
    val device: BLEDeviceWrapper,
    private val gattCompat: GattCompat = GattCompat(),
) {
    private var disconnectSignal = PublishSubject.create<Unit>()

    private fun createConnection(disconnectOnComplete: Boolean): Observable<GattEvent> =
        Observable.defer {
            Observable.create { emitter ->
                val gattCallback = object : BluetoothGattCallback() {
                    override fun onConnectionStateChange(
                        gatt: BluetoothGatt,
                        status: Int,
                        newState: Int
                    ) {
                        if (status != BluetoothGatt.GATT_SUCCESS && newState != BluetoothProfile.STATE_DISCONNECTED) {
                            emitter.onError(BluetoothException("Status Error: $status"))
                            return
                        }

                        when (newState) {
                            BluetoothProfile.STATE_CONNECTING -> {
                                emitter.onNext(GattEvent.ConnectionStateChanged(ConnectionState.Connecting))
                            }

                            BluetoothProfile.STATE_CONNECTED -> {
                                val servicesDiscoveryStarted = gatt.discoverServices()
                                if (!servicesDiscoveryStarted) {
                                    Timber.e("Service discovery failed to initiate")
                                }
                                emitter.onNext(GattEvent.ConnectionStateChanged(ConnectionState.Connected(servicesDiscoveryStarted)))
                            }

                            BluetoothProfile.STATE_DISCONNECTED -> {
                                emitter.onNext(GattEvent.ConnectionStateChanged(ConnectionState.Disconnected))
                                emitter.onComplete()
                            }

                            BluetoothProfile.STATE_DISCONNECTING -> {
                                emitter.onNext(GattEvent.ConnectionStateChanged(ConnectionState.Disconnecting))
                            }
                            else -> {
                                Timber.i("Connection change, status: $status, new state: $newState")
                            }
                        }
                    }

                    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                        emitter.onNext(
                            GattEvent.ServicesDiscovered(
                                gatt.services.orEmpty(),
                                status == BluetoothGatt.GATT_SUCCESS
                            )
                        )
                    }

                    override fun onCharacteristicChanged(
                        gatt: BluetoothGatt,
                        characteristic: BluetoothGattCharacteristic,
                        value: ByteArray
                    ) {
                        emitter.onNext(GattEvent.CharacteristicChanged(characteristic, value))
                    }

                    override fun onCharacteristicWrite(
                        gatt: BluetoothGatt?,
                        characteristic: BluetoothGattCharacteristic,
                        status: Int
                    ) {
                        emitter.onNext(
                            GattEvent.CharacteristicWritten(
                                characteristic,
                                status == BluetoothGatt.GATT_SUCCESS
                            )
                        )
                    }

                    override fun onCharacteristicRead(
                        gatt: BluetoothGatt,
                        characteristic: BluetoothGattCharacteristic,
                        value: ByteArray,
                        status: Int
                    ) {
                        emitter.onNext(
                            GattEvent.CharacteristicRead(
                                characteristic,
                                value,
                                status == BluetoothGatt.GATT_SUCCESS
                            )
                        )
                    }
                }

                emitter.setCancellable {
                    if (disconnectOnComplete) {
                        disconnect()
                    }
                }

                emitter.onNext(GattEvent.StartingConnection)
                gattCompat.assignNewGatt(device.connect(gattCallback))
            }
        }

    private var connection: Observable<GattEvent>? = null

    private fun defaultConnection(disconnectOnComplete: Boolean = false) =
        connection ?: createConnection(disconnectOnComplete)
            .takeUntil(disconnectSignal)
            .share()
            .also {
                connection = it
            }

    /**
     * Requests the read of a characteristic.
     * Returns an error if the interaction fails or if the device is not connected.
     */
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic): Completable {
        if (!gattCompat.isActive) return Completable.error(DeviceNotConnectedException())
        val success = gattCompat.read(characteristic)
        if (!success) return Completable.error(CharacteristicOperationFailed("Cannot read characteristic ${characteristic.uuid}"))
        return Completable.complete()
    }

    /**
     * Reads the characteristic and returns the corresponding acknowledge response for that characteristic.
     */
    fun readCharacteristicAndAwait(characteristic: BluetoothGattCharacteristic): Single<GattEvent.CharacteristicRead> {
        return readCharacteristic(characteristic).andThen(
            defaultConnection().ofType(GattEvent.CharacteristicRead::class.java).firstOrError()
        )
    }

    /**
     * Requests the write for a characteristic.
     * Returns an error if the interaction fails or if the device is not connected.
     */
    fun writeCharacteristic(
        characteristic: BluetoothGattCharacteristic,
        data: ByteArray
    ): Completable {
        if (!gattCompat.isActive) return Completable.error(DeviceNotConnectedException())
        val success = gattCompat.write(characteristic, data)
        if (!success) return Completable.error(CharacteristicOperationFailed("Cannot write characteristic ${characteristic.uuid}"))
        return Completable.complete()
    }

    /**
     * Writes the characteristic and returns the corresponding acknowledge response for that characteristic.
     */
    fun writeCharacteristicAndAwait(
        characteristic: BluetoothGattCharacteristic,
        data: ByteArray
    ): Single<GattEvent.CharacteristicWritten> {
        return writeCharacteristic(characteristic, data).andThen(
            defaultConnection().ofType(GattEvent.CharacteristicWritten::class.java).firstOrError()
        )
    }

    /**
     * Sets the notification for a characteristic.
     * Returns an error if the interaction fails or if the device is not connected.
     */
    fun setNotification(
        characteristic: BluetoothGattCharacteristic,
        enable: Boolean
    ): Completable {
        if (!gattCompat.isActive) return Completable.error(DeviceNotConnectedException())
        val success = gattCompat.setNotification(characteristic, enable)
        if (!success) return Completable.error(CharacteristicOperationFailed("Cannot set notification for characteristic ${characteristic.uuid}"))
        return Completable.complete()
    }

    /**
     * Sets a notification for a characteristic and returns the next corresponding change response for that characteristic.
     */
    fun setNotificationAndAwait(
        characteristic: BluetoothGattCharacteristic,
        enable: Boolean
    ): Single<GattEvent.CharacteristicChanged> {
        return setNotification(characteristic, enable).andThen(
            defaultConnection().ofType(GattEvent.CharacteristicChanged::class.java).firstOrError()
        )
    }

    /**
     * Requests the services discovery process.
     * Returns an error if services discovery fails.
     */
    fun discoverServices(): Completable {
        if (!gattCompat.isActive) return Completable.error(DeviceNotConnectedException())
        val servicesDiscoveryStarted = gattCompat.discoverServices()
        if (!servicesDiscoveryStarted) {
            return Completable.error(ServiceDiscoveryStartFailed())
        }
        return Completable.complete()
    }

    /**
     * Connects to the device and returns once the connection succeeds.
     */
    fun connect(): Completable {
        return defaultConnection().ofType(GattEvent.ConnectionStateChanged::class.java).filter {
            it.state is ConnectionState.Connected
        }.firstOrError().ignoreElement()
    }

    /**
     * Connects and observes the events of the connection.
     * Lasts until disconnection, an error, or when disconnect is called.
     * The connection disconnects when this observable disposes.
     */
    fun connectAndObserveEvents() = defaultConnection(disconnectOnComplete = true)

    fun disconnect() {
        if (gattCompat.isActive) {
            disconnectSignal.onNext(Unit)
        }
        disconnectSignal = PublishSubject.create()
        gattCompat.disconnect()
        connection = null
    }
}
