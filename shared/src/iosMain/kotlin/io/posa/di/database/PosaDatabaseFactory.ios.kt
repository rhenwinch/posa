package io.posa.di.database

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.NativeSQLiteDriver
import io.posa.core.database.PosaDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual class PosaDatabaseFactory {
    actual fun createDatabase(): RoomDatabase.Builder<PosaDatabase> {
        val dbFilePath = documentDirectory() + "/${PosaDatabase.DATABASE_FILE}"
        return Room.databaseBuilder<PosaDatabase>(name = dbFilePath)
            .setDriver(NativeSQLiteDriver())
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun documentDirectory(): String {
        val documentDirectory = NSFileManager.Companion.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )

        return requireNotNull(documentDirectory?.path)
    }
}