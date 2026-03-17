package io.posa.di.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.AndroidSQLiteDriver
import io.posa.core.database.PosaDatabase
import org.koin.android.ext.koin.androidContext

actual class PosaDatabaseFactory(
    private val context: Context
) {
    actual fun createDatabase(): RoomDatabase.Builder<PosaDatabase> {
        val dbFile = context.getDatabasePath(PosaDatabase.DATABASE_FILE)

        return Room.databaseBuilder<PosaDatabase>(
            context = context,
            name = dbFile.absolutePath
        ).setDriver(AndroidSQLiteDriver())
    }
}