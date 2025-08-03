package com.example.lairsheet

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
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
    ).build().characterDao()

    fun characters(ruleset: Ruleset): Flow<List<Character>> = dao.charactersByRuleset(ruleset)

    fun addCharacter(name: String, subtitle: String, ruleset: Ruleset) {
        viewModelScope.launch {
            dao.insert(Character(name = name, subtitle = subtitle, ruleset = ruleset))
        }
    }
}