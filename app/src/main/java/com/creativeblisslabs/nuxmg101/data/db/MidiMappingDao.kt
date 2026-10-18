package com.creativeblisslabs.nuxmg101.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.creativeblisslabs.nuxmg101.data.model.MidiMappingEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface MidiMappingDao {
    @Query("SELECT * FROM midi_mappings ORDER BY category ASC, displayOrder ASC, name ASC")
    fun getAllMappings(): Flow<List<MidiMappingEntry>>

    @Query("SELECT * FROM midi_mappings WHERE category = :category ORDER BY displayOrder ASC, id ASC")
    fun getMappingsByCategory(category: String): Flow<List<MidiMappingEntry>>

    @Query("SELECT * FROM midi_mappings WHERE category = :category AND name = :name LIMIT 1")
    suspend fun getMappingByName(category: String, name: String): MidiMappingEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMappings(mappings: List<MidiMappingEntry>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMapping(mapping: MidiMappingEntry)

    @Update
    suspend fun updateMapping(mapping: MidiMappingEntry)

    @Query("DELETE FROM midi_mappings WHERE id = :id")
    suspend fun deleteMapping(id: String)

    @Query("DELETE FROM midi_mappings")
    suspend fun deleteAllMappings()
}
