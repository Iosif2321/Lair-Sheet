package com.example.lairsheet.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {

    @Query("SELECT * FROM characters ORDER BY updatedAt DESC")
    fun getAllCharacters(): Flow<List<Character>>

    @Query("SELECT * FROM characters WHERE id = :id")
    suspend fun getCharacterById(id: Long): Character?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: Character): Long

    @Update
    suspend fun updateCharacter(character: Character)

    @Delete
    suspend fun deleteCharacter(character: Character)

    @Query("DELETE FROM characters WHERE id = :id")
    suspend fun deleteCharacterById(id: Long)

    @Query("DELETE FROM characters")
    suspend fun deleteAllCharacters()

    @Query("SELECT COUNT(*) FROM characters")
    suspend fun getCharactersCount(): Int

    @Query("SELECT * FROM characters WHERE ruleset = :ruleset ORDER BY updatedAt DESC")
    fun getCharactersByRuleset(ruleset: Ruleset): Flow<List<Character>>
}