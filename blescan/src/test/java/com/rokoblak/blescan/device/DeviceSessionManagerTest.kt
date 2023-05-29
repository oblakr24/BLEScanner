package com.rokoblak.blescan.device

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import com.rokoblak.blescan.exceptions.BluetoothException
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.UUID


@RunWith(JUnit4::class)
class DeviceSessionManagerTest {

    @RelaxedMockK
    lateinit var mgr: DeviceSessionManager

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun testConnectionSucceeds() {
        // Given: healthy setup
        val delegate = createMockDelegate(connects = true)

        mgr = DeviceSessionManager(delegate)

        // When: we connect
        val connectObserver = mgr.connect().test()

        // Then: complete without errors
        connectObserver.assertNoErrors()
        connectObserver.assertComplete()
    }

    @Test
    fun testConnectionSucceedsInstantlyIfAlreadyConnected() {
        // Given: healthy setup
        val delegate = createMockDelegate(connects = true)

        mgr = DeviceSessionManager(delegate)

        // When: we connect and observe events
        val connectObserver = mgr.connectAndObserveEvents().test()

        // Then: complete without errors
        connectObserver.assertNoErrors()
        connectObserver.assertNotComplete()
    }

    @Test
    fun testSessionAccumulates() {
        // Given: healthy setup with services discovered
        val char = createMockChar()
        val svcs = createMockService(char)
        val delegate = createMockDelegate(connects = true, intermediate = listOf(
            GattEvent.ServicesDiscovered(svcs, true)
        ))

        mgr = DeviceSessionManager(delegate)

        // When: we connect and observe events
        val connectObserver = mgr.connectAndObserveEvents().test()

        // Then: we receive session states in the right sequence
        connectObserver.assertNoErrors()
        connectObserver.assertValueAt(1) {
            !it.connected // Not connected at first
        }
        connectObserver.assertValueAt(2) {
            it.connected // Now connected
        }
        connectObserver.assertValueAt(3) {
            it.connected && it.services.size == 1 // Now we have services as well
        }
        // Then: observer still active since the events didn't terminate yet
        connectObserver.assertNotComplete()
    }

    @Test
    fun testSessionEndsAfterDisconnecting() {
        // Given: healthy setup with services discovered
        val char = createMockChar()
        val svcs = createMockService(char)
        val stopSignal = PublishSubject.create<Unit>()
        val endingEventSubject = BehaviorSubject.create<GattEvent>()
        val delegate = createMockDelegate(connects = true, intermediate = listOf(
            GattEvent.ServicesDiscovered(svcs, true)
        ), endsInDisconnect = false, endingSubject = endingEventSubject.takeUntil(stopSignal))

        mgr = DeviceSessionManager(delegate)

        // When: we connect and observe events
        val sessionObserver = mgr.connectAndObserveEvents().test()

        // Then: no errors
        sessionObserver.assertNoErrors()

        // When: we call connect
        val connectObserver = mgr.connect().test()

        // Then: connect observer completes without error
        connectObserver.assertComplete()
        connectObserver.assertNoErrors()

        // Then: events observer still runs
        sessionObserver.assertNotComplete()

        // When: we call connect again
        val secondConnectObserver = mgr.connect().test()

        // Then: the second connect observer completes without error again
        secondConnectObserver.assertComplete()
        secondConnectObserver.assertNoErrors()
        // Then: events observer still runs
        sessionObserver.assertNotComplete()

        // When: we call disconnect
        every { delegate.disconnect() } just runs
        mgr.disconnect()
        verify(exactly = 1) { delegate.disconnect() }
        // When: we ensure the delegate communicates the last event and completes
        endingEventSubject.onNext(GattEvent.ConnectionStateChanged(ConnectionState.Disconnected))
        stopSignal.onNext(Unit)

        // Then: the events observer finally completes
        sessionObserver.assertComplete()
    }

    @Test
    fun testSessionAccumulatesAndCharacteristicInteractionsWork() {
        // Given: healthy setup with services discovered
        val charWrite = createMockChar()
        val char1 = createMockChar()
        val char2 = createMockChar()
        val char3 = createMockChar()
        val svcs = createMockService(charWrite, char1, char2, char3)
        val delegate = createMockDelegate(connects = true, intermediate = listOf(
            GattEvent.ServicesDiscovered(svcs, true),
            GattEvent.CharacteristicRead(char1, byteArrayOf(), success = false),
            GattEvent.ServicesDiscovered(svcs, true),
            GattEvent.CharacteristicRead(char2, byteArrayOf(), success = true),
            GattEvent.ServicesDiscovered(svcs, true),
            GattEvent.CharacteristicWritten(char3, true),
            GattEvent.CharacteristicWritten(charWrite, true)
        ))
        every { delegate.writeCharacteristic(charWrite, any()) } returns Completable.complete()

        mgr = DeviceSessionManager(delegate)

        // When: we connect and observe events
        val sessionObserver = mgr.connectAndObserveEvents().test()

        // When: we write a characteristic
        val writeObserver = mgr.writeCharacteristic(charWrite.uuid.toString(), "data1".toByteArray()).test()

        // Then: the operation succeeds and returns the corresponding characteristic
        writeObserver.assertNoErrors()
        writeObserver.assertValue {
            it.uuid == charWrite.uuid.toString() && it.success
        }

        // Then: the events observer still runs without error
        sessionObserver.assertNoErrors()
        sessionObserver.assertNotComplete()
    }

    @Test
    fun testNotificationAndReadCharacteristicWork() {
        // Given: healthy setup with services discovered
        val charRead = createMockChar()
        val charNotif = createMockChar()
        val svcs = createMockService(charRead, charNotif)
        val delegate = createMockDelegate(connects = true, intermediate = listOf(
            GattEvent.ServicesDiscovered(svcs, true),
            GattEvent.CharacteristicRead(charRead, byteArrayOf(), success = true),
            GattEvent.CharacteristicChanged(charNotif, byteArrayOf()),
        ))
        every { delegate.readCharacteristic(charRead) } returns Completable.complete()
        every { delegate.setNotification(charNotif, true) } returns Completable.complete()

        mgr = DeviceSessionManager(delegate)

        // When: we connect and observe events
        val sessionObserver = mgr.connectAndObserveEvents().test()

        // When: we read a characteristic
        val readObserver = mgr.readCharacteristic(charRead.uuid.toString()).test()

        // Then: the operation succeeds and returns the corresponding characteristic
        readObserver.assertNoErrors()
        readObserver.assertValue {
            it.uuid == charRead.uuid.toString() && it.success
        }

        // When: we set a characteristic notification
        val notifObserver = mgr.setNotification(charNotif.uuid.toString(), true).test()

        // Then: the operation succeeds and returns the corresponding characteristic
        notifObserver.assertNoErrors()
        notifObserver.assertValue {
            it.uuid == charNotif.uuid.toString()
        }

        // Then: the events observer still runs without error
        sessionObserver.assertNoErrors()
        sessionObserver.assertNotComplete()
    }

    private fun createMockChar(): BluetoothGattCharacteristic {
        val uuidChar = UUID.randomUUID()
        val mockChar = mockk<BluetoothGattCharacteristic>()
        every { mockChar.uuid } returns uuidChar
        every { mockChar.properties } returns 7
        return mockChar
    }

    private fun createMockService(vararg char: BluetoothGattCharacteristic): List<BluetoothGattService> {
        val uuidSvc = UUID.randomUUID()
        val mockSvc = mockk<BluetoothGattService>()
        every { mockSvc.uuid } returns uuidSvc
        every { mockSvc.characteristics } returns char.toList()
        return listOf(mockSvc)
    }

    private fun createMockDelegate(connects: Boolean, intermediate: List<GattEvent> = emptyList(), endsInDisconnect: Boolean = false, endingSubject: Observable<GattEvent>? = null): DeviceConnectionManager {
        val delegate = mockk<DeviceConnectionManager>()
        val out: Observable<GattEvent> = if (!connects) {
            Observable.concat(
                Observable.just(GattEvent.StartingConnection),
                Observable.error(BluetoothException("Error connecting")),
            )
        } else {
            val intermEvents = Observable.fromIterable(intermediate)
            val endingItem = if (endsInDisconnect) {
                Observable.just(GattEvent.ConnectionStateChanged(ConnectionState.Disconnected))
            } else endingSubject ?: Observable.never()
            Observable.concat(
                Observable.just(GattEvent.StartingConnection, GattEvent.ConnectionStateChanged(ConnectionState.Connected(true))),
                intermEvents,
                endingItem
            )
        }
        every { delegate.connectAndObserveEvents() } returns out
        return delegate
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }
}
