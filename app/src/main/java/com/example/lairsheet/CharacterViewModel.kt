package com.example.lairsheet

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import android.net.Uri
import org.json.JSONObject
import com.example.lairsheet.data.AppDatabase
import com.example.lairsheet.data.Character
import com.example.lairsheet.data.Ruleset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CharacterViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "characters.db"
    ).fallbackToDestructiveMigration().build().characterDao()

    fun characters(ruleset: Ruleset): Flow<List<Character>> = dao.charactersByRuleset(ruleset)

    fun addCharacter(character: Character) {
        viewModelScope.launch {
            dao.insert(character)
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
                    className = obj.getString("class"),
                    race = obj.getString("race"),
                    level = obj.getInt("level"),
                    background = obj.optString("background"),
                    alignment = obj.optString("alignment"),
                    strength = obj.optInt("strength"),
                    dexterity = obj.optInt("dexterity"),
                    constitution = obj.optInt("constitution"),
                    intelligence = obj.optInt("intelligence"),
                    wisdom = obj.optInt("wisdom"),
                    charisma = obj.optInt("charisma"),
                    ruleset = ruleset
                )
                dao.insert(character)
            }
        }
    }
}