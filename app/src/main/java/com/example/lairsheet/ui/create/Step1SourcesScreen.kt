package com.example.lairsheet.ui.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lairsheet.data.Ruleset
import com.example.lairsheet.ui.theme.DeepRed
import com.example.lairsheet.ui.theme.LightPink

@Composable
fun Step1SourcesScreen(
    state: CharacterCreationState,
    onToggle: (Ruleset, Boolean) -> Unit,
    onNext: () -> Unit,
    onCancel: () -> Unit
) {
    val items = listOf(
        Ruleset.R5E_2014 to "Основные правила 2014",
        Ruleset.R5E_2024 to "Основные правила 2024",
        Ruleset.EXPANDED to "Расширенные источники"
    )
    val selected = state.selectedSources.firstOrNull()

    Scaffold(
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, DeepRed),
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                ) {
                    Text(
                        "ОТМЕНА",
                        style = MaterialTheme.typography.titleMedium,
                        color = DeepRed
                    )
                }
                Button(
                    onClick = onNext,
                    enabled = state.selectedSources.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepRed,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                ) {
                    Text(
                        "ДАЛЕЕ",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = LightPink),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Создание персонажа",
                        style = MaterialTheme.typography.headlineMedium,
                        color = DeepRed
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Шаг 1: источники",
                        style = MaterialTheme.typography.titleLarge,
                        color = DeepRed
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
 
            // Раздельные блоки для каждого ruleset в единой палитре
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items.forEach { (ruleset, title) ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (ruleset != selected) {
                                    state.selectedSources.forEach { rs ->
                                        if (rs != ruleset) onToggle(rs, false)
                                    }
                                    onToggle(ruleset, true)
                                }
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = LightPink
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            RadioButton(
                                selected = ruleset == selected,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = DeepRed
                                )
                            )
                            Text(
                                title,
                                modifier = Modifier.padding(start = 12.dp),
                                style = MaterialTheme.typography.titleMedium,
                                color = DeepRed
                            )
                        }
                    }
                }
            }
        }
    }
}
