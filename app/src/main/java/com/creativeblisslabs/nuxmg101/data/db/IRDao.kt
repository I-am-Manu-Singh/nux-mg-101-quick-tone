package com.creativeblisslabs.nuxmg101.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.creativeblisslabs.nuxmg101.data.model.CabinetIR
import kotlinx.coroutines.flow.Flow

@Dao
interface IRDao {
    @Query("SELECT * FROM ir_files ORDER BY slotIndex ASC")
    fun getAllIRs(): Flow<List<CabinetIR>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIRs(irs: List<CabinetIR>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIR(ir: CabinetIR)

    @Query("UPDATE ir_files SET isActiveSlot = CASE WHEN slotIndex = :activeSlot THEN 1 ELSE 0 END")
    suspend fun setActiveIRSlot(activeSlot: String)

    @Update
    suspend fun updateIR(ir: CabinetIR)
}
