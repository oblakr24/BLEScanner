package com.rokoblak.blescan.di

import com.rokoblak.blescan.scan.DeviceScanner
import com.rokoblak.blescan.scan.DeviceScannerImpl
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
