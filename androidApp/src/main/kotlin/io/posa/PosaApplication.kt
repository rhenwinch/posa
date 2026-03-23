package io.posa

import android.app.Application
import io.posa.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class PosaApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@PosaApplication)
            androidLogger()
        }
    }
}