package com.zeros.basheer.data.local.dao

import androidx.room.*
import com.zeros.basheer.data.models.Units  // Already correct
import kotlinx.coroutines.flow.Flow

@Dao
interface UnitDao {
    @Query("SELECT * FROM units WHERE subjectId = :subjectId ORDER BY `order`")
    fun getUnitsBySubject(subjectId: String): Flow<List<Units>>  // CHANGED

    @Query("SELECT * FROM units WHERE id = :unitId")
    suspend fun getUnitById(unitId: String): Units?  // CHANGED

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unit: Units)  // CHANGED

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnits(units: List<Units>)  // CHANGED

    @Delete
    suspend fun deleteUnit(unit: Units)  // CHANGED

    @Query("DELETE FROM units WHERE subjectId = :subjectId")
    suspend fun deleteUnitsBySubject(subjectId: String)
}