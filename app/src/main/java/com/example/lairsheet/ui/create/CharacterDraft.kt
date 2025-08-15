package com.example.lairsheet.ui.create

import com.example.lairsheet.data.dnd.DndClass
import com.example.lairsheet.data.dnd.Race
import com.example.lairsheet.data.dnd.Subclass
import com.example.lairsheet.data.dnd.Subrace

/**
 * Черновик персонажа для шага 1 (имя/раса/класс).
 */
data class CharacterDraft(
    val playerName: String = "",
    val race: Race? = null,
    val subrace: Subrace? = null,
    val clazz: DndClass? = null,
    val subclass: Subclass? = null
)
