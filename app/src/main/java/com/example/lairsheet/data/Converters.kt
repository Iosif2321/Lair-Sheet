package com.example.lairsheet.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.lairsheet.ui.create.Race
import com.example.lairsheet.ui.create.Subrace
import com.example.lairsheet.data.dnd.DndClass

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromRuleset(value: Ruleset): String = value.name

    @TypeConverter
    fun toRuleset(value: String): Ruleset = Ruleset.valueOf(value)

    // Ability Scores Map
    @TypeConverter
    fun fromAbilityScoresMap(scores: Map<AbilityType, AbilityScore>): String {
        return gson.toJson(scores)
    }

    @TypeConverter
    fun toAbilityScoresMap(json: String): Map<AbilityType, AbilityScore> {
        val type = object : TypeToken<Map<AbilityType, AbilityScore>>() {}.type
        return gson.fromJson(json, type)
    }

    // Race
    @TypeConverter
    fun fromRace(race: Race): String = gson.toJson(race)

    @TypeConverter
    fun toRace(json: String): Race = gson.fromJson(json, Race::class.java)

    // Subrace (nullable)
    @TypeConverter
    fun fromSubrace(subrace: Subrace?): String? = subrace?.let { gson.toJson(it) }

    @TypeConverter
    fun toSubrace(json: String?): Subrace? = json?.let { gson.fromJson(it, Subrace::class.java) }

    // DndClass
    @TypeConverter
    fun fromDndClass(dndClass: DndClass): String = gson.toJson(dndClass)

    @TypeConverter
    fun toDndClass(json: String): DndClass = gson.fromJson(json, DndClass::class.java)

    // Subclass (nullable)
    @TypeConverter
    fun fromSubclass(subclass: Subclass?): String? = subclass?.let { gson.toJson(it) }

    @TypeConverter
    fun toSubclass(json: String?): Subclass? = json?.let { gson.fromJson(it, Subclass::class.java) }

    // List<String> (equipment items)
    @TypeConverter
    fun fromStringList(list: List<String>): String = gson.toJson(list)

    @TypeConverter
    fun toStringList(json: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type)
    }
}