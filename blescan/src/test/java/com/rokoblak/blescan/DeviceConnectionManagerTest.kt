package com.rokoblak.blescan

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import com.rokoblak.blescan.connection.BLEDeviceWrapper
import com.rokoblak.blescan.connection.DeviceConnectionManager
import com.rokoblak.blescan.connection.GattCompat
import com.rokoblak.blescan.exceptions.BluetoothException
import com.rokoblak.blescan.exceptions.CharacteristicOperationFailed
import com.rokoblak.blescan.exceptions.DeviceNotConnectedException
import io.mockk.CapturingSlot
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.UUID


@RunWith(JUnit4::class)
class DeviceConnectionManagerTest {

    @RelaxedMockK
    lateinit var mgr: DeviceConnectionManager

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun testConnectionSucceeds() {
        // Given: a healthy setup
        val gatt: BluetoothGatt = mockk()
        every { gatt.discoverServices() } returns true
        val (device, callbackSlot) = createMockDevice(gatt)

        mgr = DeviceConnectionManager(device)

        // When: we connect and establish a connection
        val testObserver = mgr.connect().test()

        val callback = callbackSlot.captured
        callback.onConnectionStateChange(gatt, BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTING)
        callback.onConnectionStateChange(gatt, BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTED)

        // Then: connect succeeds and finishes
        testObserver.assertNoErrors()
        testObserver.assertComplete()
    }

    @Test
    fun testConnectDoesNotStartNewConnectionIfAlreadyStarted() {
        // Given: a healthy setup
        val gatt: BluetoothGatt = mockk()
        every { gatt.discoverServices() } returns true
        val (device, callbackSlot) = createMockDevice(gatt)

        mgr = DeviceConnectionManager(device)

        verify(exactly = 0) { device.connect(any()) }

        // When: we start observing, and afterwards also call connect
        val testObserver = mgr.connectAndObserveEvents().test()

        // Then: first connect call made
        verify(exactly = 1) { device.connect(any()) }

        val testObserverConnect = mgr.connect().test()

        // Then: second call not made
        verify(exactly = 1) { device.connect(any()) }

        // Establish connection
        val callback = callbackSlot.captured
        callback.onConnectionStateChange(gatt, BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTING)
        callback.onConnectionStateChange(gatt, BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTED)

        // Then: main observer should not complete yet
        testObserver.assertNoErrors()
        testObserver.assertNotComplete()
        // Then: the connect observer finished
        testObserverConnect.assertNoErrors()
        testObserverConnect.assertComplete()

        every { gatt.disconnect() } just runs
        every { gatt.close() } just runs
        callback.onConnectionStateChange(gatt, BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_DISCONNECTED)
        // Then: the main observer also finished
        testObserver.assertComplete()

        // Now let's connect again
        val testObserverConnectAfter = mgr.connect().test()
        val callback2 = callbackSlot.captured

        // Then: the second connect observer finishes with the second connect call being made
        callback2.onConnectionStateChange(gatt, BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTING)
        callback2.onConnectionStateChange(gatt, BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTED)
        verify(exactly = 2) { device.connect(any()) }
        testObserverConnectAfter.assertNoErrors()
        testObserverConnectAfter.assertComplete()
    }

    @Test
    fun testObserverFinishesIfDisconnectCalled() {
        // Given: a healthy setup
        val gatt: BluetoothGatt = mockk()
        every { gatt.discoverServices() } returns true
        every { gatt.disconnect() } just runs
        every { gatt.close() } just runs
        val (device, callbackSlot) = createMockDevice(gatt)

        val gattCompat: GattCompat = mockk()
        every { gattCompat.disconnect() } just runs
        every { gattCompat.isActive } returns true

        mgr = DeviceConnectionManager(device)

        // When: we establish a connection and then call disconnect
        val testObserver = mgr.connectAndObserveEvents().test()

        val callback = callbackSlot.captured
        callback.onConnectionStateChange(gatt, BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTED)

        mgr.disconnect()
        verify(exactly = 1) { gatt.disconnect() }

        // Call again
        mgr.disconnect()
        // Then: disconnect will not call again internally
        verify(exactly = 1) { gatt.disconnect() }
        // Then: the observer completes
        testObserver.assertComplete()
    }

    @Test
    fun testConnectionFailsIfCallbackReceived() {
        val gatt: BluetoothGatt = mockk()
        every { gatt.discoverServices() } returns true
        val (device, callbackSlot) = createMockDevice(gatt)

        mgr = DeviceConnectionManager(device)

        val testObserver = mgr.connect().test()

        val callback = callbackSlot.captured

        callback.onConnectionStateChange(gatt, BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTING)
        callback.onConnectionStateChange(gatt, BluetoothGatt.GATT_FAILURE, BluetoothProfile.STATE_CONNECTING)

        testObserver.assertError {
            it is BluetoothException
        }
    }

    @Test
    fun testEventsEmitAndComplete() {
        val gatt: BluetoothGatt = mockk()
        every { gatt.discoverServices() } returns false
        every { gatt.disconnect() } just runs
        every { gatt.close() } just runs
        val (device, callbackSlot) = createMockDevice(gatt)

        mgr = DeviceConnectionManager(device)

        val testObserver = mgr.connectAndObserveEvents().test()

        val callback = callbackSlot.captured

        callback.onConnectionStateChange(gatt, BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTING)
        callback.onConnectionStateChange(gatt, BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTED)
        callback.onConnectionStateChange(gatt, BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_DISCONNECTING)
        callback.onConnectionStateChange(gatt, BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_DISCONNECTED)

        testObserver.assertNoErrors()
        testObserver.assertValueCount(5) // 4 + initial connecting
        testObserver.assertComplete()
    }

    @Test
    fun characteristicInteractionsFailIfNotConnected() {
        val gatt: BluetoothGatt = mockk()
        val (device, _) = createMockDevice(gatt)

        mgr = DeviceConnectionManager(device)

        val testObserverRead = mgr.readCharacteristicAndAwait(createMockCharacteristic(0)).test()
        val testObserverWrite = mgr.writeCharacteristicAndAwait(createMockCharacteristic(1), byteArrayOf()).test()
        val testObserverNotify = mgr.setNotificationAndAwait(createMockCharacteristic(2), true).test()

        testObserverRead.assertError {
            it is DeviceNotConnectedException
        }
        testObserverWrite.assertError {
            it is DeviceNotConnectedException
        }
        testObserverNotify.assertError {
            it is DeviceNotConnectedException
        }
    }

    @Test
    fun testCharacteristicWrite() {
        val gatt: BluetoothGatt = mockk()
        every { gatt.discoverServices() } returns true
        every { gatt.disconnect() } just runs
        every { gatt.close() } just runs
        val (device, callbackSlot) = createMockDevice(gatt)

        val gattCompat: GattCompat = mockk()
        every { gattCompat.disconnect() } just runs
        every { gattCompat.isActive } returns true
        every { gattCompat.write(any(), any()) } returns true
        every { gattCompat.assignNewGatt(any()) } just runs

        mgr = DeviceConnectionManager(device, gattCompat)

        val testObserver = mgr.connectAndObserveEvents().test()

        val callback = callbackSlot.captured

        callback.onConnectionStateChange(gatt, BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTING)
        callback.onConnectionStateChange(gatt, BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTED)

        every { gatt.services } returns null
        callback.onServicesDiscovered(gatt, BluetoothGatt.GATT_FAILURE)
        every { gatt.services } returns emptyList()
        callback.onServicesDiscovered(gatt, BluetoothGatt.GATT_SUCCESS)

        val char1 = createMockCharacteristic(0)
        mgr.writeCharacteristic(char1, "data1".toByteArray())

        callback.onCharacteristicWrite(gatt, char1, BluetoothGatt.GATT_SUCCESS)

        // Another one with failure
        val char2 = createMockCharacteristic(1)
        callback.onCharacteristicWrite(gatt, char2, BluetoothGatt.GATT_FAILURE)

        callback.onConnectionStateChange(gatt, BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_DISCONNECTED)

        testObserver.assertNoErrors()
        testObserver.assertValueCount(8) // 3 + initial connecting + 2 write responses + 2 service responses
        testObserver.assertComplete()
    }

    @Test
    fun testCharacteristicReadAndNotification() {
        val gatt: BluetoothGatt = mockk()
        every { gatt.discoverServices() } returns true
        every { gatt.disconnect() } just runs
        every { gatt.close() } just runs
        val (device, callbackSlot) = createMockDevice(gatt)

        val gattCompat: GattCompat = mockk()
        every { gattCompat.disconnect() } just runs
        every { gattCompat.isActive } returns true

        every { gattCompat.assignNewGatt(any()) } just runs


        mgr = DeviceConnectionManager(device, gattCompat)

        val testObserver = mgr.connectAndObserveEvents().test()

        val callback = callbackSlot.captured

        callback.onConnectionStateChange(gatt, BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTING)
        callback.onConnectionStateChange(gatt, BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTED)

        every { gatt.services } returns emptyList()
        callback.onServicesDiscovered(gatt, BluetoothGatt.GATT_SUCCESS)

        val char1 = createMockCharacteristic(0)
        every { gattCompat.read(char1) } returns true
        mgr.readCharacteristic(char1)
        callback.onCharacteristicRead(gatt, char1, "value".toByteArray(), BluetoothGatt.GATT_SUCCESS)

        // Another one with failure
        val char2 = createMockCharacteristic(1)
        every { gattCompat.read(char2) } returns true
        mgr.readCharacteristic(char2)
        callback.onCharacteristicRead(gatt, char2, "value".toByteArray(), BluetoothGatt.GATT_FAILURE)

        val char3 = createMockCharacteristic(2)
        every { gattCompat.write(char3, any()) } returns true
        mgr.writeCharacteristic(char3, "data1".toByteArray())
        callback.onCharacteristicRead(gatt, char3, "value".toByteArray(), BluetoothGatt.GATT_SUCCESS)

        val char4 = createMockCharacteristic(3)
        every { gattCompat.setNotification(char4, any()) } returns true
        mgr.setNotification(char4, false)
        val char5 = createMockCharacteristic(4)
        every { gattCompat.setNotification(char5, any()) } returns true
        val notifObserver = mgr.setNotificationAndAwait(char5, true).test()
        callback.onCharacteristicChanged(gatt, char5, "value5".toByteArray())

        val char6 = createMockCharacteristic(5)
        every { gattCompat.setNotification(char6, any()) } returns false
        val notifObserverFails = mgr.setNotificationAndAwait(char6, false).test()

        val char7 = createMockCharacteristic(6)
        every { gattCompat.write(char7, any()) } returns false
        val writeObserverFails = mgr.writeCharacteristicAndAwait(char7, "data2".toByteArray()).test()

        val char8 = createMockCharacteristic(7)
        every { gattCompat.read(char8) } returns false
        val readObserverFails = mgr.readCharacteristicAndAwait(char8).test()

        readObserverFails.assertError {
            it is CharacteristicOperationFailed
        }

        writeObserverFails.assertError {
            it is CharacteristicOperationFailed
        }

        notifObserverFails.assertError {
            it is CharacteristicOperationFailed
        }

        notifObserver.assertNoErrors()
        notifObserver.assertComplete()
        notifObserver.assertValue {
            String(it.value) == "value5"
        }

        callback.onConnectionStateChange(gatt, BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_DISCONNECTED)

        testObserver.assertNoErrors()
        testObserver.assertValueCount(9) // 3 + initial connecting + 3 read responses + 1 service response + 1 change response
        testObserver.assertComplete()
    }

    private fun createMockCharacteristic(index: Int): BluetoothGattCharacteristic {
        val id = "46b254df-672e-49fd-aabb-99172d63a7e$index"
        val char: BluetoothGattCharacteristic = mockk()
        every { char.uuid } returns UUID.fromString(id)
        return char
    }

    private fun createMockDevice(gatt: BluetoothGatt, name: String = "name"): Pair<BLEDeviceWrapper, CapturingSlot<BluetoothGattCallback>> {
        val device = mockk<BLEDeviceWrapper>()
        val slot = CapturingSlot<BluetoothGattCallback>()
        every { device.connect(capture(slot)) } answers {
            gatt
        }
        every { device.name() } returns name
        return device to slot
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }
}
