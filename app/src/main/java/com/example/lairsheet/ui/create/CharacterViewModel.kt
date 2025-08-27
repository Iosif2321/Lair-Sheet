package com.example.lairsheet.ui.create

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader

class CharacterViewModel : ViewModel() {

    var state by mutableStateOf(CharacterState())
        private set

    private val availableSources = listOf(
        "Основные правила 2014",
        "Основные правила 2024",
        "Расширенные источники"
    )

    private var availableRaces: List<String> = emptyList()
    private var availableSubraces: List<String> = emptyList()
    private var availableClasses: List<String> = emptyList()
    private var availableSubclasses: List<String> = emptyList()
    private var availableBackgrounds: List<String> = emptyList()

    fun getSources(): List<String> = availableSources
    fun getRaces(): List<String> = availableRaces
    fun getSubraces(): List<String> = availableSubraces
    fun getClasses(): List<String> = availableClasses
    fun getSubclasses(): List<String> = availableSubclasses
    fun getBackgrounds(): List<String> = availableBackgrounds

    fun setName(name: String) {
        state = state.copy(name = name)
    }

    fun toggleSource(source: String) {
        val cur = state.selectedSources.toMutableList()
        if (cur.contains(source)) cur.remove(source) else cur.add(source)
        state = state.copy(selectedSources = cur)
    }

    private fun readJsonArrayFromAssets(context: Context, fileName: String): JSONArray? {
        return try {
            context.assets.open(fileName).use { input ->
                val text = BufferedReader(InputStreamReader(input)).readText()
                JSONArray(text)
            }
        } catch (_: Exception) {
            null
        }
    }

    fun loadRacesAndClasses(context: Context) {
        val races = linkedSetOf<String>()
        val classes = linkedSetOf<String>()

        if (state.selectedSources.contains("Основные правила 2014")) {
            readJsonArrayFromAssets(context, "races_phb2014.json")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    races.add(o.optString("name_ru", o.optString("name_en", "")))
                }
            }
            readJsonArrayFromAssets(context, "classes_phb2014.json")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    classes.add(o.optString("name_ru", o.optString("name_en", "")))
                }
            }
        }
        if (state.selectedSources.contains("Основные правила 2024")) {
            readJsonArrayFromAssets(context, "species_phb2024_index.json")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    races.add(o.optString("name_ru", o.optString("name_en", "")))
                }
            }
            readJsonArrayFromAssets(context, "classes_phb2024_index.json")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    classes.add(o.optString("name_ru", o.optString("name_en", "")))
                }
            }
        }

        availableRaces = races.toList().sorted()
        availableClasses = classes.toList().sorted()
        availableSubraces = emptyList()
        availableSubclasses = emptyList()
    }

    fun selectRace(context: Context, race: String) {
        state = state.copy(selectedRace = race, selectedSubrace = "")
        availableSubraces = loadSubracesFor(context, race)
    }

    private fun loadSubracesFor(context: Context, raceRuOrEn: String): List<String> {
        val result = linkedSetOf<String>()

        fun collect(from: JSONArray?) {
            from ?: return
            for (i in 0 until from.length()) {
                val o = from.getJSONObject(i)
                val name = o.optString("name_ru", o.optString("name_en", ""))
                if (name == raceRuOrEn) {
                    val subs = o.optJSONArray("subraces") ?: o.optJSONArray("subspecies")
                    if (subs != null) {
                        for (j in 0 until subs.length()) {
                            val so = subs.getJSONObject(j)
                            result.add(so.optString("name_ru", so.optString("name_en", "")))
                        }
                    }
                }
            }
        }

        if (state.selectedSources.contains("Основные правила 2014")) {
            collect(readJsonArrayFromAssets(context, "races_phb2014.json"))
        }
        if (state.selectedSources.contains("Основные правила 2024")) {
            collect(readJsonArrayFromAssets(context, "species_phb2024_index.json"))
        }
        return result.toList()
    }

    fun selectSubrace(subrace: String) {
        state = state.copy(selectedSubrace = subrace)
    }

    fun selectClass(context: Context, clazz: String) {
        state = state.copy(selectedClass = clazz, selectedSubclass = "")
        availableSubclasses = loadSubclassesFor(context, clazz)
    }

    private fun loadSubclassesFor(context: Context, classRuOrEn: String): List<String> {
        val result = linkedSetOf<String>()

        fun collect(from: JSONArray?) {
            from ?: return
            for (i in 0 until from.length()) {
                val o = from.getJSONObject(i)
                val name = o.optString("name_ru", o.optString("name_en", ""))
                if (name == classRuOrEn) {
                    val subs = o.optJSONArray("subclasses")
                    if (subs != null) {
                        for (j in 0 until subs.length()) {
                            val so = subs.getJSONObject(j)
                            result.add(so.optString("name_ru", so.optString("name_en", "")))
                        }
                    }
                }
            }
        }

        if (state.selectedSources.contains("Основные правила 2014")) {
            collect(readJsonArrayFromAssets(context, "classes_phb2014.json"))
        }
        if (state.selectedSources.contains("Основные правила 2024")) {
            collect(readJsonArrayFromAssets(context, "classes_phb2024_index.json"))
        }
        return result.toList()
    }

    /** ВАЖНО: этот метод требуется экрану выбора подкласса */
    fun selectSubclass(subclass: String) {
        state = state.copy(selectedSubclass = subclass)
    }

    fun setAttributeScores(str: Int, dex: Int, con: Int, int: Int, wis: Int, cha: Int) {
        state = state.copy(
            strength = str, dexterity = dex, constitution = con,
            intelligence = int, wisdom = wis, charisma = cha
        )
    }

    fun loadBackgrounds(context: Context) {
        val res = linkedSetOf<String>()
        readJsonArrayFromAssets(context, "backgrounds_phb2014.json")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                res.add(o.optString("name_ru", o.optString("name_en", "")))
            }
        }
        readJsonArrayFromAssets(context, "backgrounds_phb2024_index.json")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                res.add(o.optString("name_ru", o.optString("name_en", "")))
            }
        }
        availableBackgrounds = res.toList().sorted()
    }

    fun selectBackground(background: String) {
        state = state.copy(selectedBackground = background)
    }

    fun setEquipment(equipment: List<String>) {
        state = state.copy(selectedEquipment = equipment)
    }

    fun saveCharacter() { /* TODO: интеграция с БД при необходимости */ }
}
