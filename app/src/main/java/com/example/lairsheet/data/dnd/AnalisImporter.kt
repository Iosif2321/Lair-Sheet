package com.example.lairsheet.data.dnd

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.Normalizer
import java.util.Locale

/**
 * Импортёр локальных JSON из assets/analis для обоих правилетов (2014/2024).
 * Пишет оверрайды в filesDir/dnd_import/, которые подхватывают репозитории.
 *
 * Ожидаемые файлы в assets/analis:
 * - classes.json              (BR-2024 классы, богатый текст)
 * - character_origins.json    (BR-2024 "Origins": содержит разделы рас)
 * - classes_2014.json         (SRD‑2014 классы, компактно)
 * - races_2014.json           (SRD‑2014 расы c подрасами)
 */
object AnalisImporter {

    data class ImportReport(
        val savedFiles: List<String>,
        val notes: List<String> = emptyList()
    )

    suspend fun importFromAssetsAnalis(ctx: Context): ImportReport = withContext(Dispatchers.IO) {
        val dir = File(ctx.filesDir, "dnd_import").apply { mkdirs() }
        val saved = mutableListOf<String>()
        val notes = mutableListOf<String>()
        val gsonPretty: Gson = GsonBuilder().setPrettyPrinting().create()

        // 1) BR‑2024: классы (classes.json)
        runCatching {
            val raw = readAssetOrNull(ctx, "analis/classes.json")
            if (raw != null) {
                val list = parseClasses2024FromAnalis(raw)
                File(dir, "br_2024_classes.json").writeText(gsonPretty.toJson(list))
                saved += File(dir, "br_2024_classes.json").absolutePath
            } else {
                notes += "assets/analis/classes.json не найден"
            }
        }.onFailure { notes += "classes.json: ${it.message}" }

        // 2) BR‑2024: расы (character_origins.json)
        runCatching {
            val raw = readAssetOrNull(ctx, "analis/character_origins.json")
            if (raw != null) {
                val list = parseRaces2024FromAnalis(raw)
                File(dir, "br_2024_races.json").writeText(gsonPretty.toJson(list))
                saved += File(dir, "br_2024_races.json").absolutePath
            } else {
                notes += "assets/analis/character_origins.json не найден"
            }
        }.onFailure { notes += "character_origins.json: ${it.message}" }

        // 3) BR‑2014: классы (classes_2014.json)
        runCatching {
            val raw = readAssetOrNull(ctx, "analis/classes_2014.json")
            if (raw != null) {
                val list = parseClasses2014FromAnalis(raw)
                File(dir, "br_2014_classes.json").writeText(gsonPretty.toJson(list))
                saved += File(dir, "br_2014_classes.json").absolutePath
            } else {
                notes += "assets/analis/classes_2014.json не найден"
            }
        }.onFailure { notes += "classes_2014.json: ${it.message}" }

        // 4) BR‑2014: расы (races_2014.json)
        runCatching {
            val raw = readAssetOrNull(ctx, "analis/races_2014.json")
            if (raw != null) {
                val list = parseRaces2014FromAnalis(raw)
                File(dir, "br_2014_races.json").writeText(gsonPretty.toJson(list))
                saved += File(dir, "br_2014_races.json").absolutePath
            } else {
                notes += "assets/analis/races_2014.json не найден"
            }
        }.onFailure { notes += "races_2014.json: ${it.message}" }

        ImportReport(savedFiles = saved, notes = notes)
    }

    // ------------------------- PARSERS -------------------------

    private fun parseClasses2024FromAnalis(json: String): List<BrClass> {
        val root = JsonParser.parseString(json).asJsonObject
        val entities = root.getAsJsonObject("entities") ?: return emptyList()
        val result = mutableListOf<BrClass>()

        val enToRu = mapOf(
            "Barbarian" to "Варвар",
            "Bard" to "Бард",
            "Cleric" to "Жрец",
            "Druid" to "Друид",
            "Fighter" to "Воин",
            "Monk" to "Монах",
            "Paladin" to "Паладин",
            "Ranger" to "Рейнджер",
            "Rogue" to "Разбойник",
            "Sorcerer" to "Чародей",
            "Warlock" to "Колдун",
            "Wizard" to "Волшебник"
        )

        entities.entrySet().forEach { e ->
            val enName = e.key
            // Фильтруем только базовые 12 классов 2024 BR
            if (!enToRu.containsKey(enName)) return@forEach
            val o = e.value.asJsonObject
            val summary = o.get("summary")?.asString?.cleanText()?.take(280) ?: "Класс ${enToRu[enName]}"
            result += BrClass(
                key = slug(enName),
                name = enToRu[enName] ?: enName,
                description = summary,
                effects = null,
                subclasses = emptyList()
            )
        }
        return result
    }

    private fun parseRaces2024FromAnalis(json: String): List<BrRace> {
        val allowed = setOf("Dragonborn","Dwarf","Elf","Gnome","Goliath","Halfling","Human","Orc","Tiefling")
        val enToRu = mapOf(
            "Dragonborn" to "Драконорожденный",
            "Dwarf" to "Дварф",
            "Elf" to "Эльф",
            "Gnome" to "Гном",
            "Goliath" to "Голиаф",
            "Halfling" to "Халфлинг",
            "Human" to "Человек",
            "Orc" to "Орк",
            "Tiefling" to "Тифлинг"
        )
        val root = JsonParser.parseString(json).asJsonObject
        val entities = root.getAsJsonObject("entities") ?: return emptyList()
        val out = mutableListOf<BrRace>()

        entities.entrySet().forEach { e ->
            val en = e.key
            if (en !in allowed) return@forEach
            val obj = e.value.asJsonObject
            val sections = obj.getAsJsonObject("sections")
            val traitsKey = "${en} Traits"
            val traits = sections?.entrySet()?.firstOrNull { it.key.contains("Traits") }?.value?.asString
            val summary = obj.get("summary")?.asString?.cleanText()?.take(240)
            val desc = summary ?: traits?.cleanText()?.take(240) ?: "Раса ${enToRu[en] ?: en}"
            out += BrRace(
                key = slug(en),
                name = enToRu[en] ?: en,
                description = desc,
                effects = null,
                subraces = emptyList()
            )
        }
        return out
    }

    private fun parseClasses2014FromAnalis(json: String): List<BrClass> {
        val root = JsonParser.parseString(json).asJsonObject
        val entities = root.getAsJsonObject("entities") ?: return emptyList()
        val out = mutableListOf<BrClass>()
        entities.entrySet().forEach { e ->
            val en = e.key
            val obj = e.value.asJsonObject
            val summary = obj.get("summary")?.asString?.cleanText()?.take(200) ?: en
            val ru = srdenToRuClass(en)
            out += BrClass(
                key = slug(en),
                name = ru,
                description = summary,
                effects = null,
                subclasses = emptyList()
            )
        }
        return out
    }

    private fun parseRaces2014FromAnalis(json: String): List<BrRace> {
        val root = JsonParser.parseString(json).asJsonObject
        val entities = root.getAsJsonObject("entities") ?: return emptyList()
        val out = mutableListOf<BrRace>()
        entities.entrySet().forEach { e ->
            val en = e.key
            val o = e.value.asJsonObject
            val summary = o.get("summary")?.asString?.cleanText()?.take(220) ?: en
            val ruName = srdenToRuRace(en)
            val subraces = mutableListOf<BrSubrace>()
            if (o.has("subraces") && o.get("subraces").isJsonObject) {
                val subs = o.getAsJsonObject("subraces")
                subs.entrySet().forEach { se ->
                    val subEn = se.key
                    val subRu = srdEnToRuSubrace(en, subEn)
                    val desc = se.value.asJsonObject.entrySet().firstOrNull()?.value?.asString?.cleanText()?.take(180) ?: subRu
                    subraces += BrSubrace(
                        key = slug("$en-$subEn"),
                        name = subRu,
                        description = desc,
                        effects = null
                    )
                }
            }
            out += BrRace(
                key = slug(en),
                name = ruName,
                description = summary,
                effects = null,
                subraces = subraces
            )
        }
        return out
    }

    // ------------------------- HELPERS -------------------------

    private fun readAssetOrNull(ctx: Context, path: String): String? = try {
        ctx.assets.open(path).bufferedReader().use { it.readText() }
    } catch (_: Exception) { null }

    private fun String.cleanText(): String =
        this.replace(Regex("\\s+"), " ")
            .replace("©", "")
            .trim()

    private fun slug(s: String): String {
        val n = Normalizer.normalize(s, Normalizer.Form.NFD)
        val ascii = n.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        return ascii.lowercase(Locale.US)
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
    }

    private fun srdenToRuClass(en: String): String = when (en) {
        "Barbarian" -> "Варвар"
        "Bard" -> "Бард"
        "Cleric" -> "Жрец"
        "Druid" -> "Друид"
        "Fighter" -> "Воин"
        "Monk" -> "Монах"
        "Paladin" -> "Паладин"
        "Ranger" -> "Рейнджер"
        "Rogue" -> "Разбойник"
        "Sorcerer" -> "Чародей"
        "Warlock" -> "Колдун"
        "Wizard" -> "Волшебник"
        else -> en
    }

    private fun srdenToRuRace(en: String): String = when (en) {
        "Dwarf" -> "Дварф"
        "Elf" -> "Эльф"
        "Halfling" -> "Халфлинг"
        "Human" -> "Человек"
        else -> en
    }

    private fun srdEnToRuSubrace(parentEn: String, subEn: String): String = when (parentEn to subEn) {
        "Dwarf" to "Hill Dwarf" -> "Холмовой дварф"
        "Dwarf" to "Mountain Dwarf" -> "Горный дварф"
        "Elf" to "High Elf" -> "Высший эльф"
        "Elf" to "Wood Elf" -> "Лесной эльф"
        "Elf" to "Dark Elf (Drow)" -> "Дроу"
        "Halfling" to "Lightfoot" -> "Лёгконогий халфлинг"
        "Halfling" to "Stout" -> "Крепыш"
        else -> subEn
    }
}