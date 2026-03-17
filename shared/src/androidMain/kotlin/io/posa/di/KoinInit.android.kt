package io.posa.di

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.KoinApplication
import org.koin.dsl.KoinAppDeclaration


actual class KoinInitializer(
    private val app: Application
) {
    actual fun start() {
        initKoin {
            androidContext(app)
            androidLogger()
        }
    }
}