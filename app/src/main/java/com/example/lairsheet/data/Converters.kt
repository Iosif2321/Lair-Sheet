package com.example.lairsheet.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromRuleset(value: Ruleset): String = value.name

    @TypeConverter
    fun toRuleset(value: String): Ruleset = Ruleset.valueOf(value)
}