package com.example.lairsheet.data

data class AbilityScoreState(
    val strength: Int = 10,
    val dexterity: Int = 10,
    val constitution: Int = 10,
    val intelligence: Int = 10,
    val wisdom: Int = 10,
    val charisma: Int = 10
) {
    val totalPoints: Int
        get() = strength + dexterity + constitution + intelligence + wisdom + charisma

    fun updateStrength(value: Int) = copy(strength = value)
    fun updateDexterity(value: Int) = copy(dexterity = value)
    fun updateConstitution(value: Int) = copy(constitution = value)
    fun updateIntelligence(value: Int) = copy(intelligence = value)
    fun updateWisdom(value: Int) = copy(wisdom = value)
    fun updateCharisma(value: Int) = copy(charisma = value)
}