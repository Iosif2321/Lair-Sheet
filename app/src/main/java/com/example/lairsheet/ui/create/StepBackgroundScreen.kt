package com.example.lairsheet.ui.create

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.lairsheet.data.AbilityType

@Composable
fun StepBackgroundScreen(
    state: CharacterCreationState,
    backgrounds: List<Background>,
    onSelect: (Background) -> Unit,
    onAssignBonus: (AbilityType, Int) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val selected = state.selectedBackground
    val bonuses = state.backgroundBonuses

    val scheme = selected?.scheme
    val targetMagnitudes = remember(scheme) {
        scheme?.sortedDescending() ?: emptyList()
    }

    // Проверка корректности распределения фишек
    val assignedOk = remember(bonuses, targetMagnitudes) {
        if (targetMagnitudes.isEmpty()) true
        else {
            val nonZero = bonuses.filterValues { it != 0 }
            // Размер должен совпасть со схемой
            if (nonZero.size != targetMagnitudes.size) return@remember false
            // Все в разные статы (map.keys уникальны по определению)
            val given = nonZero.values.sortedDescending()
            given == targetMagnitudes
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Предыстория", style = MaterialTheme.typography.titleLarge)
        Text(
            "Выберите предысторию. Для правил 2024 распределите бонусы по разным характеристикам согласно схеме.",
            style = MaterialTheme.typography.bodyMedium
        )

        // Список предысторий
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                backgrounds.forEach { bg ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(bg) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected == bg,
                            onClick = { onSelect(bg) }
                        )
                        Column(Modifier.padding(start = 8.dp)) {
                            Text(bg.nameRu, style = MaterialTheme.typography.titleMedium)
                            if (bg.shortDescription.isNotBlank()) {
                                Text(
                                    bg.shortDescription,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (bg.scheme != null) {
                                Text(
                                    "Бонусы: ${bg.scheme}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            bg.feat?.let {
                                Text(
                                    "Черта: $it",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                    HorizontalDivider()
                }
            }
        }

        // Панель распределения бонусов (только если у выбранной предыстории есть схема)
        if (!targetMagnitudes.isNullOrEmpty()) {
            Text("Распределите бонусы ${targetMagnitudes} по разным характеристикам", style = MaterialTheme.typography.titleMedium)

            // Подсчёт уже использованных «фишек» каждой величины
            val usedCounts = targetMagnitudes.groupingBy { it }.eachCount().mapValues { (mag, req) ->
                val used = bonuses.values.count { it == mag }
                mag to Pair(used, req)
            }

            val names = mapOf(
                AbilityType.STRENGTH to "Сила",
                AbilityType.DEXTERITY to "Ловкость",
                AbilityType.CONSTITUTION to "Телосложение",
                AbilityType.INTELLIGENCE to "Интеллект",
                AbilityType.WISDOM to "Мудрость",
                AbilityType.CHARISMA to "Харизма"
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AbilityType.values().forEach { t ->
                    val current = bonuses[t] ?: 0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(names[t] ?: t.name, modifier = Modifier.weight(1f))
                        targetMagnitudes.distinct().sorted().forEach { m ->
                            val pair = usedCounts[m]!!
                            val used = pair.second.first
                            val req = pair.second.second
                            // Разрешено нажимать, если эта «фишка» ещё не исчерпана ИЛИ если уже выбрана у этой способности
                            val enabled = (current == m) || (used < req)
                            OutlinedButton(
                                onClick = {
                                    // toggle: если уже стоит эта величина — снимаем (0), иначе ставим m
                                    val newVal = if (current == m) 0 else m
                                    onAssignBonus(t, newVal)
                                },
                                enabled = enabled,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                val mark = if (current == m) "✓" else "+"
                                Text("$mark$m")
                            }
                        }
                        // Кнопка сброса выбранного бонуса по этой характеристике (если есть)
                        if (current != 0) {
                            Spacer(Modifier.width(8.dp))
                            OutlinedButton(onClick = { onAssignBonus(t, 0) }) { Text("Сброс") }
                        }
                    }
                    HorizontalDivider()
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        NavBarButtons(
            onBack = onBack,
            onNext = onNext,
            nextEnabled = selected != null && (selected.scheme == null || assignedOk)
        )
    }
}