package com.zeros.basheer.data.local.dao

import androidx.room.*
import com.zeros.basheer.data.models.Units
import com.zeros.basheer.data.relations.UnitWithLessons
import kotlinx.coroutines.flow.Flow

@Dao
interface UnitDao {
    @Query("SELECT * FROM units WHERE subjectId = :subjectId ORDER BY `order`")
    fun getUnitsBySubject(subjectId: String): Flow<List<Units>>

    @Query("SELECT * FROM units WHERE id = :unitId")
    suspend fun getUnitById(unitId: String): Units?

    @Transaction
    @Query("SELECT * FROM units WHERE id = :unitId")
    suspend fun getUnitWithLessons(unitId: String): UnitWithLessons?

    @Transaction
    @Query("SELECT * FROM units WHERE subjectId = :subjectId ORDER BY `order`")
    fun getUnitsWithLessons(subjectId: String): Flow<List<UnitWithLessons>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unit: Units)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnits(units: List<Units>)

    @Delete
    suspend fun deleteUnit(unit: Units)

    @Query("DELETE FROM units WHERE subjectId = :subjectId")
    suspend fun deleteUnitsBySubject(subjectId: String)
}
