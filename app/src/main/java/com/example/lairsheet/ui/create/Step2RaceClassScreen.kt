package com.example.lairsheet.ui.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Button
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.lairsheet.data.Ruleset
import com.example.lairsheet.data.Subclass
import com.example.lairsheet.data.dnd.DndClass
import com.example.lairsheet.data.dnd.DndRepository2014
import com.example.lairsheet.data.dnd.DndRepository2024

@Composable
fun Step2RaceClassScreen(
    state: CharacterCreationState,
    onNameChanged: (String) -> Unit,
    onRaceSelected: (Race) -> Unit,
    onSubraceSelected: (Subrace) -> Unit,
    onClassSelected: (DndClass) -> Unit,
    onSubclassSelected: (Subclass) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    // Выбор активного набора правил:
    // Если выбран 2014 — предпочитаем его (даже если 2024 тоже отмечен), т.к. только там есть подрасы.
    val has2014 = remember(state.selectedSources) { Ruleset.R5E_2014 in state.selectedSources }
    val has2024 = remember(state.selectedSources) { Ruleset.R5E_2024 in state.selectedSources }
    // Предпочитаем 2024 как основной источник для списка рас/классов, но поддерживаем fallback подрас с 2014.
    val use2024 = has2024
    val repo2024 = remember { DndRepository2024(ctx) }
    val repo2014 = remember { DndRepository2014(ctx) }

    // Данные
    val races = remember(state.selectedSources) { if (use2024) repo2024.getRaces() else repo2014.getRaces() }
    val classes = remember(state.selectedSources) { if (use2024) repo2024.getClasses() else repo2014.getClasses() }
    val subracesRaw = remember(state.selectedRace, state.selectedSources) {
        val r = state.selectedRace ?: return@remember emptyList()
        // 1) Пытаемся взять подрасы из активного источника (2024 или 2014)
        val primary = if (use2024) repo2024.getSubraces(r) else repo2014.getSubraces(r)
        if (primary.isNotEmpty()) {
            primary
        } else {
            // 2) Fallback: если активен 2024 и подрас нет, а 2014 включён — подгружаем из 2014
            if (use2024 && has2014) repo2014.getSubraces(r) else emptyList()
        }
    }
    // Фильтрация "вариант" подрас — такие подрасы не показываем вовсе
    val subraces = remember(subracesRaw) {
        subracesRaw.filterNot { sr ->
            sr.nameRu.contains("вариант", ignoreCase = true) ||
            sr.nameEn.contains("variant", ignoreCase = true)
        }
    }
    val subclasses = remember(state.selectedClass, state.selectedSources) {
        when (val cl = state.selectedClass) {
            null -> emptyList()
            else -> if (use2024) repo2024.getSubclasses(cl.name) else repo2014.getSubclasses(cl.name)
        }
    }

    // Состояние раскрывающих списков
    var raceExpanded by remember { mutableStateOf(false) }
    var subraceExpanded by remember { mutableStateOf(false) }
    var classExpanded by remember { mutableStateOf(false) }
    var subclassExpanded by remember { mutableStateOf(false) }

    val needSubrace = subraces.isNotEmpty()
    val needSubclass = subclasses.isNotEmpty()

    val nameOk = remember(state.characterName) { state.characterName.trim().length >= 3 }

    // При блокировке по имени — закрываем раскрывашки
    LaunchedEffect(nameOk) {
        if (!nameOk) {
            raceExpanded = false
            subraceExpanded = false
            classExpanded = false
            subclassExpanded = false
        }
    }

    val ready = state.selectedRace != null &&
        (!needSubrace || state.selectedSubrace != null) &&
        state.selectedClass != null &&
        (!needSubclass || state.selectedSubclass != null) &&
        nameOk

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) { Text("Назад") }
                Spacer(Modifier.weight(1f))
                Button(onClick = onConfirm, enabled = ready) { Text("Далее") }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Создание персонажа", style = MaterialTheme.typography.headlineMedium)

            // Имя персонажа
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Имя персонажа",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                OutlinedTextField(
                    value = state.characterName,
                    onValueChange = onNameChanged,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Введите имя (не короче 3 символов)") }
                )
            }

            // РАСА
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Раса",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                OutlinedTextField(
                    value = state.selectedRace?.nameRu ?: if (nameOk) "Выберите расу" else "Введите имя персонажа",
                    onValueChange = {},
                    readOnly = true,
                    enabled = nameOk,
                    trailingIcon = {
                        IconButton(onClick = { raceExpanded = !raceExpanded }, enabled = nameOk) {
                            Icon(
                                imageVector = if (raceExpanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                SelectorList(
                    expanded = raceExpanded && nameOk,
                    items = races.map { it.nameRu },
                    modifier = Modifier.fillMaxWidth()
                ) { idx ->
                    onRaceSelected(races[idx])
                    raceExpanded = false
                    subraceExpanded = false
                }

                val raceDesc = state.selectedRace?.description.orEmpty()
                if (raceDesc.isNotBlank()) {
                    Text(
                        raceDesc,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Подраса: показываем ТОЛЬКО если у выбранной расы есть подрасы (после фильтра «вариант/variant»)
                if (needSubrace) {
                    Text(
                        "Подраса",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    OutlinedTextField(
                        value = state.selectedSubrace?.nameRu ?: "Выберите подрасу",
                        onValueChange = {},
                        readOnly = true,
                        enabled = nameOk,
                        trailingIcon = {
                            IconButton(onClick = { subraceExpanded = !subraceExpanded }, enabled = nameOk) {
                                Icon(
                                    imageVector = if (subraceExpanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    SelectorList(
                        expanded = subraceExpanded && nameOk,
                        items = subraces.map { it.nameRu },
                        modifier = Modifier.fillMaxWidth()
                    ) { idx ->
                        onSubraceSelected(subraces[idx])
                        subraceExpanded = false
                    }

                    val subDesc = state.selectedSubrace?.description.orEmpty()
                    if (subDesc.isNotBlank()) {
                        Text(
                            subDesc,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 5,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // КЛАСС
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Класс",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                OutlinedTextField(
                    value = state.selectedClass?.name ?: if (nameOk) "Выберите класс" else "Введите имя персонажа",
                    onValueChange = {},
                    readOnly = true,
                    enabled = nameOk,
                    trailingIcon = {
                        IconButton(onClick = { classExpanded = !classExpanded }, enabled = nameOk) {
                            Icon(
                                imageVector = if (classExpanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                SelectorList(
                    expanded = classExpanded && nameOk,
                    items = classes.map { it.name },
                    modifier = Modifier.fillMaxWidth()
                ) { idx ->
                    onClassSelected(classes[idx])
                    classExpanded = false
                    subclassExpanded = false
                }

                val classDesc = state.selectedClass?.system?.description?.value.orEmpty()
                if (classDesc.isNotBlank()) {
                    Text(
                        classDesc,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (needSubclass) {
                    Text(
                        "Подкласс",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    OutlinedTextField(
                        value = state.selectedSubclass?.name ?: "Выберите подкласс",
                        onValueChange = {},
                        readOnly = true,
                        enabled = nameOk,
                        trailingIcon = {
                            IconButton(onClick = { subclassExpanded = !subclassExpanded }, enabled = nameOk) {
                                Icon(
                                    imageVector = if (subclassExpanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    SelectorList(
                        expanded = subclassExpanded && nameOk,
                        items = subclasses.map { it.name },
                        modifier = Modifier.fillMaxWidth()
                    ) { idx ->
                        onSubclassSelected(subclasses[idx])
                        subclassExpanded = false
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectorList(
    expanded: Boolean,
    items: List<String>,
    modifier: Modifier = Modifier,
    onSelectIndex: (Int) -> Unit
) {
    AnimatedVisibility(visible = expanded) {
        Card(
            modifier = modifier.padding(top = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column {
                items.forEachIndexed { index, title ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectIndex(index) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(title, style = MaterialTheme.typography.bodyMedium)
                    }
                    if (index != items.lastIndex) {
                        Divider()
                    }
                }
            }
        }
    }
}