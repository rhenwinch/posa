package io.posa

import android.app.Application
import io.posa.di.KoinInitializer

class PosaApplication : Application() {
    private val koin by lazy { KoinInitializer(this) }

    override fun onCreate() {
        super.onCreate()
        koin.start()
    }
}