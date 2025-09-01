package com.example.lairsheet.ui.create

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dnd.lair.core.PointBuy as CorePointBuy
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lairsheet.data.AbilityType
import com.example.lairsheet.data.Ruleset
import com.example.lairsheet.data.dnd.DndRepository2014
import com.example.lairsheet.ui.create.NavBarButtons

/**
 * Шаг 3 — «Point Buy» с макетом, как на референсах.
 * - Блок «Бюджет очков»: прогресс, Сброс, показатели Потрачено/Осталось/Бюджет.
 * - Блок «Таблица стоимости»: ряд 8..15 и стоимостей.
 * - Блок «Характеристики»: для каждой характеристики круглые кнопки − / +, в центре текущее значение и стоимость,
 *   справа «Итог» и модификатор.
 * - Любое изменение триггерит onStateChanged(vm.state).
 */
@Composable
fun Step3AbilitiesPointBuyScreen(
    initial: CharacterCreationState,
    onBack: () -> Unit,
    onStateChanged: (AttributesUiState) -> Unit,
    onDone: (Map<AbilityType, Int>) -> Unit,
    vm: AttributesViewModel = viewModel()
) {
    val ctx = LocalContext.current
    val is2014 = remember(initial.selectedSources) { Ruleset.R5E_2014 in initial.selectedSources }

    LaunchedEffect(Unit) {
        // Инициализация метода и восстановление из state
        vm.selectMethod(AbilityMethod.POINT_BUY)
        val base = if (initial.abilityScores.isNotEmpty()) {
            AbilityType.values().associateWith { t -> initial.abilityScores[t] ?: 8 }
        } else initial.pointBuyValues
        val rem = initial.pointBuyRemaining
        vm.loadBase(base = base, remainingBudget = rem)

        if (is2014) {
            val repo2014 = DndRepository2014(ctx)
            val eff = initial.selectedRace?.let { r -> repo2014.getRaceEffects(r, initial.selectedSubrace) }
            val fixed = eff?.abilityBonuses?.mapNotNull { (k, v) ->
                val at = when (k.lowercase()) {
                    "strength" -> AbilityType.STRENGTH
                    "dexterity" -> AbilityType.DEXTERITY
                    "constitution" -> AbilityType.CONSTITUTION
                    "intelligence" -> AbilityType.INTELLIGENCE
                    "wisdom" -> AbilityType.WISDOM
                    "charisma" -> AbilityType.CHARISMA
                    else -> null
                }
                at?.let { it to v }
            }?.toMap() ?: emptyMap()
            vm.toggleRacialMode(RacialBonusMode.FIXED_2014)
            vm.setFixedBonuses(fixed)
        } else {
            vm.toggleRacialMode(RacialBonusMode.FLEXIBLE_2024)
            vm.setFixedBonuses(emptyMap())
        }

        onStateChanged(vm.state)
    }

    val state = remember(vm.state) { vm.state }

    val names = remember {
        mapOf(
            AbilityType.STRENGTH to "Сила",
            AbilityType.DEXTERITY to "Ловкость",
            AbilityType.CONSTITUTION to "Телосложение",
            AbilityType.INTELLIGENCE to "Интеллект",
            AbilityType.WISDOM to "Мудрость",
            AbilityType.CHARISMA to "Харизма"
        )
    }
    val abbr = remember {
        mapOf(
            AbilityType.STRENGTH to "СИЛ",
            AbilityType.DEXTERITY to "ЛОВ",
            AbilityType.CONSTITUTION to "ТЕЛ",
            AbilityType.INTELLIGENCE to "ИНТ",
            AbilityType.WISDOM to "МУД",
            AbilityType.CHARISMA to "ХАР"
        )
    }

    val budget = state.pointBuy.budget
    val spent = (budget - state.remainingBudget).coerceAtLeast(0)
    val progress = if (budget > 0) spent.toFloat() / budget.toFloat() else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Заголовок шага (опционально сверху экрана)
        Text("Определение характеристик", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)

        // Карточка режима (легкий анонс)
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                Text("Point Buy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Распределите $budget очков между характеристиками", style = MaterialTheme.typography.bodySmall)
            }
        }

        // Блок «Бюджет очков»
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Бюджет очков", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    OutlinedButton(
                        onClick = {
                            // Сброс к дефолту 27, диапазон 8..15
                            vm.setPointBuyConfig(minV = 8, maxV = 15, budget = 27, costs = null)
                            vm.loadBase(base = defaultBase(), remainingBudget = 27)
                            onStateChanged(vm.state)
                        },
                        shape = MaterialTheme.shapes.large,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Сброс", tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.size(8.dp))
                        Text("Сброс")
                    }
                }
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth(),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Потрачено: $spent", style = MaterialTheme.typography.bodyMedium)
                    Text("Осталось: ${state.remainingBudget}", style = MaterialTheme.typography.bodyMedium)
                    Text("Бюджет: $budget", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Блок «Таблица стоимости»
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Таблица стоимости", style = MaterialTheme.typography.titleMedium)
                // Ряд значений
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    (8..15).forEach { v ->
                        Text("$v", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                // Ряд стоимостей
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    (8..15).forEach { v ->
                        val cost = CorePointBuy.costFor(v)
                        Text("${cost}очк", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                }
            }
        }

        // Блок «Характеристики»
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text(
                    "Характеристики",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
                HorizontalDivider()

                AbilityType.values().forEachIndexed { index, t ->
                    val baseVal = state.base[t] ?: 8
                    val totalVal = state.total[t] ?: baseVal
                    val mod = java.lang.Math.floorDiv(totalVal - 10, 2)
                    val costForVal = state.pointBuy.costs[baseVal] ?: 0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(names[t] ?: t.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(abbr[t] ?: t.name.take(3), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }

                        // Кнопка уменьшения
                        CircleIconButton(
                            enabled = baseVal > state.pointBuy.min,
                            onClick = {
                                vm.dec(t)
                                onStateChanged(vm.state)
                            },
                            icon = { Icon(Icons.Filled.Remove, contentDescription = "−") }
                        )

                        // Центр: текущее значение и его стоимость
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Text("$baseVal", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("${costForVal}очк", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }

                        // Кнопка увеличения
                        val canInc = baseVal < state.pointBuy.max
                        CircleIconButton(
                            enabled = canInc,
                            onClick = {
                                vm.inc(t)
                                onStateChanged(vm.state)
                            },
                            icon = { Icon(Icons.Filled.Add, contentDescription = "+") }
                        )

                        // Справа: Итог и модификатор
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.padding(start = 16.dp)
                        ) {
                            Text("$totalVal", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            val modStr = if (mod >= 0) "+$mod" else "$mod"
                            Text(modStr, style = MaterialTheme.typography.bodySmall, color = Color(0xFF1E88E5))
                            val deltaVal = totalVal - baseVal
                            val deltaStr = if (deltaVal >= 0) "+$deltaVal" else "$deltaVal"
                            Text(deltaStr, style = MaterialTheme.typography.bodySmall, color = Color(0xFF1E88E5))
                        }
                    }

                    if (index != AbilityType.values().lastIndex) Divider(thickness = 1.dp)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        NavBarButtons(
            onBack = onBack,
            onNext = {
                val totals = state.total.mapValues { it.value }
                onDone(totals)
            },
            nextEnabled = state.ready
        )
    }
}

@Composable
private fun CircleIconButton(
    enabled: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = CircleShape,
        modifier = Modifier.size(40.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        icon()
    }
}