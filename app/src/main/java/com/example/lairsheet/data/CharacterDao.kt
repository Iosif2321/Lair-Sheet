package com.example.lairsheet.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {
    @Query("SELECT * FROM characters WHERE ruleset = :ruleset")
    fun charactersByRuleset(ruleset: Ruleset): Flow<List<Character>>

    @Insert
    suspend fun insert(character: Character)
}
