package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Household::class, Contribution::class, Ceremony::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val householdDao: HouseholdDao
    abstract val contributionDao: ContributionDao
    abstract val ceremonyDao: CeremonyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "abc_church_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
