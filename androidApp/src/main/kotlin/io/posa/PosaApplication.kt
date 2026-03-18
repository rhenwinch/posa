package io.posa

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.posa.di.KoinInitializer

class PosaApplication : Application() {
    private val koin by lazy { KoinInitializer(this) }

    override fun onCreate() {
        super.onCreate()
        koin.start()
    }
}