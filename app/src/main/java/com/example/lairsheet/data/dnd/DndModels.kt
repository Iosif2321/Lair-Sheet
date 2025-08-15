package com.example.lairsheet.data.dnd

data class Subrace(
    val nameEn: String,
    val nameRu: String
)

data class Race(
    val nameEn: String,
    val nameRu: String,
    val subraces: List<Subrace> = emptyList()
)

data class Subclass(
    val nameEn: String,
    val nameRu: String
)

data class DndClass(
    val nameEn: String,
    val nameRu: String,
    val subclasses: List<Subclass> = emptyList()
)

data class DndSource(
    val title: String,
    val races: List<Race>,
    val classes: List<DndClass>
)

data class DndSources(
    val sources: List<DndSource>
)
