package com.example.lairsheet.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lairsheet.CharacterViewModel
import com.example.lairsheet.data.AbilityScore
import com.example.lairsheet.data.AbilityType
import com.example.lairsheet.data.Character
import com.example.lairsheet.data.DndDescriptions
import com.example.lairsheet.data.Ruleset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(
    id: Long,
    vm: CharacterViewModel,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit = {},
    onDelete: (Long) -> Unit
) {
    var loading by remember { mutableStateOf(true) }
    var character by remember { mutableStateOf<Character?>(null) }
    var showDelete by remember { mutableStateOf(false) }

    LaunchedEffect(id) {
        character = vm.getCharacterById(id)
        loading = false
    }

    if (showDelete) {
        ConfirmDeleteDialog(
            onDismiss = { showDelete = false },
            onConfirm = {
                showDelete = false
                onDelete(id)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = character?.name ?: "Персонаж")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(id) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDelete = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator()
                    }
                }
                character == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Персонаж не найден")
                    }
                }
                else -> {
                    CharacterDetailContent(character = character!!)
                }
            }
        }
    }
}

@Composable
private fun CharacterDetailContent(character: Character) {
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Основная карточка
        Card(
            colors = CardDefaults.cardColors(containerColor = LightPink),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = character.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = DeepRed,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = buildString {
                        append("Раса: ")
                        append(character.race.nameRu)
                        character.subrace?.let { append(" (${it.nameRu})") }
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = buildString {
                        append("Класс: ")
                        append(character.characterClass.name)
                        character.subclass?.let { append(" (${it.name})") }
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                RuleChip(character.ruleset)
            }
        }

        // Описания (раса/класс)
        DescriptionsCard(character)

        // Характеристики
        AbilityScoresCard(character.abilityScores)

        // Метаданные
        MetaCard(
            step = character.creationStep,
            createdAt = character.createdAt,
            updatedAt = character.updatedAt
        )

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun RuleChip(ruleset: Ruleset) {
    val txt = when (ruleset) {
        Ruleset.R5E_2014 -> "D&D 5e (2014)"
        Ruleset.R5E_2024 -> "D&D 5e (2024)"
        Ruleset.EXPANDED -> "Expanded"
    }
    Text(
        text = txt,
        style = MaterialTheme.typography.labelMedium,
        color = DeepRed
    )
}

@Composable
private fun DescriptionsCard(character: Character) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Описание расы (безопасно обрабатываем null/пустые)
            val raceDesc = DndDescriptions.getRaceDescription(
                character.race.nameRu,
                character.race.description ?: ""
            )
            Text("Описание расы", style = MaterialTheme.typography.titleMedium, color = DeepRed)
            Text(raceDesc, style = MaterialTheme.typography.bodyMedium)

            HorizontalDivider()

            // Описание класса (безопасно обрабатываем null/пустые)
            val classDesc = DndDescriptions.getClassDescription(
                character.characterClass.name,
                character.characterClass.system.description.value ?: ""
            )
            Text("Описание класса", style = MaterialTheme.typography.titleMedium, color = DeepRed)
            Text(classDesc, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun AbilityScoresCard(scores: Map<AbilityType, AbilityScore>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Характеристики", style = MaterialTheme.typography.titleMedium, color = DeepRed)
            Spacer(Modifier.height(8.dp))
            // Вывод по двум колонкам
            val ordered = listOf(
                AbilityType.STRENGTH,
                AbilityType.DEXTERITY,
                AbilityType.CONSTITUTION,
                AbilityType.INTELLIGENCE,
                AbilityType.WISDOM,
                AbilityType.CHARISMA
            )
            for (i in ordered.chunked(2)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    i.forEach { type ->
                        val v = scores[type]?.value ?: 10
                        val mod = modOf(v)
                        Column(Modifier.weight(1f)) {
                            Text(typeLabel(type), fontWeight = FontWeight.SemiBold)
                            Text("$v (${modSign(mod)})")
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

private fun typeLabel(t: AbilityType): String = when (t) {
    AbilityType.STRENGTH -> "Сила (STR)"
    AbilityType.DEXTERITY -> "Ловкость (DEX)"
    AbilityType.CONSTITUTION -> "Телосложение (CON)"
    AbilityType.INTELLIGENCE -> "Интеллект (INT)"
    AbilityType.WISDOM -> "Мудрость (WIS)"
    AbilityType.CHARISMA -> "Харизма (CHA)"
}

private fun modOf(score: Int): Int = kotlin.math.floor((score - 10) / 2.0).toInt()
private fun modSign(m: Int): String = if (m >= 0) "+$m" else "$m"

@Composable
private fun MetaCard(step: Int, createdAt: Long, updatedAt: Long) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Метаданные", style = MaterialTheme.typography.titleMedium, color = DeepRed)
            Text("Шаг создания: $step", style = MaterialTheme.typography.bodyMedium)
            Text("Создан: ${formatTs(createdAt)}", style = MaterialTheme.typography.bodyMedium)
            Text("Обновлён: ${formatTs(updatedAt)}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun formatTs(ts: Long): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(ts))
}

@Composable
private fun ConfirmDeleteDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Удалить персонажа?") },
        text = { Text("Это действие нельзя отменить.") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Удалить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}