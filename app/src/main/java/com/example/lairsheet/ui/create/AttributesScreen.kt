package com.example.lairsheet.ui.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AttributesScreen(
    viewModel: CharacterViewModel,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Шаг 4: Введите значения характеристик (обычно 8–18)")
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = viewModel.state.strength.toString(),
                onValueChange = { str ->
                    val v = str.toIntOrNull() ?: viewModel.state.strength
                    viewModel.setAttributeScores(
                        v, viewModel.state.dexterity, viewModel.state.constitution,
                        viewModel.state.intelligence, viewModel.state.wisdom, viewModel.state.charisma
                    )
                },
                label = { Text("Сила") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = viewModel.state.dexterity.toString(),
                onValueChange = { dex ->
                    val v = dex.toIntOrNull() ?: viewModel.state.dexterity
                    viewModel.setAttributeScores(
                        viewModel.state.strength, v, viewModel.state.constitution,
                        viewModel.state.intelligence, viewModel.state.wisdom, viewModel.state.charisma
                    )
                },
                label = { Text("Ловкость") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = viewModel.state.constitution.toString(),
                onValueChange = { con ->
                    val v = con.toIntOrNull() ?: viewModel.state.constitution
                    viewModel.setAttributeScores(
                        viewModel.state.strength, viewModel.state.dexterity, v,
                        viewModel.state.intelligence, viewModel.state.wisdom, viewModel.state.charisma
                    )
                },
                label = { Text("Телосложение") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = viewModel.state.intelligence.toString(),
                onValueChange = { intv ->
                    val v = intv.toIntOrNull() ?: viewModel.state.intelligence
                    viewModel.setAttributeScores(
                        viewModel.state.strength, viewModel.state.dexterity, viewModel.state.constitution,
                        v, viewModel.state.wisdom, viewModel.state.charisma
                    )
                },
                label = { Text("Интеллект") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = viewModel.state.wisdom.toString(),
                onValueChange = { wis ->
                    val v = wis.toIntOrNull() ?: viewModel.state.wisdom
                    viewModel.setAttributeScores(
                        viewModel.state.strength, viewModel.state.dexterity, viewModel.state.constitution,
                        viewModel.state.intelligence, v, viewModel.state.charisma
                    )
                },
                label = { Text("Мудрость") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = viewModel.state.charisma.toString(),
                onValueChange = { cha ->
                    val v = cha.toIntOrNull() ?: viewModel.state.charisma
                    viewModel.setAttributeScores(
                        viewModel.state.strength, viewModel.state.dexterity, viewModel.state.constitution,
                        viewModel.state.intelligence, viewModel.state.wisdom, v
                    )
                },
                label = { Text("Харизма") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Далее")
        }
    }
}
