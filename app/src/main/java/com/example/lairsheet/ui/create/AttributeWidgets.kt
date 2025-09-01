package com.example.lairsheet.ui.create

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.lairsheet.data.AbilityScore
import com.example.lairsheet.data.AbilityType
import com.example.lairsheet.ui.theme.DeepRed
import com.example.lairsheet.ui.theme.LightPink

@Composable
fun PoolChips(
    pool: List<Int>,
    onAssign: (value: Int, type: AbilityType) -> Unit
) {
    if (pool.isEmpty()) {
        Text("Пул пуст", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        return
    }
    Column {
        Text("Пул значений:", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(0.dp))
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AbilityType.values().forEach { ability ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    pool.forEach { v ->
                        Chip(text = "$v") { onAssign(v, ability) }
                    }
                }
            }
        }
    }
}

@Composable
fun Chip(text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .clickable { onClick() },
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Text(text, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
    }
}

@Composable
fun AbilityGrid(
    totals: Map<AbilityType, Int>,
    base: Map<AbilityType, Int>,
    mode: AbilityMethod,
    onPlus: (AbilityType) -> Unit,
    onMinus: (AbilityType) -> Unit,
    onCustomChange: (AbilityType, Int) -> Unit
) {
    val names = mapOf(
        AbilityType.STRENGTH to "Сила",
        AbilityType.DEXTERITY to "Ловкость",
        AbilityType.CONSTITUTION to "Телосложение",
        AbilityType.INTELLIGENCE to "Интеллект",
        AbilityType.WISDOM to "Мудрость",
        AbilityType.CHARISMA to "Харизма"
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AbilityType.values().forEach { t ->
            Card {
                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                    Text(names[t] ?: t.name, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val baseVal = base[t] ?: 8
                        val totalVal = totals[t] ?: baseVal
                        val delta = totalVal - baseVal
                        Text("База: $baseVal")
                        Text("Итог: $totalVal", fontWeight = FontWeight.Bold, color = DeepRed)
                        if (delta != 0) {
                            val ds = if (delta > 0) "+$delta" else "$delta"
                            Text(ds, color = DeepRed)
                        }
                    }
                    when (mode) {
                        AbilityMethod.POINT_BUY -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { onMinus(t) }) { Text("−") }
                                TextButton(onClick = { onPlus(t) }) { Text("+") }
                            }
                        }
                        AbilityMethod.CUSTOM -> {
                            var txt by remember(t) { mutableStateOf((base[t] ?: 8).toString()) }
                            OutlinedTextField(
                                value = txt,
                                onValueChange = {
                                    txt = it
                                    it.toIntOrNull()?.let { v -> onCustomChange(t, v) }
                                },
                                label = { Text("Значение") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(120.dp)
                            )
                        }
                        else -> { /* Standard / Roll: управление через пул */ }
                    }
                }
            }
        }
    }
}

@Composable
fun RacialBonusesPanel(
    mode: RacialBonusMode,
    scheme: FlexibleScheme,
    bonuses: Map<AbilityType, Int>,
    onToggleMode: (RacialBonusMode) -> Unit,
    onSelectScheme: (FlexibleScheme) -> Unit,
    onChangeBonus: (AbilityType, Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text("Расовые бонусы", style = MaterialTheme.typography.titleMedium, color = DeepRed)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Фиксированные (2014)")
                Checkbox(
                    checked = mode == RacialBonusMode.FIXED_2014,
                    onCheckedChange = { chk -> onToggleMode(if (chk) RacialBonusMode.FIXED_2014 else RacialBonusMode.FLEXIBLE_2024) }
                )
            }
            if (mode == RacialBonusMode.FLEXIBLE_2024) {
                Text("Схема распределения", fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    RadioButton(selected = scheme == FlexibleScheme.PLUS2_PLUS1, onClick = { onSelectScheme(FlexibleScheme.PLUS2_PLUS1) })
                    Text("+2 и +1")
                    RadioButton(selected = scheme == FlexibleScheme.PLUS1x3, onClick = { onSelectScheme(FlexibleScheme.PLUS1x3) })
                    Text("+1 × 3")
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
                        val v = bonuses[t] ?: 0
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(names[t] ?: t.name, modifier = Modifier.weight(1f))
                            TextButton(onClick = { onChangeBonus(t, -1) }) { Text("−") }
                            Text("$v", modifier = Modifier.width(24.dp))
                            TextButton(onClick = { onChangeBonus(t, +1) }) { Text("+") }
                        }
                        HorizontalDivider()
                    }
                }
            } else {
                Text("Фиксированные бонусы будут применены автоматически (если заданы у выбранной расы).")
            }
        }
    }
}

@Composable
fun TotalsCard(total: Map<AbilityType, Int>) {
    Card {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text("Итоговые значения", fontWeight = FontWeight.SemiBold)
            total.forEach { (t, v) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        when (t) {
                            AbilityType.STRENGTH -> "Сила"
                            AbilityType.DEXTERITY -> "Ловкость"
                            AbilityType.CONSTITUTION -> "Телосложение"
                            AbilityType.INTELLIGENCE -> "Интеллект"
                            AbilityType.WISDOM -> "Мудрость"
                            AbilityType.CHARISMA -> "Харизма"
                        }
                    )
                    val mod = java.lang.Math.floorDiv(v - 10, 2)
                    Text("$v (мод ${if (mod >= 0) "+$mod" else "$mod"})", fontWeight = FontWeight.SemiBold)
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun PointBuyConfigPanel(
    min: Int,
    max: Int,
    budget: Int,
    onApply: (Int, Int, Int) -> Unit
) {
    var minTxt by remember { mutableStateOf(min.toString()) }
    var maxTxt by remember { mutableStateOf(max.toString()) }
    var budTxt by remember { mutableStateOf(budget.toString()) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Настройки Point Buy", fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = minTxt, onValueChange = { minTxt = it }, label = { Text("Min") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = maxTxt, onValueChange = { maxTxt = it }, label = { Text("Max") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = budTxt, onValueChange = { budTxt = it }, label = { Text("Бюджет") }, modifier = Modifier.weight(1f))
        }
        TextButton(onClick = {
            val mi = minTxt.toIntOrNull() ?: min
            val ma = maxTxt.toIntOrNull() ?: max
            val bu = budTxt.toIntOrNull() ?: budget
            onApply(mi, ma, bu)
        }) { Text("Применить") }
    }
}

@Composable
fun RollConfigPanel(
    formula: String,
    count: Int,
    onApply: (String, Int) -> Unit,
    onRoll: () -> Unit
) {
    var f by remember { mutableStateOf(formula) }
    var cTxt by remember { mutableStateOf(count.toString()) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Настройки броска", fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = f, onValueChange = { f = it }, label = { Text("Формула") }, modifier = Modifier.weight(2f))
            OutlinedTextField(value = cTxt, onValueChange = { cTxt = it }, label = { Text("Кол-во") }, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = {
                val c = cTxt.toIntOrNull() ?: count
                onApply(f, c)
            }) { Text("Применить") }
            Button(onClick = onRoll) { Text("Бросок") }
        }
    }
}