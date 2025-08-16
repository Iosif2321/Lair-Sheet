package com.example.lairsheet.ui.create

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.lairsheet.data.Character
import com.example.lairsheet.data.Ruleset
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader

data class CreationState(
    val sources: List<String> = listOf(
        "Основные правила 2014",
        "Основные правила 2024",
        "Расширенные источники"
    ),
    val selectedSources: Set<String> = setOf("Основные правила 2014"),
    val races: List<String> = emptyList(),
    val classes: List<String> = emptyList(),
    val subclasses: List<String> = emptyList(),
    val backgrounds: List<String> = emptyList(),

    val name: String = "",
    val race: String = "",
    val clazz: String = "",
    val subclass: String = "",
    val level: Int = 1,
    val background: String = "",
    val alignment: String = "",

    val str: Int = 10, val dex: Int = 10, val con: Int = 10,
    val intel: Int = 10, val wis: Int = 10, val cha: Int = 10,
)

class CreationViewModel : ViewModel() {
    var ui by mutableStateOf(CreationState())
        private set

    fun toggleSource(src: String) {
        val cur = ui.selectedSources.toMutableSet()
        if (!cur.add(src)) cur.remove(src)
        ui = ui.copy(selectedSources = cur)
    }

    fun setName(value: String) { ui = ui.copy(name = value) }
    fun pickRace(value: String) { ui = ui.copy(race = value) }
    fun pickClass(value: String) { ui = ui.copy(clazz = value, subclass = "") }
    fun pickSubclass(value: String) { ui = ui.copy(subclass = value) }
    fun setLevel(value: Int) { ui = ui.copy(level = value.coerceIn(1, 20)) }
    fun setBackground(value: String) { ui = ui.copy(background = value) }
    fun setAlignment(value: String) { ui = ui.copy(alignment = value) }

    fun setStats(str: Int, dex: Int, con: Int, intel: Int, wis: Int, cha: Int) {
        ui = ui.copy(str = str, dex = dex, con = con, intel = intel, wis = wis, cha = cha)
    }

    private fun readArray(context: Context, asset: String): JSONArray? = try {
        context.assets.open(asset).use { input ->
            val text = BufferedReader(InputStreamReader(input)).readText()
            JSONArray(text)
        }
    } catch (_: Exception) { null }

    fun loadData(context: Context) {
        val racesSet = linkedSetOf<String>()
        val classesSet = linkedSetOf<String>()
        val backgroundsSet = linkedSetOf<String>()

        fun addNames(arr: JSONArray?, field: String = "name_ru"): List<String> {
            val out = mutableListOf<String>()
            arr ?: return out
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                out += o.optString(field, o.optString("name_en", ""))
            }
            return out
        }

        if ("Основные правила 2014" in ui.selectedSources) {
            addNames(readArray(context, "races_phb2014.json")).forEach { racesSet += it }
            addNames(readArray(context, "classes_phb2014.json")).forEach { classesSet += it }
            addNames(readArray(context, "backgrounds_phb2014.json")).forEach { backgroundsSet += it }
        }
        if ("Основные правила 2024" in ui.selectedSources) {
            addNames(readArray(context, "species_phb2024_index.json")).forEach { racesSet += it }
            addNames(readArray(context, "classes_phb2024_index.json")).forEach { classesSet += it }
            addNames(readArray(context, "backgrounds_phb2024_index.json")).forEach { backgroundsSet += it }
        }
        // "Расширенные источники" — при необходимости добавь аналогично

        ui = ui.copy(
            races = racesSet.toList(),
            classes = classesSet.toList(),
            backgrounds = backgroundsSet.toList()
        )
    }

    fun toCharacter(ruleset: Ruleset): Character = Character(
        name = ui.name,
        className = listOf(ui.clazz, ui.subclass).filter { it.isNotBlank() }.joinToString(" / "),
        race = ui.race,
        level = ui.level,
        background = ui.background,
        alignment = ui.alignment,
        strength = ui.str,
        dexterity = ui.dex,
        constitution = ui.con,
        intelligence = ui.intel,
        wisdom = ui.wis,
        charisma = ui.cha,
        ruleset = ruleset
    )
}
