package com.example.lairsheet.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.serializer
import java.io.BufferedReader

// ---------- МОДЕЛИ ПРАВИЛ ----------
@Serializable
data class Race(
    val id: String? = null,
    val name: String,
    @SerialName("ability_bonuses") val abilityBonuses: Map<String, Int> = emptyMap(),
    val size: String = "Medium",
    val speed: Int = 30,
    val traits: List<String> = emptyList(),
    val source: String? = null
)

@Serializable
data class Subrace(
    val id: String? = null,
    val name: String,
    val parent: String,            // имя базовой расы
    val source: String? = null
)

@Serializable
data class ClassDef(
    val id: String? = null,
    val name: String,
    @SerialName("hit_die") val hitDie: Int = 8,
    @SerialName("primary_abilities") val primaryAbilities: List<String> = emptyList(),
    val saves: List<String> = emptyList(),
    val proficiencies: List<String> = emptyList(),
    val subclasses: List<String> = emptyList(), // имена подклассов (заполняем из Subclass.parent)
    val source: String? = null
)

@Serializable
data class Subclass(
    val id: String? = null,
    val name: String,
    val parent: String,            // имя базового класса
    val source: String? = null
)

@Serializable
data class Background(
    val id: String? = null,
    val name: String,
    val proficiencies: List<String> = emptyList(),
    val languages: List<String> = emptyList(),
    val equipment: List<String> = emptyList(),
    val source: String? = null
)

@Serializable
data class Feat(
    val id: String? = null,
    val name: String,
    val prerequisites: List<String> = emptyList(),
    val source: String? = null
)

@Serializable
data class Spell(
    val id: String? = null,
    val name: String,
    val level: Int,
    val school: String,
    val classes: List<String> = emptyList(),
    val castingTime: String? = null,
    val range: String? = null,
    val duration: String? = null,
    val concentration: Boolean = false,
    val ritual: Boolean = false,
    val source: String? = null
)

@Serializable
data class EquipmentItem(
    val id: String? = null,
    val name: String,
    val category: String,
    val cost: String? = null,
    val weight: String? = null,
    val properties: List<String> = emptyList(),
    val source: String? = null
)

// ---------- КОНТРАКТ ----------
interface RulesRepository {
    suspend fun getRaces(version: Ruleset): List<Race>
    suspend fun getSubraces(version: Ruleset): List<Subrace>
    suspend fun getClasses(version: Ruleset): List<ClassDef>
    suspend fun getSubclasses(version: Ruleset): List<Subclass>
    suspend fun getBackgrounds(version: Ruleset): List<Background>
    suspend fun getFeats(version: Ruleset): List<Feat>
    suspend fun getSpells(version: Ruleset): List<Spell>
    suspend fun getEquipment(version: Ruleset): List<EquipmentItem>
}

// ---------- РЕПОЗИТОРИЙ: ЧИТАЕМ ВСЕ ФАЙЛЫ В КАТЕГОРИЯХ И СЛИВАЕМ ----------
class AssetsRulesRepository(
    private val context: Context
) : RulesRepository {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val cache = mutableMapOf<String, Any>()

    override suspend fun getRaces(version: Ruleset): List<Race> =
        readFolders(version, listOf("races", "racesmpmm"), listOf("races", "race"))

    override suspend fun getSubraces(version: Ruleset): List<Subrace> =
        // если отдельной папки нет — вернём пустой список
        readFolders(version, listOf("subraces"), listOf("subraces", "subrace"), missingOk = true)

    override suspend fun getClasses(version: Ruleset): List<ClassDef> {
        val classes: List<ClassDef> =
            readFolders(version, listOf("classes"), listOf("classes", "class"))
        val subclasses: List<Subclass> = getSubclasses(version)

        if (subclasses.isEmpty()) return classes

        // вшиваем имена подклассов внутрь классов
        val byParent = subclasses.groupBy { it.parent }
        return classes.map { c ->
            val subs = byParent[c.name]?.map { it.name } ?: emptyList()
            if (subs.isEmpty()) c else c.copy(subclasses = subs.sorted())
        }
    }

    override suspend fun getSubclasses(version: Ruleset): List<Subclass> =
        readFolders(version, listOf("subclasses"), listOf("subclasses", "subclass"), missingOk = true)

    override suspend fun getBackgrounds(version: Ruleset): List<Background> =
        readFolders(version, listOf("backgrounds"), listOf("backgrounds", "background"))

    override suspend fun getFeats(version: Ruleset): List<Feat> =
        readFolders(version, listOf("feats"), listOf("feats", "feat"), missingOk = true)

    override suspend fun getSpells(version: Ruleset): List<Spell> =
        readFolders(version, listOf("spells"), listOf("spells", "spell"), missingOk = true)

    override suspend fun getEquipment(version: Ruleset): List<EquipmentItem> =
        readFolders(version, listOf("equipment", "items"), listOf("equipment", "items", "item"), missingOk = true)

    // ---------- helpers ----------
    private fun baseDir(version: Ruleset): String =
        when (version) {
            Ruleset.R5E_2014 -> "dnd_data/5e_2014"
            Ruleset.R5E_2024 -> "dnd_data/5e_2024"
        }

    private suspend inline fun <reified T> readFolders(
        version: Ruleset,
        folders: List<String>,
        possibleKeys: List<String>,
        missingOk: Boolean = false
    ): List<T> = withContext(Dispatchers.IO) {
        val listDeserializer = ListSerializer(json.serializersModule.serializer<T>())
        val out = mutableListOf<T>()

        for (folder in folders) {
            val dir = "${baseDir(version)}/$folder"
            val cacheKey = "dir:$dir:${T::class.qualifiedName}"
            @Suppress("UNCHECKED_CAST")
            val cached = cache[cacheKey] as? List<T>
            if (cached != null) {
                out += cached
                continue
            }

            val names = context.assets.list(dir)?.toList()
            if (names.isNullOrEmpty()) {
                if (!missingOk) {
                    // просто пропускаем, если папка отсутствует
                }
                cache[cacheKey] = emptyList<T>()
                continue
            }

            val acc = mutableListOf<T>()
            for (name in names) {
                val path = "$dir/$name"
                val raw = try {
                    context.assets.open(path).bufferedReader().use(BufferedReader::readText)
                } catch (_: Throwable) {
                    null
                } ?: continue

                try {
                    val elt = json.parseToJsonElement(raw)
                    when (elt) {
                        is JsonArray -> {
                            acc += json.decodeFromJsonElement(listDeserializer, elt)
                        }
                        is JsonObject -> {
                            // ищем массив по известным ключам
                            val key = possibleKeys.firstOrNull { k -> elt[k] is JsonArray }
                            if (key != null) {
                                acc += json.decodeFromJsonElement(listDeserializer, elt[key]!!.jsonArray)
                            } else if (elt["compendium"] is JsonObject) {
                                val comp = elt["compendium"]!!.jsonObject
                                val ckey = possibleKeys.firstOrNull { k -> comp[k] is JsonArray }
                                if (ckey != null) {
                                    acc += json.decodeFromJsonElement(listDeserializer, comp[ckey]!!.jsonArray)
                                }
                            }
                        }
                        is JsonPrimitive -> {
                            // примитивный JSON на корне нам не интересен — пропускаем
                        }
                        else -> {
                            // защита на будущее, чтобы when был исчерпывающим
                        }
                    }
                } catch (_: Throwable) {
                    // проглатываем проблемный файл и идём дальше
                }
            }

            // дедуп по id, иначе — по name|source
            val dedup = LinkedHashMap<String, T>()
            for (item in acc) {
                val key = when (item) {
                    is Race          -> (item.id?.ifBlank { null } ?: "${item.name}|${item.source ?: ""}")
                    is Subrace       -> (item.id?.ifBlank { null } ?: "${item.name}|${item.parent}|${item.source ?: ""}")
                    is ClassDef      -> (item.id?.ifBlank { null } ?: "${item.name}|${item.source ?: ""}")
                    is Subclass      -> (item.id?.ifBlank { null } ?: "${item.name}|${item.parent}|${item.source ?: ""}")
                    is Background    -> (item.id?.ifBlank { null } ?: "${item.name}|${item.source ?: ""}")
                    is Feat          -> (item.id?.ifBlank { null } ?: "${item.name}|${item.source ?: ""}")
                    is Spell         -> (item.id?.ifBlank { null } ?: "${item.name}|${item.level}|${item.source ?: ""}")
                    is EquipmentItem -> (item.id?.ifBlank { null } ?: "${item.name}|${item.source ?: ""}")
                    else             -> item.hashCode().toString()
                }
                dedup.putIfAbsent(key, item)
            }
            val result = dedup.values.toList()
            cache[cacheKey] = result
            out += result
        }
        out
    }
}
