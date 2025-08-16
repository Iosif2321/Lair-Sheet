package com.example.lairsheet.ui.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SourcesScreen(
    viewModel: CharacterViewModel,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Шаг 1: Выберите источники и введите имя персонажа")
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = viewModel.state.name,
            onValueChange = { viewModel.setName(it) },
            label = { Text("Имя персонажа") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Источники правил:")
        viewModel.getSources().forEach { source ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = viewModel.state.selectedSources.contains(source),
                        onValueChange = { viewModel.toggleSource(source) }
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = viewModel.state.selectedSources.contains(source),
                    onCheckedChange = { viewModel.toggleSource(source) }
                )
                Spacer(Modifier.width(8.dp))
                Text(text = source)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onNext,
            enabled = viewModel.state.name.isNotBlank() && viewModel.state.selectedSources.isNotEmpty(),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Далее")
        }
    }
}
