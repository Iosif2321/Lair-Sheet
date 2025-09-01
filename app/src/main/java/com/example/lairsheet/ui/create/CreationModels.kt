package com.example.lairsheet.ui.create

import com.example.lairsheet.data.Ruleset
import com.example.lairsheet.data.AbilityType
import com.example.lairsheet.data.AbilityScore
import com.example.lairsheet.data.dnd.DndClass
import com.example.lairsheet.data.Subclass

/**
 * Набор моделей и общего состояния мастера создания персонажа.
 */

data class Subrace(val nameRu: String, val nameEn: String, val description: String = "")
data class Race(
    val nameRu: String,
    val nameEn: String,
    val description: String = "",
    val subraces: List<Subrace> = emptyList()
)

data class CharacterCreationState(
    // Источники правил (выбран только один)
    val selectedSources: Set<Ruleset> = setOf(Ruleset.R5E_2024),

    // Имя и ID сохранённого персонажа (ID null до первого сохранения)
    val characterName: String = "",
    val characterId: Long? = null,

    // Выборы на шаге выбора расы/класса
    val selectedRace: Race? = null,
    val selectedSubrace: Subrace? = null,
    val selectedClass: DndClass? = null,
    val selectedSubclass: Subclass? = null,

    // Предыстория (экран между шагами 3 и 4)
    val selectedBackground: Background? = null,
    val backgroundBonuses: Map<AbilityType, Int> = emptyMap(),

    // Шаг начисления характеристик
    val abilityMethod: AbilityMethod? = null,
    // Сырые итоговые значения (до применения расовых/фоновых модификаторов в дальнейших шагах)
    val abilityScores: Map<AbilityType, Int> = emptyMap(),
    val abilityStepCompleted: Boolean = false,

    // Состояние под конкретные методы (для восстановления/автосохранения)
    val pointBuyRemaining: Int = 27,
    val pointBuyValues: Map<AbilityType, Int> = defaultBase(),
    val standardArrayPool: List<Int> = listOf(15, 14, 13, 12, 10, 8),
    // Для каждого набора бросков храним 4 исходных куба (или только суммарные значения) — здесь список сумм
    val rollResults: List<Int> = emptyList(),
    val manualValues: Map<AbilityType, Int> = defaultBase(),

    // Собранное снаряжение (этап Equip)
    val selectedEquipment: List<String> = emptyList(),

    // Итоговые агрегаты по снаряжению (для сохранения/показа)
    val equipmentArmorClass: Int = 10,
    val equipmentCarryWeight: Double = 0.0,
    val equipmentGoldValue: Double = 0.0
)

/* ====== Этап 2: Распределение характеристик — тип метода ====== */

/**
 * ВНИМАНИЕ: значение CUSTOM используется как ручной ввод (manual).
 * routeKey служит для маршрутов шага 3:
 * - standard_array
 * - point_buy
 * - roll
 * - manual
 */
enum class AbilityMethod(val routeKey: String) {
    STANDARD_ARRAY("standard_array"),
    POINT_BUY("point_buy"),
    ROLL("roll"),
    CUSTOM("manual")
}

data class PointBuyConfig(
    val min: Int = 8,
    val max: Int = 15,
    val budget: Int = 27,
    // Стандартная стоимость по 5e: 8:0, 9:1, 10:2, 11:3, 12:4, 13:5, 14:7, 15:9
    val costs: Map<Int, Int> = mapOf(
        8 to 0, 9 to 1, 10 to 2, 11 to 3, 12 to 4, 13 to 5, 14 to 7, 15 to 9
    )
)

data class RollConfig(
    // Формула: NdXdly (напр. 4d6dl1 - 4d6 и отбросить низший 1)
    val formula: String = "4d6dl1",
    val rollsCount: Int = 6
)

enum class RacialBonusMode { FLEXIBLE_2024, FIXED_2014 }
enum class FlexibleScheme { PLUS2_PLUS1, PLUS1x3 }

// Предыстория (минимальная модель под схему бонусов 2024)
data class Background(
    val nameRu: String,
    val shortDescription: String = "",
    // Для 2024: схема распределения бонусов по разным хар-кам; null для 2014
    val scheme: List<Int>? = null,
    val feat: String? = null
)

data class RacialBonusChoice(
    val scheme: FlexibleScheme = FlexibleScheme.PLUS2_PLUS1,
    // Карта добавок от гибкой схемы
    val bonuses: Map<AbilityType, Int> = emptyMap()
)

/* ====== UI-состояние для экрана распределения (VM) ====== */

data class AttributesUiState(
    val method: AbilityMethod = AbilityMethod.STANDARD_ARRAY,
    // База до расовых бонусов
    val base: Map<AbilityType, Int> = defaultBase(),
    // Пул значений для STANDARD_ARRAY/ROLL (для Standard Array стартовый: 15, 14, 13, 12, 10, 8)
    val pool: List<Int> = listOf(15, 14, 13, 12, 10, 8),
    // PointBuy
    val pointBuy: PointBuyConfig = PointBuyConfig(),
    val remainingBudget: Int = pointBuy.budget,
    // Roll
    val roll: RollConfig = RollConfig(),
    // Детали бросков по характеристикам: 4 значения d6 на строку
    val rollDetails: Map<AbilityType, List<Int>> = emptyMap(),
    // Расовые бонусы
    val racialMode: RacialBonusMode = RacialBonusMode.FLEXIBLE_2024,
    val flexible: RacialBonusChoice = RacialBonusChoice(),
    // Фиксированные бонусы (2014) для автоматического применения
    val fixedBonuses: Map<AbilityType, Int> = emptyMap(),
    // Итоговые значения (base + racial)
    val total: Map<AbilityType, Int> = defaultBase(),
    // Готовность шага (все значения назначены, бюджет валиден и т.д.)
    val ready: Boolean = false
)

fun defaultBase(): Map<AbilityType, Int> = mapOf(
    AbilityType.STRENGTH to 8,
    AbilityType.DEXTERITY to 8,
    AbilityType.CONSTITUTION to 8,
    AbilityType.INTELLIGENCE to 8,
    AbilityType.WISDOM to 8,
    AbilityType.CHARISMA to 8
)
