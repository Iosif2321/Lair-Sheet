package com.example.lairsheet.data

import android.content.Context
import androidx.room.Room

object Db {
    @Volatile private var INSTANCE: AppDatabase? = null

    fun get(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "lairsheet.db"
            )
                .addMigrations(
                    AppDatabase.MIGRATION_1_2,
                    AppDatabase.MIGRATION_2_3
                )
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
                .also { INSTANCE = it }
        }
    }
}
