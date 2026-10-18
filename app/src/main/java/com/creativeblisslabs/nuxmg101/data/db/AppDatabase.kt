package com.creativeblisslabs.nuxmg101.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.creativeblisslabs.nuxmg101.data.model.CabinetIR
import com.creativeblisslabs.nuxmg101.data.model.MidiMappingEntry
import com.creativeblisslabs.nuxmg101.data.model.Patch

@Database(entities = [Patch::class, CabinetIR::class, MidiMappingEntry::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun patchDao(): PatchDao
    abstract fun irDao(): IRDao
    abstract fun midiMappingDao(): MidiMappingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quicktone_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
