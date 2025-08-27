package com.example.lairsheet.ui.create

data class CharacterState(
    val name: String = "",
    val selectedSources: List<String> = emptyList(),
    val selectedRace: String = "",
    val selectedSubrace: String = "",
    val selectedClass: String = "",
    val selectedSubclass: String = "",
    val strength: Int = 10,
    val dexterity: Int = 10,
    val constitution: Int = 10,
    val intelligence: Int = 10,
    val wisdom: Int = 10,
    val charisma: Int = 10,
    val selectedBackground: String = "",
    val selectedEquipment: List<String> = emptyList()
)
