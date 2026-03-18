package io.posa.di.datastore

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import io.posa.core.common.AppDispatchers
import io.posa.core.datastore.PosaDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okio.Path.Companion.toPath

actual class PosaDataStoreFactory(
    private val context: Context
) {
    private val scope by lazy { CoroutineScope(AppDispatchers.IO) }

    actual fun createDataStore(): DataStore<Preferences> {
        val dataStore = PreferenceDataStoreFactory.createWithPath(
            produceFile = {
                context.filesDir
                    .resolve(PosaDataStore.DATASTORE_NAME)
                    .absolutePath
                    .toPath()
            }
        )

        scope.launch {
            dataStore.edit { prefs ->
                if (PosaDataStore.PREF_USER_ID in prefs) return@edit

                prefs[PosaDataStore.PREF_USER_ID] = "apposa:demo-${System.currentTimeMillis()}"
            }
        }

        return dataStore
    }
}