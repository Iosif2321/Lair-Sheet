package com.example.lairsheet.data.dnd

import android.content.Context
import com.example.lairsheet.R
import com.example.lairsheet.data.Subclass
import com.example.lairsheet.ui.create.Race
import com.example.lairsheet.ui.create.Subrace
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class DndRepository2014(private val context: Context) {

    private val gson = Gson()

    // Cache
    private var cachedRaces: List<Race>? = null
    private var cachedClasses: List<DndClass>? = null
    private var cachedBrRaces: List<BrRace>? = null
    private var cachedBrClasses: List<BrClass>? = null
    private var cachedItems: List<Item>? = null
    private var cachedStartingEquipments: List<StartingEquipment>? = null
    private var cachedFeatures: BrFeaturesData? = null

    private fun loadJsonFromRaw(resourceId: Int): String =
        context.resources.openRawResource(resourceId).bufferedReader().use { it.readText() }

    private fun loadJson(resourceId: Int, importName: String? = null): String {
        if (importName != null) {
            val f = File(context.filesDir, "dnd_import/$importName")
            if (f.exists()) return f.readText()
        }
        return loadJsonFromRaw(resourceId)
    }

    private fun getBrRaces(): List<BrRace> {
        if (cachedBrRaces == null) {
            val json = loadJson(R.raw.br_2014_races, "br_2014_races.json")
            val type = object : TypeToken<List<BrRace>>() {}.type
            cachedBrRaces = gson.fromJson(json, type)
        }
        return cachedBrRaces ?: emptyList()
    }

    private fun getBrClasses(): List<BrClass> {
        if (cachedBrClasses == null) {
            val json = loadJson(R.raw.br_2014_classes, "br_2014_classes.json")
            val type = object : TypeToken<List<BrClass>>() {}.type
            cachedBrClasses = gson.fromJson(json, type)
        }
        return cachedBrClasses ?: emptyList()
    }

    private fun getItemsInternal(): List<Item> {
        if (cachedItems == null) {
            val json = loadJson(R.raw.br_2014_items, "br_2014_items.json")
            val type = object : TypeToken<List<Item>>() {}.type
            cachedItems = gson.fromJson(json, type)
        }
        return cachedItems ?: emptyList()
    }

    private fun getStartingEquipmentsInternal(): List<StartingEquipment> {
        if (cachedStartingEquipments == null) {
            val json = loadJson(R.raw.br_2014_starting_equipment, "br_2014_starting_equipment.json")
            val type = object : TypeToken<List<StartingEquipment>>() {}.type
            cachedStartingEquipments = gson.fromJson(json, type)
        }
        return cachedStartingEquipments ?: emptyList()
    }

    private fun getFeaturesData(): BrFeaturesData {
        if (cachedFeatures == null) {
            val json = loadJsonFromRaw(R.raw.br_2014_features)
            val type = object : TypeToken<BrFeaturesData>() {}.type
            cachedFeatures = gson.fromJson(json, type)
        }
        return cachedFeatures!!
    }

    // Public API

    fun getRaces(): List<Race> {
        if (cachedRaces == null) {
            cachedRaces = getBrRaces().map { brRace ->
                Race(
                    nameRu = brRace.name,
                    nameEn = brRace.key,
                    description = brRace.description,
                    subraces = brRace.subraces.map { brSubrace ->
                        Subrace(brSubrace.name, brSubrace.key, brSubrace.description)
                    }
                )
            }
        }
        return cachedRaces ?: emptyList()
    }

    fun getClasses(): List<DndClass> {
        if (cachedClasses == null) {
            cachedClasses = getBrClasses().map { brClass ->
                DndClass(
                    name = brClass.name,
                    type = brClass.key,
                    system = ClassSystem(
                        description = Description(brClass.description),
                        source = Source("BR-2014", "2014"),
                        advancement = emptyList(),
                        spellcasting = null
                    )
                )
            }
        }
        return cachedClasses ?: emptyList()
    }

    fun getSubraces(race: Race): List<Subrace> {
        return getBrRaces()
            .find { it.key == race.nameEn || it.name == race.nameRu }
            ?.subraces
            ?.map { brSubrace ->
                Subrace(brSubrace.name, brSubrace.key, brSubrace.description)
            } ?: emptyList()
    }

    fun getSubclasses(className: String): List<Subclass> {
        return getBrClasses()
            .find { it.name == className }
            ?.subclasses
            ?.map { brSubclass ->
                Subclass(brSubclass.name, brSubclass.key)
            } ?: emptyList()
    }

    // Effects

    fun getRaceEffects(race: Race, subrace: Subrace? = null): Effects? {
        val brRace = getBrRaces().find { it.key == race.nameEn || it.name == race.nameRu } ?: return null
        val raceEff = brRace.effects
        val subEff = subrace?.let { sr ->
            brRace.subraces.find { it.key == sr.nameEn || it.name == sr.nameRu }?.effects
        }
        return mergeEffects(raceEff, subEff)
    }

    fun getClassEffects(clazz: DndClass, subclass: Subclass? = null): Effects? {
        val brClass = getBrClasses().find { it.key == clazz.type || it.name == clazz.name } ?: return null
        val classEff = brClass.effects
        val subEff = subclass?.let { sc ->
            brClass.subclasses.find { it.key == sc.classIdentifier || it.name == sc.name }?.effects
        }
        return mergeEffects(classEff, subEff)
    }

    // Starting equipment / Items

    fun getStartingEquipment(classKey: String): StartingEquipment? =
        getStartingEquipmentsInternal().find { it.classKey == classKey }

    fun getStartingEquipment(clazz: DndClass): StartingEquipment? =
        getStartingEquipment(clazz.type)

    fun getItems(keys: Collection<String>): List<Item> {
        if (keys.isEmpty()) return emptyList()
        val all = getItemsInternal()
        val set = keys.toSet()
        return all.filter { it.key in set }
    }

    // ------- Class progression / spellcasting -------

    fun getClassAdvancement(classKey: String): List<BrAdvancementLevel> {
        val fd = getFeaturesData()
        return fd.classProgressions?.find { it.classKey == classKey }?.levels ?: emptyList()
    }

    fun getClassAdvancement(clazz: DndClass): List<BrAdvancementLevel> =
        getClassAdvancement(clazz.type)

    fun getSpellcastingProgression(classKey: String): Map<Int, BrSpellcastingProgression> {
        val map = getFeaturesData().spellcastingProgression[classKey] ?: return emptyMap()
        return map.mapKeys { (k, _) -> k.toIntOrNull() ?: -1 }.filterKeys { it >= 0 }
    }

    // ------- Merge helpers (копия из 2024) -------

    private fun mergeEffects(a: Effects?, b: Effects?): Effects? {
        if (a == null) return b
        if (b == null) return a
        return Effects(
            abilityBonuses = mergeAbilityBonuses(a.abilityBonuses, b.abilityBonuses),
            proficiencies = mergeProficiencies(a.proficiencies, b.proficiencies),
            senses = mergeSenses(a.senses, b.senses),
            speed = mergeSpeed(a.speed, b.speed),
            resistances = mergeList(a.resistances, b.resistances),
            vulnerabilities = mergeList(a.vulnerabilities, b.vulnerabilities),
            conditionsImmunity = mergeList(a.conditionsImmunity, b.conditionsImmunity),
            languages = mergeList(a.languages, b.languages),
            size = b.size ?: a.size,
            features = mergeList(a.features, b.features),
            notes = listOfNotNull(a.notes, b.notes).joinToString("\n").ifBlank { null },
            advantageOn = mergeAdvantage(a.advantageOn, b.advantageOn),
            disadvantageOn = mergeAdvantage(a.disadvantageOn, b.disadvantageOn)
        )
    }

    private fun mergeAbilityBonuses(a: Map<String, Int>?, b: Map<String, Int>?): Map<String, Int>? {
        if (a == null) return b
        if (b == null) return a
        val out = HashMap<String, Int>()
        (a.keys + b.keys).forEach { k ->
            val v = (a[k] ?: 0) + (b[k] ?: 0)
            if (v != 0) out[k] = v
        }
        return if (out.isEmpty()) null else out
    }

    private fun mergeProficiencies(a: EffProficiencies?, b: EffProficiencies?): EffProficiencies? {
        if (a == null) return b
        if (b == null) return a
        return EffProficiencies(
            weapons = mergeList(a.weapons, b.weapons),
            armor = mergeList(a.armor, b.armor),
            tools = mergeList(a.tools, b.tools),
            skills = mergeList(a.skills, b.skills),
            savingThrows = mergeList(a.savingThrows, b.savingThrows)
        )
    }

    private fun mergeSenses(a: EffSenses?, b: EffSenses?): EffSenses? {
        if (a == null) return b
        if (b == null) return a
        fun pickMax(x: Int?, y: Int?) = listOfNotNull(x, y).maxOrNull()
        return EffSenses(
            darkvision = pickMax(a.darkvision, b.darkvision),
            blindsight = pickMax(a.blindsight, b.blindsight),
            tremorsense = pickMax(a.tremorsense, b.tremorsense),
            truesight = pickMax(a.truesight, b.truesight)
        )
    }

    private fun mergeSpeed(a: EffSpeed?, b: EffSpeed?): EffSpeed? {
        if (a == null) return b
        if (b == null) return a
        fun pickMax(x: Int?, y: Int?) = listOfNotNull(x, y).maxOrNull()
        return EffSpeed(
            walk = pickMax(a.walk, b.walk),
            climb = pickMax(a.climb, b.climb),
            swim = pickMax(a.swim, b.swim),
            fly = pickMax(a.fly, b.fly)
        )
    }

    private fun mergeAdvantage(a: EffAdvantage?, b: EffAdvantage?): EffAdvantage? {
        if (a == null) return b
        if (b == null) return a
        return EffAdvantage(
            checks = mergeList(a.checks, b.checks),
            saves = mergeList(a.saves, b.saves)
        )
    }

    private fun <T> mergeList(a: List<T>?, b: List<T>?): List<T>? {
        return when {
            a == null -> b
            b == null -> a
            else -> (a + b).distinct()
        }
    }
}