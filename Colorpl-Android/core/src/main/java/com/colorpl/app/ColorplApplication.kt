package com.colorpl.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.colorpl.BuildConfig
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import com.naver.maps.map.NaverMapSdk
import kr.co.bootpay.android.Bootpay
import kr.co.bootpay.android.constants.BootpayBuildConfig
import kr.co.bootpay.android.constants.BootpayConstant
import javax.inject.Inject

@HiltAndroidApp
class ColorplApplication : Application(), Configuration.Provider {


    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface HiltWorkerFactoryEntryPoint {
        fun workerFactory(): HiltWorkerFactory
    }

    override val workManagerConfiguration: Configuration =
        Configuration.Builder()
             .setWorkerFactory(EntryPoints.get(this, HiltWorkerFactoryEntryPoint::class.java).workerFactory())
            .build()


    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        Timber.plant(Timber.DebugTree())
        // NaverMapSdk
        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NaverCloudPlatformClient(BuildConfig.NAVER_MAP_CLIENT_ID)

        WorkManager.initialize(this, workManagerConfiguration)
    }


}