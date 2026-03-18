package io.posa.core.datastore

import androidx.datastore.preferences.core.stringPreferencesKey

object PosaDataStore {
    const val DATASTORE_NAME = "posa_datastore.preferences_pb"

    val PREF_USER_ID = stringPreferencesKey("user_id")
}