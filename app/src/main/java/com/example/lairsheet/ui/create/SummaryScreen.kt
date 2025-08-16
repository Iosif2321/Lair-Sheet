package com.example.lairsheet.ui.create

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SummaryScreen(
    viewModel: CharacterViewModel,
    onFinish: () -> Unit
) {
    val state = viewModel.state
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Шаг 7: Итоговая информация")
        Spacer(modifier = Modifier.height(16.dp))
        Text("Имя: ${state.name}")
        Text("Источники: ${state.selectedSources.joinToString()}")
        Text("Раса: ${state.selectedRace}" + if (state.selectedSubrace.isNotBlank()) " (${state.selectedSubrace})" else "")
        Text("Класс: ${state.selectedClass}" + if (state.selectedSubclass.isNotBlank()) " (${state.selectedSubclass})" else "")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Характеристики:")
        Text("  Сила: ${state.strength}")
        Text("  Ловкость: ${state.dexterity}")
        Text("  Телосложение: ${state.constitution}")
        Text("  Интеллект: ${state.intelligence}")
        Text("  Мудрость: ${state.wisdom}")
        Text("  Харизма: ${state.charisma}")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Предыстория: ${state.selectedBackground}")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Снаряжение: ${state.selectedEquipment.joinToString()}")
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onFinish, modifier = Modifier.align(Alignment.End)) {
            Text("Сохранить")
        }
    }
}
