package com.you4me.you4me.core

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.you4me.you4me.model.User

@Database(entities = [User::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun UserDAO(): UserDAO

    companion object {
        private const val DATABASE_NAME = "you4me_database.db"

        @Volatile private var instance: AppDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) =
            instance ?: synchronized(LOCK) {
                instance ?: buildDatabase(context).also {
                    instance = it
                }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME,
            ).fallbackToDestructiveMigration().build()
    }
}
