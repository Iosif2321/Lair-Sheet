// ===== Character Core =====

package dnd.lair.core

import kotlin.random.Random

// --- Базовые типы

enum class Ruleset { R2014, R2024 }
enum class Ability { STR, DEX, CON, INT, WIS, CHA }

// Итог по характеристике
data class AbilityResult(val score: Int, val modifier: Int)

// Полный срез слоёв (то, что удобно хранить в стейте билдера)
data class AbilityLayers(
    val base: Map<Ability, Int>,                 // из метода распределения
    val origin: Map<Ability, Int> = emptyMap(),  // раса/подраса (2014) или бэкграунд (2024)
    val featAsi: Map<Ability, Int> = emptyMap(), // фиты/ASI (+1/+2)
    val temporary: Map<Ability, Int> = emptyMap(),// временные баффы
    val setTo: Map<Ability, Int> = emptyMap(),    // предметы "установить в X"
    val naturalCap: Int = 20                      // кап для «натурального»
) {
    fun compute(): Map<Ability, AbilityResult> =
        Ability.values().associateWith { a ->
            val natUnclamped = (base[a] ?: 0) + (origin[a] ?: 0) + (featAsi[a] ?: 0) + (temporary[a] ?: 0)
            val natural = natUnclamped.coerceIn(1, naturalCap)
            val set = setTo[a] ?: Int.MIN_VALUE
            val finalScore = maxOf(natural, set)
            AbilityResult(finalScore, java.lang.Math.floorDiv(finalScore - 10, 2))
        }
}

// --- Методы набора БАЗЫ

sealed class BaseMethod {
    object StandardArray : BaseMethod()
    data class PointBuy(val budget: Int = 27, val min: Int = 8, val max: Int = 15) : BaseMethod()
    object Roll4d6DropLowest : BaseMethod()
    data class Custom(val values: Map<Ability, Int>) : BaseMethod() // UI уже дал готовые значения
}

object StandardArray {
    val values = listOf(15, 14, 13, 12, 10, 8)
    /**
     * assignment: Ability -> выбранное число из массива.
     * Валидируем, что это перестановка массива (все 6 значений использованы ровно по 1 разу).
     */
    fun assign(assignment: Map<Ability, Int>): Map<Ability, Int> {
        require(assignment.keys.containsAll(Ability.values().toSet())) { "Назначьте все 6 характеристик." }
        val picked = assignment.values.sortedDescending()
        require(picked == values.sortedDescending()) { "Должны быть использованы значения стандартного массива: $values." }
        return assignment
    }
}

object PointBuy {
    // RAW 5e (8..15)
    fun costFor(score: Int): Int = when (score) {
        8 -> 0
        9 -> 1
        10 -> 2
        11 -> 3
        12 -> 4
        13 -> 5
        14 -> 7
        15 -> 9
        else -> error("Недопустимое значение $score для Point Buy.")
    }

    /**
     * values: итоговые базовые значения (6 штук). Проверяем бюджет/диапазон.
     */
    fun validate(values: Map<Ability, Int>, budget: Int = 27, min: Int = 8, max: Int = 15): Map<Ability, Int> {
        require(values.size == 6) { "Нужно задать все 6 характеристик." }
        values.forEach { (a, v) -> require(v in min..max) { "$a=$v вне диапазона $min..$max" } }
        val spent = values.values.sumOf { costFor(it) }
        require(spent <= budget) { "Превышен бюджет Point Buy: потрачено $spent из $budget." }
        return values
    }
}

object Roller {
    fun roll4d6DropLowest(rnd: Random = Random): Int {
        val rolls = IntArray(4) { rnd.nextInt(1, 7) }.sortedDescending()
        return rolls.take(3).sum()
    }
    fun six(rnd: Random = Random): List<Int> = List(6) { roll4d6DropLowest(rnd) }
    /**
     * assignment: Ability -> число из списка rolls (перестановка).
     */
    fun assign(rolls: List<Int>, assignment: Map<Ability, Int>): Map<Ability, Int> {
        require(rolls.size == 6) { "Нужно 6 бросков." }
        require(assignment.size == 6) { "Назначьте все 6 характеристик." }
        require(assignment.values.sorted() == rolls.sorted()) { "Назначенные значения не совпадают с бросками: $rolls." }
        return assignment
    }
}

// --- Источник стартовых бонусов (Origin)

sealed class OriginMode {
    /** 2014 RAW: фиксированная карта Race -> [+2/+1/...] в конкретные статы */
    data class Raw2014(val raceBonuses: Map<Ability, Int>) : OriginMode()

    /**
     * 2014 (Tasha) и 2024: перечень величин, которые надо разложить по РАЗНЫМ статам.
     * Примеры: [2,1] / [1,1,1] / [2,2] / [2,1,1] (half-elf в 2014).
     */
    data class DistinctAssignment(val magnitudes: List<Int>) : OriginMode()
}

object OriginAllocator {
    /**
     * picks: Ability -> величина, которую игрок положил в стат (0 если ничего).
     * Для DistinctAssignment — каждая величина должна быть использована ровно 1 раз и все в РАЗНЫЕ статы.
     * Для Raw2014 — picks должен совпасть с raceBonuses.
     */
    fun assign(mode: OriginMode, picks: Map<Ability, Int>): Map<Ability, Int> = when (mode) {
        is OriginMode.Raw2014 -> {
            require(picks == mode.raceBonuses) { "2014 RAW: бонусы должны соответствовать расе/подрасе." }
            picks
        }
        is OriginMode.DistinctAssignment -> {
            val nonZero = picks.filterValues { it != 0 }
            // 1) разложены все фишки по разным характеристикам
            require(nonZero.size == mode.magnitudes.size) {
                "Нужно разложить все фишки ${mode.magnitudes} по разным характеристикам."
            }
            // 2) списки величин совпадают
            val given = nonZero.values.sortedDescending()
            val need = mode.magnitudes.sortedDescending()
            require(given == need) { "Нужно разложить величины $need по разным характеристикам." }
            // 3) доп.проверка: одна характеристика не получает две «фишки»
            val grouped = nonZero.keys.groupingBy { it }.eachCount()
            require(grouped.values.all { it == 1 }) { "Нельзя класть несколько фишек в один стат." }
            picks
        }
    }
}

// --- Утилиты слияния слоёв

fun Map<Ability, Int>.plusScores(other: Map<Ability, Int>): Map<Ability, Int> =
    (this.keys + other.keys)
        .associateWith { (this[it] ?: 0) + (other[it] ?: 0) }

// ===== Пример движка билдера без UI (упростил до чистых вызовов) =====

class CharacterBuilder(
    var ruleset: Ruleset
) {
    // шаг 1: база
    fun makeBase(method: BaseMethod,
                 assignmentProvider: () -> Map<Ability, Int> = { emptyMap() },
                 rollsProvider: () -> List<Int> = { emptyList() }
    ): Map<Ability, Int> = when (method) {
        is BaseMethod.StandardArray -> StandardArray.assign(assignmentProvider())
        is BaseMethod.PointBuy -> PointBuy.validate(assignmentProvider(), method.budget, method.min, method.max)
        is BaseMethod.Roll4d6DropLowest -> {
            val rolls = rollsProvider().ifEmpty { Roller.six() }
            Roller.assign(rolls, assignmentProvider())
        }
        is BaseMethod.Custom -> {
            val v = method.values
            require(v.size == 6) { "Нужно задать все 6 характеристик." }
            v
        }
    }

    // шаг 2: начисление стартовых бонусов (раса/бэкграунд)
    /**
     * Для 2014 RAW передавай OriginMode.Raw2014(raceBonuses).
     * Для 2014 (Tasha) и 2024 — OriginMode.DistinctAssignment(magnitudes).
     */
    fun applyOrigin(mode: OriginMode, picks: Map<Ability, Int>): Map<Ability, Int> =
        OriginAllocator.assign(mode, picks)

    // сведение слоёв в итог
    fun finalize(
        base: Map<Ability, Int>,
        origin: Map<Ability, Int>,
        featAsi: Map<Ability, Int> = emptyMap(),
        temporary: Map<Ability, Int> = emptyMap(),
        setTo: Map<Ability, Int> = emptyMap(),
        naturalCap: Int = 20
    ): Map<Ability, AbilityResult> =
        AbilityLayers(base, origin, featAsi, temporary, setTo, naturalCap).compute()
}

// ======== Примеры использования (можно удалить/закомментировать в проде) ========

@Suppress("unused")
fun demo2014Raw() {
    val engine = CharacterBuilder(Ruleset.R2014)

    // БАЗА (Standard Array) — UI дал сопоставление
    val base = engine.makeBase(
        BaseMethod.StandardArray,
        assignmentProvider = {
            mapOf(
                Ability.STR to 15, Ability.DEX to 14, Ability.CON to 13,
                Ability.INT to 12, Ability.WIS to 10, Ability.CHA to 8
            )
        }
    )

    // РАСА (пример: +2 STR, +1 CON — фиксировано)
    val origin = engine.applyOrigin(
        OriginMode.Raw2014(mapOf(Ability.STR to 2, Ability.CON to 1)),
        picks = mapOf(Ability.STR to 2, Ability.CON to 1) // ровно как в raceBonuses
    )

    // Фит/ASI на 1 уровне отсутствует (или, напр., +1 к STR)
    val featAsi = emptyMap<Ability, Int>()

    val result = engine.finalize(base, origin, featAsi)
    // result[Ability.STR]?.score и .modifier — готово
}

@Suppress("unused")
fun demo2024Background() {
    val engine = CharacterBuilder(Ruleset.R2024)

    // БАЗА (Point Buy) — UI собрал значения и проверка бюджета прошла
    val base = engine.makeBase(
        BaseMethod.PointBuy(),
        assignmentProvider = {
            mapOf(
                Ability.STR to 8, Ability.DEX to 14, Ability.CON to 15,
                Ability.INT to 15, Ability.WIS to 10, Ability.CHA to 8
            )
        }
    )

    // БЭКГРАУНД 2024: схема [+2,+1] в РАЗНЫЕ статы (например, +2 INT, +1 CON)
    val origin = engine.applyOrigin(
        OriginMode.DistinctAssignment(listOf(2, 1)),
        picks = mapOf(Ability.INT to 2, Ability.CON to 1)
    )

    // Фит 1 lvl → даёт +1 к INT (пример)
    val featAsi = mapOf(Ability.INT to 1)

    // Без временных/предметов
    val result = engine.finalize(base, origin, featAsi)
}