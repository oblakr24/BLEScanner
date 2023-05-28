package com.rokoblak.blescan.connection

import com.rokoblak.blescan.connection.model.CharacteristicWrapper
import com.rokoblak.blescan.connection.model.DeviceSession
import com.rokoblak.blescan.connection.model.accumulate
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.Optional
import java.util.concurrent.TimeUnit

/**
 * Allows for simplified connection and session handling.
 * Accumulates the session state and maintains the connection for as long as there are observers.
 */
class DeviceSessionManager(val device: BLEDeviceWrapper) {

    private val mgr = DeviceConnectionManager(device)
    private val lastSessionSubject: BehaviorSubject<DeviceSession> = BehaviorSubject.create()

    private fun accumulate(): Observable<DeviceSession> {
        return mgr.connectAndObserveEvents().scan(DeviceSession()) { acc, e ->
            acc.accumulate(e).also {
                lastSessionSubject.onNext(it)
            }
        }
    }

    private var obs: Observable<DeviceSession>? = null

    private fun session() = obs ?: accumulate().also { obs = it }

    fun observeEvents(): Observable<DeviceSession> = session()

    fun connect(): Completable {
        if (lastSessionSubject.value?.connected == true) {
            return Completable.complete()
        }
        return session().filter { it.connected }.firstOrError().ignoreElement()
    }

    fun disconnect() {
        lastSessionSubject.onNext(DeviceSession())
        mgr.disconnect()
    }

    fun readCharacteristic(uuid: String): Single<GattEvent.CharacteristicRead> =
        findCharacteristic(uuid).flatMap { char ->
            mgr.readCharacteristic(char.characteristic).andThen(
                findLastMatchingEvent(uuid)
            )
        }

    fun writeCharacteristic(
        uuid: String,
        data: ByteArray
    ): Single<GattEvent.CharacteristicWritten> = findCharacteristic(uuid).flatMap { char ->
        mgr.writeCharacteristic(char.characteristic, data).andThen(
            findLastMatchingEvent(uuid)
        )
    }

    fun setNotification(
        uuid: String,
        enable: Boolean
    ): Single<GattEvent.CharacteristicChanged> = findCharacteristic(uuid).flatMap { char ->
        mgr.setNotification(char.characteristic, enable).andThen(
            findLastMatchingEvent(uuid)
        )
    }

    private inline fun <reified T : GattEvent> findLastMatchingEvent(uuid: String): Single<T> =
        lastSessionSubject.mapOptional { s ->
            val lastMatching = s.events.mapNotNull { e ->
                if (e is CharacteristicEvent && e is T) {
                    e.takeIf { it.uuid == uuid } as? T
                } else {
                    null
                }
            }.lastOrNull()
            Optional.ofNullable(lastMatching)
        }.firstOrError().timeout(TIMEOUT_SECOND_AWAIT_CHARACTERISTIC_NOTIF, TimeUnit.SECONDS)

    private fun findCharacteristic(uuid: String): Single<CharacteristicWrapper> =
        lastSessionSubject.mapOptional { session ->
            val char = session.allCharacteristics.firstOrNull { it.uuid == uuid }
            Optional.ofNullable(char)
        }.firstOrError().timeout(TIMEOUT_SECONDS_FIND_CHARACTERISTIC, TimeUnit.SECONDS)

    companion object {
        private const val TIMEOUT_SECONDS_FIND_CHARACTERISTIC = 5L
        private const val TIMEOUT_SECOND_AWAIT_CHARACTERISTIC_NOTIF = 10L
    }
}
