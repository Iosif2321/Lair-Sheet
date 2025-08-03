package com.example.lairsheet.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters")
data class Character(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val className: String,
    val race: String,
    val level: Int,
    val background: String,
    val alignment: String,
    val strength: Int,
    val dexterity: Int,
    val constitution: Int,
    val intelligence: Int,
    val wisdom: Int,
    val charisma: Int,
    val ruleset: Ruleset
)