package com.rokoblak.blescan.scan

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import com.rokoblak.blescan.exceptions.BTUnavailableException
import com.rokoblak.blescan.exceptions.PermissionNotGrantedException
import com.rokoblak.blescan.exceptions.ScanFailedException
import io.mockk.CapturingSlot
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.time.Duration


@RunWith(JUnit4::class)
class DeviceScannerTest {

    @RelaxedMockK
    lateinit var scanner: DeviceScanner

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun testScanSuccess() {
        // Given: healthy setup
        val mockDevice = mockk<BluetoothDevice>()
        val mockDevice2 = mockk<BluetoothDevice>()
        val scanResult = mockk<ScanResult>()
        val scanResult2 = mockk<ScanResult>()
        every { mockDevice.address } returns "Mock-address1"
        every { mockDevice.name } returns "Mock-name"
        every { mockDevice2.address } returns "Mock-address2"
        every { mockDevice2.name } returns null
        every { scanResult.device } returns mockDevice
        every { scanResult2.device } returns mockDevice2

        val bleScanner: BluetoothLeScanner = mockk()
        val btProvider: BluetoothProvider = mockk()
        val permissionsChecker: PermissionsChecker = mockk()
        val slot = CapturingSlot<ScanCallback>()

        var capturedScanCallback: ScanCallback? = null
        every { bleScanner.startScan(null, any(), capture(slot)) } answers {
            capturedScanCallback = slot.captured
        }
        every { bleScanner.stopScan(capture(slot)) } just runs

        every { permissionsChecker.checkMissingPermissions() } returns null
        every { btProvider.bleScanner } returns bleScanner

        scanner = DeviceScannerImpl(btProvider, permissionsChecker)

        // When: we start scanning
        val testObserver =  scanner.startScanning(createMockSettings()).test()

        // When: we receive two results via the callback
        capturedScanCallback?.onScanResult(0, scanResult)
        capturedScanCallback?.onScanResult(0, scanResult2)

        // Then: we receive these two results
        testObserver.assertNoErrors()
        testObserver.assertValueCount(2)
        testObserver.assertValueAt(0) { dev ->
            dev.deviceAddress == "Mock-address1" && dev.deviceName == "Mock-name"
        }
        testObserver.assertValueAt(1) { dev ->
            dev.deviceAddress == "Mock-address2" && dev.deviceName == "Unknown device"
        }
    }

    @Test
    fun testFailure() {
        // Given: healthy setup
        val mockDevice = mockk<BluetoothDevice>()
        val scanResult = mockk<ScanResult>()
        every { mockDevice.address } returns "Mock-address"
        every { mockDevice.name } returns "Mock-name"
        every { scanResult.device } returns mockDevice

        val bleScanner: BluetoothLeScanner = mockk()
        val btProvider: BluetoothProvider = mockk()
        val permissionsChecker: PermissionsChecker = mockk()
        val slot = CapturingSlot<ScanCallback>()

        var capturedScanCallback: ScanCallback? = null
        every { bleScanner.startScan(null, any(), capture(slot)) } answers {
            capturedScanCallback = slot.captured
        }
        every { bleScanner.stopScan(capture(slot)) } just runs

        every { permissionsChecker.checkMissingPermissions() } returns null
        every { btProvider.bleScanner } returns bleScanner

        scanner = DeviceScannerImpl(btProvider, permissionsChecker)

        // When: we start scanning
        val testObserver =  scanner.startScanning(createMockSettings()).test()

        // When: we get a failure via the callback
        capturedScanCallback?.onScanFailed(0)

        // Then: the observer returns an error
        testObserver.assertError {
            it is ScanFailedException
        }
    }

    @Test
    fun testStopScanningCompletes() {
        // Given: healthy setup
        val mockDevice = mockk<BluetoothDevice>()
        val scanResult = mockk<ScanResult>()
        every { mockDevice.address } returns "Mock-address"
        every { mockDevice.name } returns "Mock-name"
        every { scanResult.device } returns mockDevice

        val bleScanner: BluetoothLeScanner = mockk()
        val btProvider: BluetoothProvider = mockk()
        val permissionsChecker: PermissionsChecker = mockk()

        every { bleScanner.startScan(null, any(), any<ScanCallback>()) } just runs
        every { bleScanner.stopScan(any<ScanCallback>()) } just runs

        every { permissionsChecker.checkMissingPermissions() } returns null
        every { btProvider.bleScanner } returns bleScanner

        scanner = DeviceScannerImpl(btProvider, permissionsChecker)

        // When: we start scanning
        val testObserver =  scanner.startScanning(createMockSettings()).test()
        // When: we stop scanning
        scanner.stopScanning()

        // Then: the observer completes without an error
        testObserver.assertNoValues()
        testObserver.assertComplete()
    }

    @Test
    fun testNoPermissions() {
        val bleScanner: BluetoothLeScanner = mockk()
        val btProvider: BluetoothProvider = mockk()
        val permissionsChecker: PermissionsChecker = mockk()

        every { permissionsChecker.checkMissingPermissions() } returns PermissionNotGrantedException("Anything")
        every { btProvider.bleScanner } returns bleScanner

        scanner = DeviceScannerImpl(btProvider, permissionsChecker)

        val testObserver =  scanner.startScanning().test()

        testObserver.assertError {
            it is PermissionNotGrantedException
        }
    }

    @Test
    fun testNoScannerReturnsError() {
        // Given: setup without permissions
        val permissionsChecker: PermissionsChecker = mockk()
        val btProvider: BluetoothProvider = mockk()
        every { permissionsChecker.checkMissingPermissions() } returns null
        every { btProvider.bleScanner } returns null

        scanner = DeviceScannerImpl(btProvider, permissionsChecker)

        // When: we start scanning
        val testObserver =  scanner.startScanning().test()

        // Then: we get an error
        testObserver.assertError {
            it is BTUnavailableException
        }
    }

    @Test
    fun enableAndSupportedShouldQueryProvider() {
        // Given: healthy setup
        val permissionsChecker: PermissionsChecker = mockk()
        val bleScanner: BluetoothLeScanner = mockk()
        val btProvider: BluetoothProvider = mockk()

        // When: the provider answers about bt state
        every { btProvider.enabled() } returns false
        every { btProvider.supported() } returns true
        every { btProvider.bleScanner } returns bleScanner

        scanner = DeviceScannerImpl(btProvider, permissionsChecker)

        // Then: the scanner answers accordingly
        assertEquals(false, scanner.enabled())
        assertEquals(true, scanner.supported())
    }

    private fun createMockSettings(): BleScanSettings {
        return mockk<BleScanSettings>().apply {
            every { timeout } returns Duration.ofSeconds(8)
            every { resolve() } returns mockk()
        }
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }
}
