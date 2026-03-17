package io.posa.di.database

import androidx.room.RoomDatabaseConstructor
import io.posa.core.database.PosaDatabase

@Suppress("KotlinNoActualForExpect", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object PosaDatabaseConstructor : RoomDatabaseConstructor<PosaDatabase> {
    override fun initialize(): PosaDatabase
}