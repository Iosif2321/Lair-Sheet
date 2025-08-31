package com.example.lairsheet.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.lairsheet.ui.create.Race
import com.example.lairsheet.ui.create.Subrace
import com.example.lairsheet.data.dnd.DndClass
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "characters")
@TypeConverters(Converters::class)
data class Character(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val race: Race,
    val subrace: Subrace?,
    val characterClass: DndClass,
    val subclass: Subclass?,
    // Метод начисления характеристик (routeKey), нужен для восстановления
    val abilityMethod: String? = null,
    val abilityScores: Map<AbilityType, AbilityScore>,
    val ruleset: Ruleset = Ruleset.R5E_2024,
    val creationStep: Int = 0, // 0: draft, 1: basic info, 2: abilities, 3: completed
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)