package io.posa.di.database

import androidx.room.RoomDatabase
import io.posa.core.database.PosaDatabase

expect class PosaDatabaseFactory {
    fun createDatabase(): RoomDatabase.Builder<PosaDatabase>
}