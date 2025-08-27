package com.example.lairsheet.data.dnd

import com.google.gson.annotations.SerializedName

// Модель расы
data class DndRace(
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String,
    @SerializedName("system") val system: RaceSystem
)

data class RaceSystem(
    @SerializedName("description") val description: Description,
    @SerializedName("source") val source: Source,
    @SerializedName("advancement") val advancement: List<AdvancementItem>
)

data class Description(
    @SerializedName("value") val value: String
)

data class Source(
    @SerializedName("book") val book: String,
    @SerializedName("rules") val rules: String
)

// Модель класса
data class DndClass(
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String,
    @SerializedName("system") val system: ClassSystem
)

data class ClassSystem(
    @SerializedName("description") val description: Description,
    @SerializedName("source") val source: Source,
    @SerializedName("advancement") val advancement: List<AdvancementItem>,
    @SerializedName("spellcasting") val spellcasting: Spellcasting?
)

data class Spellcasting(
    @SerializedName("progression") val progression: String,
    @SerializedName("ability") val ability: String
)

// Модель подкласса
data class DndSubclass(
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String,
    @SerializedName("classIdentifier") val classIdentifier: String,
    @SerializedName("system") val system: SubclassSystem
)

data class SubclassSystem(
    @SerializedName("description") val description: Description,
    @SerializedName("source") val source: Source,
    @SerializedName("advancement") val advancement: List<AdvancementItem>
)

// Общие модели для advancement
data class AdvancementItem(
    @SerializedName("type") val type: String,
    @SerializedName("level") val level: Int,
    @SerializedName("title") val title: String,
    @SerializedName("configuration") val configuration: AdvancementConfiguration?
)

data class AdvancementConfiguration(
    @SerializedName("items") val items: List<ItemReference>?,
    @SerializedName("fixed") val fixed: Map<String, Int>?
)

data class ItemReference(
    @SerializedName("uuid") val uuid: String,
    @SerializedName("optional") val optional: Boolean
)

// Models for 2014 edition parsing

data class DndSources(val sources: List<DndSource>)

data class DndSource(val title: String, val races: List<Race2014>, val classes: List<DndClass2014>)

data class Race2014(
    val nameEn: String,
    val nameRu: String,
    val subraces: List<Subrace2014>
)

data class Subrace2014(
    val nameEn: String,
    val nameRu: String
)

data class DndClass2014(
    val nameEn: String,
    val nameRu: String,
    val subclasses: List<Subclass2014>
)

data class Subclass2014(
    val nameEn: String,
    val nameRu: String
)
