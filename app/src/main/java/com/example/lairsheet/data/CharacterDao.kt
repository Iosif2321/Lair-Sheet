package com.example.lairsheet.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {

    @Query("SELECT * FROM characters WHERE ruleset = :ruleset ORDER BY id DESC")
    fun charactersByRuleset(ruleset: Ruleset): Flow<List<Character>>

    @Query("SELECT * FROM characters ORDER BY id DESC")
    fun all(): Flow<List<Character>>

    @Query("SELECT * FROM characters WHERE id = :id")
    suspend fun findById(id: Long): Character?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(character: Character): Long

    @Update(onConflict = OnConflictStrategy.ABORT)
    suspend fun update(character: Character)

    @Delete
    suspend fun delete(character: Character)

    @Query("DELETE FROM characters WHERE id = :id")
    suspend fun deleteById(id: Long)
}
