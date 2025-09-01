package com.example.lairsheet.ui.create

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.lairsheet.data.AbilityScore
import com.example.lairsheet.data.AbilityType
import com.example.lairsheet.core.toCore
import com.example.lairsheet.core.toApp
import dnd.lair.core.AbilityLayers
import dnd.lair.core.OriginMode
import dnd.lair.core.OriginAllocator
import dnd.lair.core.PointBuy as CorePointBuy
import dnd.lair.core.Roller as CoreRoller
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class AttributesViewModel : ViewModel() {

    var state by mutableStateOf(AttributesUiState())
        private set

    fun selectMethod(method: AbilityMethod) {
        state = state.copy(method = method).recalcTotals()
    }

    /**
     * Императивная загрузка базовых значений/бюджета/пула для восстановления шага.
     */
    fun loadBase(
        base: Map<AbilityType, Int>,
        remainingBudget: Int? = null,
        pool: List<Int>? = null
    ) {
        var p = pool ?: state.pool
        if (state.method == AbilityMethod.STANDARD_ARRAY || state.method == AbilityMethod.ROLL) {
            val used = base.values.groupingBy { it }.eachCount()
            val all = p.groupingBy { it }.eachCount().toMutableMap()
            // вычитаем использованные значения из пула
            used.forEach { (v, c) -> all[v] = (all[v] ?: 0) - c }
            p = all.flatMap { (v, c) -> List(c.coerceAtLeast(0)) { v } }
        }
        state = state.copy(
            base = base,
            remainingBudget = remainingBudget ?: state.remainingBudget,
            pool = p
        ).recalcTotals()
    }

    // ===== Standard Array =====

    fun applyStandardArrayDefaultOrder() {
        // Присваиваем 15,14,13,12,10,8 по фиксированному порядку характеристик
        val arr = listOf(15, 14, 13, 12, 10, 8)
        val keys = AbilityType.values()
        val base = keys.indices.associate { keys[it] to arr[it] }
        state = state.copy(base = base, pool = emptyList()).recalcTotals()
    }

    // ===== Point Buy =====

    fun inc(type: AbilityType) {
        val pb = state.pointBuy
        val current = state.base[type] ?: pb.min
        if (current >= pb.max) return
        val next = current + 1
        val costNow = costFor(current, pb)
        val costNext = costFor(next, pb)
        val delta = costNext - costNow
        if (state.remainingBudget - delta < 0) return
        val newBase = state.base.toMutableMap()
        newBase[type] = next
        state = state.copy(
            base = newBase,
            remainingBudget = state.remainingBudget - delta
        ).recalcTotals()
    }

    fun dec(type: AbilityType) {
        val pb = state.pointBuy
        val current = state.base[type] ?: pb.min
        if (current <= pb.min) return
        val prev = current - 1
        val costNow = costFor(current, pb)
        val costPrev = costFor(prev, pb)
        val delta = costNow - costPrev // возвращаем бюджет
        val newBase = state.base.toMutableMap()
        newBase[type] = prev
        state = state.copy(
            base = newBase,
            remainingBudget = state.remainingBudget + delta
        ).recalcTotals()
    }

    fun setPointBuyConfig(minV: Int? = null, maxV: Int? = null, budget: Int? = null, costs: Map<Int, Int>? = null) {
        val cur = state.pointBuy
        val conf = cur.copy(
            min = minV ?: cur.min,
            max = maxV ?: cur.max,
            budget = budget ?: cur.budget,
            costs = costs ?: cur.costs
        )
        // Пересчитываем remainingBudget заново от текущих значений
        val spent = state.base.entries.sumOf { (k, v) -> costFor(v, conf) }
        val rem = max(0, conf.budget - spent)
        state = state.copy(pointBuy = conf, remainingBudget = rem).recalcTotals()
    }

    private fun costFor(value: Int, conf: PointBuyConfig): Int {
        // Стоимость RAW 5e через ядро (диапазон принудительно 8..15)
        val lo = max(8, conf.min)
        val hi = min(15, conf.max)
        val v = min(max(value, lo), hi)
        return try {
            CorePointBuy.costFor(v)
        } catch (_: Throwable) {
            0
        }
    }

    // ===== Roll (броски) =====

    fun rollPool() {
        val rolls = rollByFormula(state.roll.formula, state.roll.rollsCount)
        state = state.copy(
            pool = rolls
        )
    }

    // Пер-абилити бросок 4d6 (значения 0..6, сумма пишется в базу для выбранной характеристики)
    fun rollAbility(type: AbilityType) {
        val dice = List(4) { Random.nextInt(0, 7) }
        val sum = dice.sum()
        val rd = state.rollDetails.toMutableMap()
        rd[type] = dice
        val base = state.base.toMutableMap()
        base[type] = sum
        state = state.copy(rollDetails = rd, base = base).recalcTotals()
    }

    fun setRollConfig(formula: String? = null, count: Int? = null) {
        val r = state.roll.copy(
            formula = formula ?: state.roll.formula,
            rollsCount = count ?: state.roll.rollsCount
        )
        state = state.copy(roll = r)
    }

    fun clearRolls() {
        state = state.copy(rollDetails = emptyMap()).recalcTotals()
    }

    private fun rollByFormula(formula: String, count: Int): List<Int> {
        // Быстрый путь RAW 4d6dl1×6 — используем ядро
        if (formula.trim().equals("4d6dl1", ignoreCase = true) && count == 6) {
            return CoreRoller.six().sortedDescending()
        }
        // Fallback: простой парсер NdXdlY
        val regex = Regex("(\\d+)d(\\d+)(dl(\\d+))?")
        val m = regex.matchEntire(formula.lowercase())
        val nStr = m?.groupValues?.getOrNull(1)
        val xStr = m?.groupValues?.getOrNull(2)
        val dlStr = m?.groupValues?.getOrNull(4)
        val n = nStr?.toIntOrNull() ?: 4
        val x = xStr?.toIntOrNull() ?: 6
        val dl = dlStr?.toIntOrNull() ?: 1

        fun single(): Int {
            val rolls = IntArray(n) { Random.nextInt(1, x + 1) }.sorted()
            val kept = rolls.drop(dl)
            return kept.sum()
        }
        return List(count) { single() }.sortedDescending()
    }

    fun assignFromPool(type: AbilityType, value: Int) {
        val pool = state.pool.toMutableList()
        if (!pool.contains(value)) return
        val newBase = state.base.toMutableMap()
        // Если у способности было что-то не из допустимого диапазона, просто заменяем
        newBase[type] = value
        // Удаляем одно вхождение из пула
        pool.remove(value)
        state = state.copy(base = newBase, pool = pool).recalcTotals()
    }

    // ===== Custom direct set =====

    fun setCustom(type: AbilityType, value: Int, minV: Int = 1, maxV: Int = 30) {
        val v = min(max(value, minV), maxV)
        val newBase = state.base.toMutableMap()
        newBase[type] = v
        state = state.copy(base = newBase).recalcTotals()
    }

    // ===== Расовые бонусы =====

    fun toggleRacialMode(mode: RacialBonusMode) {
        state = state.copy(racialMode = mode).recalcTotals()
    }

    fun setFlexibleScheme(scheme: FlexibleScheme) {
        state = state.copy(flexible = state.flexible.copy(scheme = scheme)).recalcTotals()
    }

    fun setFixedBonuses(bonuses: Map<AbilityType, Int>) {
        state = state.copy(fixedBonuses = bonuses).recalcTotals()
    }

    fun setFlexibleBonus(ability: AbilityType, delta: Int) {
        // Ограничиваем по схеме: +2/+1 или +1×3
        val flex = state.flexible
        val current = flex.bonuses[ability] ?: 0
        val new = (current + delta).coerceIn(0, 2)
        val newMap = flex.bonuses.toMutableMap()
        newMap[ability] = new

        // Нормализуем суммарно под схему
        val sum = newMap.values.sum()
        val (targetSum, maxPerStat) = when (flex.scheme) {
            FlexibleScheme.PLUS2_PLUS1 -> 3 to 2
            FlexibleScheme.PLUS1x3 -> 3 to 1
        }

        // Жестко обрезаем превышения maxPerStat
        for ((k, v) in newMap) {
            if (v > maxPerStat) newMap[k] = maxPerStat
        }

        // Приводим сумму к targetSum (обрезаем излишки по убыванию значений)
        var total = newMap.values.sum()
        if (total > targetSum) {
            val sorted = newMap.entries.sortedByDescending { it.value }
            var i = 0
            while (total > targetSum && i < sorted.size) {
                val (k, v) = sorted[i]
                if (v > 0) {
                    newMap[k] = v - 1
                    total--
                }
                i++
            }
        }

        state = state.copy(flexible = flex.copy(bonuses = newMap)).recalcTotals()
    }

    // ===== Итоги и готовность =====

    private fun AttributesUiState.recalcTotals(): AttributesUiState {
        // Рассчитываем origin через ядро (безопасно: при невалидном выборе получаем emptyMap())
        val originCore: Map<dnd.lair.core.Ability, Int> = when (racialMode) {
            RacialBonusMode.FLEXIBLE_2024 -> {
                val mags = when (flexible.scheme) {
                    FlexibleScheme.PLUS2_PLUS1 -> listOf(2, 1)
                    FlexibleScheme.PLUS1x3 -> listOf(1, 1, 1)
                }
                val picks = flexible.bonuses.toCore()
                try {
                    OriginAllocator.assign(OriginMode.DistinctAssignment(mags), picks)
                } catch (_: Throwable) {
                    emptyMap()
                }
            }
            RacialBonusMode.FIXED_2014 -> fixedBonuses.toCore()
        }

        // Сведение слоёв через ядро
        val final: Map<AbilityType, Int> = AbilityLayers(
            base = base.toCore(),
            origin = originCore,
            featAsi = emptyMap(),
            temporary = emptyMap(),
            setTo = emptyMap(),
            naturalCap = 20
        ).compute().entries.associate { (k, v) -> k.toApp() to v.score }

        val readyFlag = isReadyInternal(final)
        return copy(total = final, ready = readyFlag)
    }

    private fun isReadyInternal(total: Map<AbilityType, Int>): Boolean {
        // Все 6 характеристик должны быть заданы
        if (total.size != AbilityType.values().size) return false

        when (state.method) {
            AbilityMethod.POINT_BUY -> {
                // Бюджет не должен быть отрицательным, значения в указанном диапазоне
                if (state.remainingBudget < 0) return false
                val inRange = state.base.values.all { it in state.pointBuy.min..state.pointBuy.max }
                if (!inRange) return false
                // RAW‑валидатор ядра (диапазон ограничиваем 8..15 во избежание невалидных конфигов)
                val minEff = max(8, state.pointBuy.min)
                val maxEff = min(15, state.pointBuy.max)
                val baseCore = state.base.toCore()
                try {
                    CorePointBuy.validate(baseCore, state.pointBuy.budget, minEff, maxEff)
                } catch (_: Throwable) {
                    return false
                }
            }
            AbilityMethod.STANDARD_ARRAY -> {
                // Для Standard Array — пул должен быть пуст (все 6 значений назначены)
                if (state.pool.isNotEmpty()) return false
            }
            AbilityMethod.ROLL -> {
                // Для пер-абилити бросков — должны быть выполнены броски по всем 6 характеристикам
                if (state.rollDetails.size != AbilityType.values().size) return false
            }
            AbilityMethod.CUSTOM -> {
                // Ручной ввод — проверки диапазона 3..18
                val inRange = state.base.values.all { it in 3..18 }
                if (!inRange) return false
            }
        }

        // Проверка валидности гибкой схемы 2024 через ядро
        if (state.racialMode == RacialBonusMode.FLEXIBLE_2024) {
            val mags = when (state.flexible.scheme) {
                FlexibleScheme.PLUS2_PLUS1 -> listOf(2, 1)
                FlexibleScheme.PLUS1x3 -> listOf(1, 1, 1)
            }
            val picks = state.flexible.bonuses.toCore()
            val ok = try {
                OriginAllocator.assign(OriginMode.DistinctAssignment(mags), picks)
                true
            } catch (_: Throwable) {
                false
            }
            if (!ok) return false
        }

        return true
    }

    fun toAbilityScores(): Map<AbilityType, AbilityScore> {
        return state.total.mapValues { (k, v) -> AbilityScore(k, v) }
    }
}