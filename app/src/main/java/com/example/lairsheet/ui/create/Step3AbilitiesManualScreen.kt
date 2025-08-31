package com.example.lairsheet.ui.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lairsheet.data.AbilityType
import com.example.lairsheet.data.Ruleset
import com.example.lairsheet.data.dnd.DndRepository2014

/**
 * Шаг 3 — «Manual» (ручной ввод).
 * - Редактирование значений для каждой характеристики (диапазон 3..18)
 * - Готовность: все значения в диапазоне (валидируется во VM)
 * - Автосохранение: onStateChanged(vm.state) после любого действия
 */
@Composable
fun Step3AbilitiesManualScreen(
    initial: CharacterCreationState,
    onBack: () -> Unit,
    onStateChanged: (AttributesUiState) -> Unit,
    onDone: (Map<AbilityType, Int>) -> Unit,
    vm: AttributesViewModel = viewModel()
) {
    val ctx = LocalContext.current
    val is2014 = remember(initial.selectedSources) { Ruleset.R5E_2014 in initial.selectedSources }

    LaunchedEffect(Unit) {
        // Инициализация метода и восстановление
        vm.selectMethod(AbilityMethod.CUSTOM)
        val base = if (initial.abilityScores.isNotEmpty()) {
            AbilityType.values().associateWith { t -> initial.abilityScores[t] ?: 8 }
        } else initial.manualValues
        vm.loadBase(base = base)

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Шаг 3: Распределение характеристик — Ручной ввод",
            style = MaterialTheme.typography.titleLarge
        )

        // Сетка характеристик с полями ввода
        AbilityGrid(
            totals = state.total,
            base = state.base,
            mode = state.method,
            onPlus = { /* no-op */ },
            onMinus = { /* no-op */ },
            onCustomChange = { t, v ->
                vm.setCustom(t, v, minV = 3, maxV = 18)
                onStateChanged(vm.state)
            }
        )

        TotalsCard(total = state.total)

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = {
                vm.loadBase(base = defaultBase())
                onStateChanged(vm.state)
            }) {
                Text("Сброс")
            }
            Spacer(modifier = androidx.compose.ui.Modifier.weight(1f))
            TextButton(onClick = onBack) { Text("Назад") }
            ElevatedButton(
                onClick = {
                    val totals = vm.state.total.mapValues { it.value }
                    onDone(totals)
                },
                enabled = state.ready
            ) { Text("Далее") }
        }
    }
}