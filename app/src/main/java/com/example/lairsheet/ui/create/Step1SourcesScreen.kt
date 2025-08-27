package com.example.lairsheet.ui.create

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Step1SourcesScreen(
    state: CharacterCreationState,
    onToggle: (Ruleset, Boolean) -> Unit,
    onNext: () -> Unit,
    onCancel: () -> Unit
) {
    val items = listOf(
        Ruleset.PHB2014 to "Основные правила 2014",
        Ruleset.PHB2024 to "Основные правила 2024",
        Ruleset.EXPANDED to "Расширенные источники"
    )

    Scaffold(
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onCancel) { Text("Отмена") }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = onNext,
                    enabled = state.selectedSources.isNotEmpty()
                ) { Text("Далее") }
            }
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp)
        ) {
            Text("Создание персонажа", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(24.dp))
            Text("Шаг 1: источники", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            items.forEach { (ruleset, title) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = ruleset in state.selectedSources,
                        onCheckedChange = { onToggle(ruleset, it) }
                    )
                    Text(title, modifier = Modifier.padding(start = 8.dp))
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
