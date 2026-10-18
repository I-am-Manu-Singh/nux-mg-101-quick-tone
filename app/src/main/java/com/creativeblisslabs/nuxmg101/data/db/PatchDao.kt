package com.creativeblisslabs.nuxmg101.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.creativeblisslabs.nuxmg101.data.model.Patch
import kotlinx.coroutines.flow.Flow

@Dao
interface PatchDao {
    @Query("SELECT * FROM patches ORDER BY id ASC")
    fun getAllPatches(): Flow<List<Patch>>

    @Query("SELECT * FROM patches WHERE id = :id LIMIT 1")
    suspend fun getPatchById(id: String): Patch?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatches(patches: List<Patch>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatch(patch: Patch)

    @Update
    suspend fun updatePatch(patch: Patch)

    @Query("UPDATE patches SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("DELETE FROM patches WHERE id = :id")
    suspend fun deletePatch(id: String)

    @Query("DELETE FROM patches")
    suspend fun deleteAllPatches()
}
