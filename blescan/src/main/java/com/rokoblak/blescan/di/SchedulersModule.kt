package com.rokoblak.blescan.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers


@Module
@InstallIn(SingletonComponent::class)
object SchedulersModule {

    @Provides
    fun provideTimeScheduler(): Scheduler {
        return Schedulers.computation()
    }
}
