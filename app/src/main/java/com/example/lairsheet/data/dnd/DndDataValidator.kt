package com.example.lairsheet.data.dnd

import android.content.Context
import android.util.Log
import com.example.lairsheet.R
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken

/**
 * Валидация целостности данных для BR-2024 и BR-2014.
 * Проверяется:
 * - ссылки на предметы в стартовом снаряжении (все ключи должны существовать в каталоге items)
 * - корректность ключей способностей в abilityBonuses
 * - корректность ключей в advantageOn/disadvantageOn (checks/saves по словарям)
 * - (BR-2024) ссылки на features (если используются)
 */
object DndDataValidator {

    private const val TAG = "DndDataValidator"
    private val gson = Gson()

    data class ValidationReport(
        val source: String,
        val missingItems: List<String>,
        val badAbilityKeys: List<String>,
        val badAdvantageChecks: List<String>,
        val badAdvantageSaves: List<String>,
        val missingFeatures: List<String>
    ) {
        val ok: Boolean get() =
            missingItems.isEmpty() &&
            badAbilityKeys.isEmpty() &&
            badAdvantageChecks.isEmpty() &&
            badAdvantageSaves.isEmpty() &&
            missingFeatures.isEmpty()
    }

    fun validateAll(context: Context): List<ValidationReport> {
        val r2024 = validate2024(context)
        val r2014 = validate2014(context)
        val reports = listOf(r2024, r2014)
        reports.forEach { dump(it) }
        return reports
    }

    fun validate2024(context: Context): ValidationReport {
        val repo = DndRepository2024(context)
        val items = repoGetItemsMap(context, is2024 = true)
        val se = repoGetStartingEquipment(context, is2024 = true)
        val races = repoGetBrRaces(context, is2024 = true)
        val classes = repoGetBrClasses(context, is2024 = true)

        val dict = readDictionaries(context, is2024 = true)
        val abilityKeys = setOf("strength","dexterity","constitution","intelligence","wisdom","charisma")
        val knownChecks = dict.getAsJsonArrayOrEmpty("skills").map { it.asString }.toSet()
        val knownSaves = dict.getAsJsonArrayOrEmpty("proficiencyBonuses") // заглушка, лучше брать из features.json, используем дефолт
        val saveKeys = setOf("strength","dexterity","constitution","intelligence","wisdom","charisma")

        val missingItems = mutableSetOf<String>()
        val badAbility = mutableSetOf<String>()
        val badAdvChecks = mutableSetOf<String>()
        val badAdvSaves = mutableSetOf<String>()
        val missingFeatures = mutableSetOf<String>()

        // Проверка StartingEquipment
        se.forEach { s ->
            s.fixed.forEach { fi -> if (fi.key !in items) missingItems += fi.key }
            s.choices.forEach { ch ->
                ch.options.forEach { opt ->
                    opt.key?.let { if (it !in items) missingItems += it }
                    opt.bundle?.forEach { b -> if (b.key !in items) missingItems += b.key }
                }
            }
        }

        // Проверка эффектов рас/подрас и классов/подклассов
        fun checkEffects(e: Effects?) {
            e ?: return
            e.abilityBonuses?.keys?.forEach { k -> if (k !in abilityKeys) badAbility += k }
            e.advantageOn?.checks?.forEach { k -> if (k !in knownChecks) badAdvChecks += k }
            e.disadvantageOn?.checks?.forEach { k -> if (k !in knownChecks) badAdvChecks += k }
            e.advantageOn?.saves?.forEach { k -> if (k !in saveKeys) badAdvSaves += k }
            e.disadvantageOn?.saves?.forEach { k -> if (k !in saveKeys) badAdvSaves += k }
        }

        races.forEach { r ->
            checkEffects(r.effects)
            r.subraces.forEach { sr -> checkEffects(sr.effects) }
        }
        classes.forEach { c ->
            checkEffects(c.effects)
            c.subclasses.forEach { sc -> checkEffects(sc.effects) }
        }

        // Проверка наличия features (если задействованы)
        val featKeys = readFeaturesKeys2024(context)
        if (featKeys.isNotEmpty()) {
            fun checkFeatures(e: Effects?) {
                e?.features?.forEach { k -> if (k !in featKeys) missingFeatures += k }
            }
            races.forEach { r ->
                checkFeatures(r.effects)
                r.subraces.forEach { sr -> checkFeatures(sr.effects) }
            }
            classes.forEach { c ->
                checkFeatures(c.effects)
                c.subclasses.forEach { sc -> checkFeatures(sc.effects) }
            }
        }

        return ValidationReport(
            source = "BR-2024",
            missingItems = missingItems.toList().sorted(),
            badAbilityKeys = badAbility.toList().sorted(),
            badAdvantageChecks = badAdvChecks.toList().sorted(),
            badAdvantageSaves = badAdvSaves.toList().sorted(),
            missingFeatures = missingFeatures.toList().sorted()
        )
    }

    fun validate2014(context: Context): ValidationReport {
        val repo = DndRepository2014(context)
        val items = repoGetItemsMap(context, is2024 = false)
        val se = repoGetStartingEquipment(context, is2024 = false)
        val races = repoGetBrRaces(context, is2024 = false)
        val classes = repoGetBrClasses(context, is2024 = false)

        val dict = readDictionaries(context, is2024 = false)
        val abilityKeys = setOf("strength","dexterity","constitution","intelligence","wisdom","charisma")
        val knownChecks = dict.getAsJsonArrayOrEmpty("skills").map { it.asString }.toSet()
        val saveKeys = setOf("strength","dexterity","constitution","intelligence","wisdom","charisma")

        val missingItems = mutableSetOf<String>()
        val badAbility = mutableSetOf<String>()
        val badAdvChecks = mutableSetOf<String>()
        val badAdvSaves = mutableSetOf<String>()
        val missingFeatures = mutableSetOf<String>()

        se.forEach { s ->
            s.fixed.forEach { fi -> if (fi.key !in items) missingItems += fi.key }
            s.choices.forEach { ch ->
                ch.options.forEach { opt ->
                    opt.key?.let { if (it !in items) missingItems += it }
                    opt.bundle?.forEach { b -> if (b.key !in items) missingItems += b.key }
                }
            }
        }

        fun checkEffects(e: Effects?) {
            e ?: return
            e.abilityBonuses?.keys?.forEach { k -> if (k !in abilityKeys) badAbility += k }
            e.advantageOn?.checks?.forEach { k -> if (k !in knownChecks) badAdvChecks += k }
            e.disadvantageOn?.checks?.forEach { k -> if (k !in knownChecks) badAdvChecks += k }
            e.advantageOn?.saves?.forEach { k -> if (k !in saveKeys) badAdvSaves += k }
            e.disadvantageOn?.saves?.forEach { k -> if (k !in saveKeys) badAdvSaves += k }
        }

        races.forEach { r ->
            checkEffects(r.effects)
            r.subraces.forEach { sr -> checkEffects(sr.effects) }
        }
        classes.forEach { c ->
            checkEffects(c.effects)
            c.subclasses.forEach { sc -> checkEffects(sc.effects) }
        }

        return ValidationReport(
            source = "BR-2014",
            missingItems = missingItems.toList().sorted(),
            badAbilityKeys = badAbility.toList().sorted(),
            badAdvantageChecks = badAdvChecks.toList().sorted(),
            badAdvantageSaves = badAdvSaves.toList().sorted(),
            missingFeatures = missingFeatures.toList().sorted()
        )
    }

    private fun dump(r: ValidationReport) {
        if (r.ok) {
            Log.i(TAG, "[${r.source}] ОК")
            return
        }
        Log.w(TAG, "[${r.source}] Проблемы обнаружены")
        if (r.missingItems.isNotEmpty()) Log.w(TAG, "Отсутствующие items: ${r.missingItems}")
        if (r.badAbilityKeys.isNotEmpty()) Log.w(TAG, "Некорректные ability ключи: ${r.badAbilityKeys}")
        if (r.badAdvantageChecks.isNotEmpty()) Log.w(TAG, "Некорректные checks в advantage/disadvantage: ${r.badAdvantageChecks}")
        if (r.badAdvantageSaves.isNotEmpty()) Log.w(TAG, "Некорректные saves в advantage/disadvantage: ${r.badAdvantageSaves}")
        if (r.missingFeatures.isNotEmpty()) Log.w(TAG, "Отсутствующие features: ${r.missingFeatures}")
    }

    // -------- Low-level utils --------

    private fun repoGetItemsMap(context: Context, is2024: Boolean): Map<String, Item> {
        val json = context.resources.openRawResource(if (is2024) R.raw.br_2024_items else R.raw.br_2014_items)
            .bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<Item>>() {}.type
        val list: List<Item> = gson.fromJson(json, type)
        return list.associateBy { it.key }
    }

    private fun repoGetStartingEquipment(context: Context, is2024: Boolean): List<StartingEquipment> {
        val json = context.resources.openRawResource(if (is2024) R.raw.br_2024_starting_equipment else R.raw.br_2014_starting_equipment)
            .bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<StartingEquipment>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun repoGetBrRaces(context: Context, is2024: Boolean): List<BrRace> {
        val json = context.resources.openRawResource(if (is2024) R.raw.br_2024_races else R.raw.br_2014_races)
            .bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<BrRace>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun repoGetBrClasses(context: Context, is2024: Boolean): List<BrClass> {
        val json = context.resources.openRawResource(if (is2024) R.raw.br_2024_classes else R.raw.br_2014_classes)
            .bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<BrClass>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun readDictionaries(context: Context, is2024: Boolean): JsonObject {
        val resId = if (is2024) R.raw.br_2024_dictionaries else R.raw.br_2014_dictionaries
        val json = context.resources.openRawResource(resId).bufferedReader().use { it.readText() }
        return gson.fromJson(json, JsonObject::class.java)
    }

    private fun readFeaturesKeys2024(context: Context): Set<String> {
        return try {
            val json = context.resources.openRawResource(R.raw.br_2024_features).bufferedReader().use { it.readText() }
            val obj = gson.fromJson(json, JsonObject::class.java)
            obj.getAsJsonArray("features")?.map { it.asJsonObject.get("key").asString }?.toSet() ?: emptySet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    // JsonObject helpers
    private fun JsonObject.getAsJsonArrayOrEmpty(name: String) =
        (this.getAsJsonArray(name) ?: com.google.gson.JsonArray())
}