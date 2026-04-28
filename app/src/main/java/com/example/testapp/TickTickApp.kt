package com.example.testapp

import android.app.Application
import com.example.testapp.di.appModule
import com.example.testapp.notification.NotificationChannels
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class TickTickApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.INFO)
            androidContext(this@TickTickApp)
            modules(appModule)
        }
        NotificationChannels.create(this)
    }
}
