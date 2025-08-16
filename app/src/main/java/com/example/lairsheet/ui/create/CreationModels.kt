package com.example.lairsheet.ui.create

/**
 * Набор моделей и общего состояния мастера создания персонажа.
 */
enum class Ruleset { PHB2014, PHB2024, EXPANDED }

data class Subrace(val nameRu: String, val nameEn: String)
data class Race(
    val nameRu: String,
    val nameEn: String,
    val subraces: List<Subrace> = emptyList()
)

data class CharacterCreationState(
    val selectedSources: Set<Ruleset> = setOf(Ruleset.PHB2014),
    val selectedRace: Race? = null
)
