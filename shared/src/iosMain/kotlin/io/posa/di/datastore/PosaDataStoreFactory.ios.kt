package io.posa.di.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import io.posa.core.common.AppDispatchers
import io.posa.core.datastore.PosaDataStore
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import kotlin.getValue
import kotlin.time.Clock

actual class PosaDataStoreFactory {
    private val scope by lazy { CoroutineScope(AppDispatchers.IO) }
    actual fun createDataStore(): DataStore<Preferences> {
        val dataStore = PreferenceDataStoreFactory.createWithPath(
            produceFile = { getDatastoreFile().toPath() }
        )

        scope.launch {
            dataStore.edit { prefs ->
                if (PosaDataStore.PREF_USER_ID in prefs) return@edit

                prefs[PosaDataStore.PREF_USER_ID] = "apposa:demo-${Clock.System.now().toEpochMilliseconds()}"
            }
        }

        return dataStore
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun getDatastoreFile(): String {
        val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )

        return requireNotNull(documentDirectory).path + "/${PosaDataStore.DATASTORE_NAME}"
    }
}