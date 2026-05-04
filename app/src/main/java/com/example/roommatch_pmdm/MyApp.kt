package com.example.roommatch_pmdm

import android.app.Application
import com.example.roommatch_pmdm.di.appModule
import com.example.roommatch_pmdm.notifications.NotificationHelper
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
        startKoin {
            androidContext(this@MyApp)
            modules(appModule)
        }
    }
}
