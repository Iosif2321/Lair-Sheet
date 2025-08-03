package com.example.lairsheet

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.lairsheet.data.AppDatabase
import com.example.lairsheet.data.Character
import com.example.lairsheet.data.Ruleset
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    fun importCharacter(context: Context, uri: Uri) {
        viewModelScope.launch {
            val json = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            } ?: return@launch
            val character = Gson().fromJson(json, Character::class.java)
            dao.insert(character)
        }
    }
}