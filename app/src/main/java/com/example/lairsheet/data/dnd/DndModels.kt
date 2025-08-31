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

// BR-2024 Models

data class BrFeature(
    @SerializedName("key") val key: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String
)

data class BrProficiencyChoice(
    @SerializedName("type") val type: String,
    @SerializedName("options") val options: List<String>
)

data class BrEquipmentChoice(
    @SerializedName("key") val key: String,
    @SerializedName("name") val name: String,
    @SerializedName("choices") val choices: List<BrProficiencyChoice>
)

data class BrAdvancementLevel(
    @SerializedName("level") val level: Int,
    @SerializedName("features") val features: List<String>,
    @SerializedName("proficiencyBonus") val proficiencyBonus: Int
)

data class BrSpellcastingProgression(
    @SerializedName("cantrips") val cantrips: Int,
    @SerializedName("spells") val spells: Int,
    @SerializedName("level1") val level1: Int? = null,
    @SerializedName("level2") val level2: Int? = null,
    @SerializedName("level3") val level3: Int? = null,
    @SerializedName("level4") val level4: Int? = null,
    @SerializedName("level5") val level5: Int? = null,
    @SerializedName("level6") val level6: Int? = null,
    @SerializedName("level7") val level7: Int? = null,
    @SerializedName("level8") val level8: Int? = null,
    @SerializedName("level9") val level9: Int? = null
)

data class BrSubrace(
    @SerializedName("key") val key: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("effects") val effects: Effects? = null
)

data class BrRace(
    @SerializedName("key") val key: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("effects") val effects: Effects? = null,
    @SerializedName("subraces") val subraces: List<BrSubrace>
)

data class BrSubclass(
    @SerializedName("key") val key: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("effects") val effects: Effects? = null
)

data class BrClass(
    @SerializedName("key") val key: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("effects") val effects: Effects? = null,
    @SerializedName("subclasses") val subclasses: List<BrSubclass>
)

data class BrProficiencies(
    @SerializedName("weapons") val weapons: List<String>,
    @SerializedName("armor") val armor: List<String>,
    @SerializedName("tools") val tools: List<String>,
    @SerializedName("savingThrows") val savingThrows: List<String>,
    @SerializedName("skills") val skills: List<String>
)

data class BrClassProgression(
    @SerializedName("classKey") val classKey: String,
    @SerializedName("levels") val levels: List<BrAdvancementLevel>
)

data class BrFeaturesData(
    @SerializedName("features") val features: List<BrFeature>,
    @SerializedName("proficiencies") val proficiencies: BrProficiencies,
    @SerializedName("equipmentChoices") val equipmentChoices: List<BrEquipmentChoice>,
    @SerializedName("advancementLevels") val advancementLevels: List<BrAdvancementLevel>,
    @SerializedName("spellcastingProgression") val spellcastingProgression: Map<String, Map<String, BrSpellcastingProgression>>,
    @SerializedName("classProgressions") val classProgressions: List<BrClassProgression>? = null
)


// ===== Effects / Items / Starting Equipment models =====

data class Effects(
    @SerializedName("abilityBonuses") val abilityBonuses: Map<String, Int>? = null,
    @SerializedName("proficiencies") val proficiencies: EffProficiencies? = null,
    @SerializedName("senses") val senses: EffSenses? = null,
    @SerializedName("speed") val speed: EffSpeed? = null,
    @SerializedName("resistances") val resistances: List<String>? = null,
    @SerializedName("vulnerabilities") val vulnerabilities: List<String>? = null,
    @SerializedName("conditionsImmunity") val conditionsImmunity: List<String>? = null,
    @SerializedName("languages") val languages: List<String>? = null,
    // size разрешён и для классов (по требованию)
    @SerializedName("size") val size: String? = null,
    @SerializedName("features") val features: List<String>? = null,
    @SerializedName("notes") val notes: String? = null,
    // advantage/disadvantage для checks/saves
    @SerializedName("advantageOn") val advantageOn: EffAdvantage? = null,
    @SerializedName("disadvantageOn") val disadvantageOn: EffAdvantage? = null
)

data class EffProficiencies(
    @SerializedName("weapons") val weapons: List<String>? = null,
    @SerializedName("armor") val armor: List<String>? = null,
    @SerializedName("tools") val tools: List<String>? = null,
    @SerializedName("skills") val skills: List<String>? = null,
    @SerializedName("savingThrows") val savingThrows: List<String>? = null
)

data class EffSenses(
    @SerializedName("darkvision") val darkvision: Int? = null,
    @SerializedName("blindsight") val blindsight: Int? = null,
    @SerializedName("tremorsense") val tremorsense: Int? = null,
    @SerializedName("truesight") val truesight: Int? = null
)

data class EffSpeed(
    @SerializedName("walk") val walk: Int? = null,
    @SerializedName("climb") val climb: Int? = null,
    @SerializedName("swim") val swim: Int? = null,
    @SerializedName("fly") val fly: Int? = null
)

data class EffAdvantage(
    @SerializedName("checks") val checks: List<String>? = null,
    @SerializedName("saves") val saves: List<String>? = null
)

// ===== Items =====

data class Money(
    @SerializedName("cp") val cp: Int? = null,
    @SerializedName("sp") val sp: Int? = null,
    @SerializedName("gp") val gp: Int? = null
)

data class Damage(
    @SerializedName("type") val type: String,
    @SerializedName("dice") val dice: String
)

data class Item(
    @SerializedName("key") val key: String,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("price") val price: Money? = null,
    @SerializedName("weightLb") val weightLb: Float? = null,
    @SerializedName("properties") val properties: List<String>? = null,
    @SerializedName("damage") val damage: Damage? = null,
    @SerializedName("armorClass") val armorClass: Int? = null,
    @SerializedName("strengthReq") val strengthReq: Int? = null,
    @SerializedName("stealthDisadvantage") val stealthDisadvantage: Boolean? = null,
    @SerializedName("source") val source: String? = null
)

// ===== Starting Equipment =====

data class EqItem(
    @SerializedName("key") val key: String,
    @SerializedName("qty") val qty: Int = 1
)

data class EqOption(
    // одиночный предмет
    @SerializedName("key") val key: String? = null,
    @SerializedName("qty") val qty: Int? = null,
    // или набор предметов
    @SerializedName("bundle") val bundle: List<EqItem>? = null
)

data class EqChoice(
    @SerializedName("id") val id: String,
    @SerializedName("pick") val pick: Int = 1,
    @SerializedName("options") val options: List<EqOption>
)

data class StartingEquipment(
    @SerializedName("classKey") val classKey: String,
    @SerializedName("fixed") val fixed: List<EqItem> = emptyList(),
    @SerializedName("choices") val choices: List<EqChoice> = emptyList()
)
