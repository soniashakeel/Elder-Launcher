package com.elder.launcher.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [InstanceEntity::class], version = 1, exportSchema = false)
abstract class ElderDatabase : RoomDatabase() {
    abstract fun instanceDao(): InstanceDao

    companion object {
        @Volatile private var instance: ElderDatabase? = null

        fun get(context: Context): ElderDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ElderDatabase::class.java,
                    "elder_launcher.db"
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
    }
}