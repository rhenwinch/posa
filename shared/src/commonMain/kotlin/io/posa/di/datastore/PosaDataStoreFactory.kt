package io.posa.di.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

expect class PosaDataStoreFactory {
    fun createDataStore(): DataStore<Preferences>
}