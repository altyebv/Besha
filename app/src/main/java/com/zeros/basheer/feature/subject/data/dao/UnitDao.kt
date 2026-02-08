package com.zeros.basheer.feature.subject.data.dao

import androidx.room.*
import com.zeros.basheer.data.relations.UnitWithLessons
import com.zeros.basheer.feature.subject.data.entity.UnitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnitDao {
    @Query("SELECT * FROM units WHERE subjectId = :subjectId ORDER BY `order`")
    fun getUnitsBySubject(subjectId: String): Flow<List<UnitEntity>>

    @Query("SELECT * FROM units WHERE id = :unitId")
    suspend fun getUnitById(unitId: String): UnitEntity?

    @Transaction
    @Query("SELECT * FROM units WHERE id = :unitId")
    suspend fun getUnitWithLessons(unitId: String): UnitWithLessons?

    @Transaction
    @Query("SELECT * FROM units WHERE subjectId = :subjectId ORDER BY `order`")
    fun getUnitsWithLessons(subjectId: String): Flow<List<UnitWithLessons>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unit: UnitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnits(units: List<UnitEntity>)

    @Delete
    suspend fun deleteUnit(unit: UnitEntity)

    @Query("DELETE FROM units WHERE subjectId = :subjectId")
    suspend fun deleteUnitsBySubject(subjectId: String)
}
