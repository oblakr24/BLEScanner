package com.rokoblak.blescan.device

import com.rokoblak.blescan.device.model.CharacteristicWrapper
import com.rokoblak.blescan.device.model.DeviceSession
import com.rokoblak.blescan.device.model.accumulate
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
class DeviceSessionManager(private val delegate: DeviceConnectionManager) {

    private val lastSessionSubject: BehaviorSubject<DeviceSession> = BehaviorSubject.create()

    private fun accumulate(): Observable<DeviceSession> {
        return delegate.connectAndObserveEvents().scan(DeviceSession()) { acc, e ->
            acc.accumulate(e).also {
                lastSessionSubject.onNext(it)
            }
        }
    }

    private var obs: Observable<DeviceSession>? = null

    private fun session() = obs ?: accumulate().also { obs = it }

    /**
     * Connects and observes changes to the session.
     * Lasts until disconnection, an error, or when disconnect is called.
     * The connection disconnects when this observable disposes.
     */
    fun connectAndObserveEvents(): Observable<DeviceSession> = session()

    /**
     * Connects to the session, returning successfully once the connection is established.
     * Returns immediately if the is already a connected session.
     */
    fun connect(): Completable {
        if (lastSessionSubject.value?.connected == true) {
            return Completable.complete()
        }
        return session().filter { it.connected }.firstOrError().ignoreElement()
    }

    /**
     * Disconnects from the session.
     */
    fun disconnect() {
        lastSessionSubject.onNext(DeviceSession())
        delegate.disconnect()
    }

    /**
     * Reads the characteristic and returns the corresponding acknowledge response for that characteristic.
     */
    fun readCharacteristic(uuid: String): Single<GattEvent.CharacteristicRead> =
        findCharacteristic(uuid).flatMap { char ->
            delegate.readCharacteristic(char.characteristic).andThen(
                findLastMatchingEvent(uuid)
            )
        }

    /**
     * Writes the characteristic and returns the corresponding acknowledge response for that characteristic.
     */
    fun writeCharacteristic(
        uuid: String,
        data: ByteArray
    ): Single<GattEvent.CharacteristicWritten> = findCharacteristic(uuid).flatMap { char ->
        delegate.writeCharacteristic(char.characteristic, data).andThen(
            findLastMatchingEvent(uuid)
        )
    }

    /**
     * Sets a notification for a characteristic and returns the next corresponding change response for that characteristic.
     */
    fun setNotification(
        uuid: String,
        enable: Boolean
    ): Single<GattEvent.CharacteristicChanged> = findCharacteristic(uuid).flatMap { char ->
        delegate.setNotification(char.characteristic, enable).andThen(
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
