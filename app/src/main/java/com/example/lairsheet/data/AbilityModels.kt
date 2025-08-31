package com.example.lairsheet.data

enum class AbilityType {
    STRENGTH,
    DEXTERITY,
    CONSTITUTION,
    INTELLIGENCE,
    WISDOM,
    CHARISMA
}

data class AbilityScore(
    val type: AbilityType,
    val value: Int
)

data class Subclass(
    val name: String,
    val classIdentifier: String
)