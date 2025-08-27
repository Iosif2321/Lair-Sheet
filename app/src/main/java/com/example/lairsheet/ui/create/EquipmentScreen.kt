package com.example.lairsheet.ui.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EquipmentScreen(
    viewModel: CharacterViewModel,
    onNext: () -> Unit
) {
    val equipmentOptions = listOf("Короткий меч", "Длинный меч", "Легкий лук", "Щит", "Панцирь", "Зелье лечения")
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Шаг 6: Выберите снаряжение")
        Spacer(modifier = Modifier.height(16.dp))
        equipmentOptions.forEach { item ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = viewModel.state.selectedEquipment.contains(item),
                        onValueChange = {
                            val current = viewModel.state.selectedEquipment.toMutableList()
                            if (current.contains(item)) current.remove(item) else current.add(item)
                            viewModel.setEquipment(current)
                        }
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = viewModel.state.selectedEquipment.contains(item),
                    onCheckedChange = {
                        val current = viewModel.state.selectedEquipment.toMutableList()
                        if (current.contains(item)) current.remove(item) else current.add(item)
                        viewModel.setEquipment(current)
                    }
                )
                Spacer(Modifier.width(8.dp))
                Text(text = item)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onNext,
            enabled = viewModel.state.selectedEquipment.isNotEmpty(),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Далее")
        }
    }
}
