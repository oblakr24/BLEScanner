package com.rokoblak.blescan.di

import com.rokoblak.blescan.devices.DeviceScanner
import com.rokoblak.blescan.devices.DeviceScannerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
interface DeviceManagerModule {

    @Singleton
    @Binds
    fun bindDeviceManager(impl: DeviceScannerImpl): DeviceScanner
}
