package com.example.lairsheet.ui.create

import com.example.lairsheet.data.DndClass
import com.example.lairsheet.data.Race
import com.example.lairsheet.data.Subclass
import com.example.lairsheet.data.Subrace
import com.example.lairsheet.data.AbilityScoreState

/**
 * Черновик персонажа для создания персонажа.
 */
data class CharacterDraft(
    // Шаг 1: Основная информация
    val playerName: String = "",
    val race: Race? = null,
    val subrace: Subrace? = null,
    val clazz: DndClass? = null,
    val subclass: Subclass? = null,

    // Шаг 2: Характеристики
    val abilityScoreState: AbilityScoreState = AbilityScoreState(),

    // Навигация
    val currentStep: Int = 1
)
