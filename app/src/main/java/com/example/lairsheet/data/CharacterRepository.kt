package com.example.lairsheet.data

import kotlinx.coroutines.flow.Flow

class CharacterRepository(private val characterDao: CharacterDao) {

    fun getCharactersByRuleset(ruleset: Ruleset): Flow<List<Character>> =
        characterDao.getCharactersByRuleset(ruleset)

    suspend fun insertCharacter(character: Character): Long =
        characterDao.insertCharacter(character)

    suspend fun updateCharacter(character: Character) =
        characterDao.updateCharacter(character)

    suspend fun deleteCharacter(character: Character) =
        characterDao.deleteCharacter(character)

    suspend fun deleteCharacterById(id: Long) =
        characterDao.deleteCharacterById(id)

    suspend fun getCharacterById(id: Long): Character? =
        characterDao.getCharacterById(id)

    suspend fun getCharactersCount(): Int =
        characterDao.getCharactersCount()
}