package com.example.lairsheet

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import org.json.JSONObject
import com.example.lairsheet.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CharacterViewModel(application: Application) : AndroidViewModel(application) {
    private val characterRepository = CharacterRepository(
        AppDatabase.getDatabase(application).characterDao()
    )

    fun characters(ruleset: Ruleset): Flow<List<Character>> = characterRepository.getCharactersByRuleset(ruleset)

    fun addCharacter(character: Character) {
        viewModelScope.launch {
            characterRepository.insertCharacter(character)
        }
    }

    fun importCharacter(uri: Uri, ruleset: Ruleset) {
        val ctx = getApplication<Application>()
        viewModelScope.launch {
            ctx.contentResolver.openInputStream(uri)?.use { input ->
                val text = input.bufferedReader().use { it.readText() }
                val obj = JSONObject(text)
                val character = Character(
                    name = obj.getString("name"),
                    race = Race(obj.optString("race", "Человек"), "Описание расы"),
                    subrace = null,
                    characterClass = DndClass(obj.optString("class", "Воин"), "Описание класса"),
                    subclass = null,
                    abilityScores = mapOf(
                        AbilityType.STRENGTH to AbilityScore(AbilityType.STRENGTH, obj.optInt("strength", 10)),
                        AbilityType.DEXTERITY to AbilityScore(AbilityType.DEXTERITY, obj.optInt("dexterity", 10)),
                        AbilityType.CONSTITUTION to AbilityScore(AbilityType.CONSTITUTION, obj.optInt("constitution", 10)),
                        AbilityType.INTELLIGENCE to AbilityScore(AbilityType.INTELLIGENCE, obj.optInt("intelligence", 10)),
                        AbilityType.WISDOM to AbilityScore(AbilityType.WISDOM, obj.optInt("wisdom", 10)),
                        AbilityType.CHARISMA to AbilityScore(AbilityType.CHARISMA, obj.optInt("charisma", 10))
                    ),
                    ruleset = ruleset,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                characterRepository.insertCharacter(character)
            }
        }
    }
}