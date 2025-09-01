package com.example.lairsheet.ui.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lairsheet.data.AbilityType
import com.example.lairsheet.data.Ruleset
import com.example.lairsheet.data.dnd.DndRepository2014
import com.example.lairsheet.ui.theme.DeepRed
import com.example.lairsheet.ui.theme.DiceIcon
import com.example.lairsheet.ui.create.NavBarButtons

/**
 * Шаг 3 — «Броски»:
 * Строка: Название (аббр) | A+B+C+D | = | Сумма | [Кнопка броска]
 * Итоговые значения — в раскрывающемся блоке.
 */
@Composable
fun Step3AbilitiesRollScreen(
    initial: CharacterCreationState,
    onBack: () -> Unit,
    onStateChanged: (AttributesUiState) -> Unit,
    onDone: (Map<AbilityType, Int>) -> Unit,
    vm: AttributesViewModel = viewModel()
) {
    val ctx = LocalContext.current
    val is2014 = remember(initial.selectedSources) { Ruleset.R5E_2014 in initial.selectedSources }

    LaunchedEffect(Unit) {
        vm.selectMethod(AbilityMethod.ROLL)
        val base = if (initial.abilityScores.isNotEmpty()) {
            AbilityType.values().associateWith { t -> initial.abilityScores[t] ?: 8 }
        } else initial.pointBuyValues
        val pool = initial.rollResults
        vm.loadBase(base = base, pool = pool)

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

    // Константы ширины колонок для выравнивания
    val nameW = 180.dp
    val exprW = 160.dp
    val eqW = 20.dp
    val sumW = 56.dp
    val btnW = 40.dp
    val btnH = 36.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Шаг 3: Распределение характеристик — Броски",
            style = MaterialTheme.typography.titleLarge,
            color = DeepRed
        )

        // Инфо-карта
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                Text("Броски 4×d6", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Для каждой характеристики нажмите «Бросок». Выпадает 4 значения (0..6). Отображается A+B+C+D = Сумма. Перебрасывать можно бесконечно.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }

        // Карта строк бросков
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Характеристики", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                AbilityType.values().forEach { vm.rollAbility(it) }
                                onStateChanged(vm.state)
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) { Text("Бросить всё") }
                        OutlinedButton(
                            onClick = {
                                vm.clearRolls()
                                vm.loadBase(base = defaultBase(), pool = emptyList())
                                onStateChanged(vm.state)
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) { Text("Сбросить всё") }
                    }
                }
                HorizontalDivider()

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

                AbilityType.values().forEachIndexed { index, t ->
                    val dice = state.rollDetails[t] ?: emptyList()
                    val baseVal = state.base[t] ?: 0
                    val totalVal = state.total[t] ?: baseVal

                    val expr = if (dice.size == 4) {
                        "${dice[0]} + ${dice[1]} + ${dice[2]} + ${dice[3]}"
                    } else {
                        "— + — + — + —"
                    }
                    val sum = if (dice.size == 4) dice.sum().toString() else "—"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Характеристика (аббр)
                        Text(
                            text = "${names[t] ?: t.name} (${abbr[t] ?: t.name.take(3)})",
                            modifier = Modifier.width(nameW),
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // A+B+C+D
                        Text(
                            text = expr,
                            modifier = Modifier.width(exprW),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        // "="
                        Text(
                            text = "=",
                            modifier = Modifier.width(eqW),
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )

                        // (Сумма)
                        Text(
                            text = sum,
                            modifier = Modifier.width(sumW),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (dice.size == 4) DeepRed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )

                        // Заполнитель — выравнивает кнопку по правому краю строки
                        Spacer(modifier = Modifier.weight(1f))

                        // Кнопка броска кубика
                        IconButton(
                            onClick = {
                                vm.rollAbility(t)
                                onStateChanged(vm.state)
                            },
                            modifier = Modifier
                                .width(btnW)
                                .height(btnH)
                        ) {
                            DiceIcon(
                                sides = 6,
                                modifier = Modifier
                                    .width(18.dp)
                                    .height(18.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (index != AbilityType.values().lastIndex) HorizontalDivider()
                }
            }
        }

        // Итоговые значения — в раскрывающемся блоке
        ExpandableCard(
            title = "Итоговые значения",
            description = "Раскройте, чтобы посмотреть текущие итоговые значения после суммирования.",
            initiallyExpanded = false
        ) {
            TotalsCard(total = state.total)
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = {
                vm.clearRolls()
                vm.loadBase(base = defaultBase(), pool = emptyList())
                onStateChanged(vm.state)
            }) {
                Text("Сброс")
            }
        }

        NavBarButtons(
            onBack = onBack,
            onNext = {
                val totals = vm.state.total.mapValues { it.value }
                onDone(totals)
            },
            nextEnabled = state.ready
        )
    }
}