package com.example.lairsheet.ui.create

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.zIndex
import com.example.lairsheet.ui.theme.DiceMenu

@Composable
fun Step1Screen(
    modifier: Modifier = Modifier,
    viewModel: Step1ViewModel = viewModel(),
    onNext: (CharacterDraft) -> Unit = {},
    onRollDice: (Int) -> Unit = {}
) {
    val state = viewModel.uiState
    val fm = LocalFocusManager.current
    var diceExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Создание персонажа — Шаг 1",
            style = MaterialTheme.typography.headlineSmall
        )

        // 1) Источник правил
        LabeledDropdown(
            label = "Источник правил",
            options = state.allSources.map { it.title },
            selectedIndex = state.selectedSourceIndex,
            onSelected = { idx -> viewModel.setSource(idx) }
        )

        // 2) Имя персонажа
        OutlinedTextField(
            value = state.draft.playerName,
            onValueChange = viewModel::setPlayerName,
            label = { Text("Имя персонажа") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
        )

        // 3) Раса
        LabeledDropdown(
            label = "Раса",
            options = state.availableRaces.map { it.nameRu },
            selectedIndex = state.availableRaces.indexOf(state.draft.race),
            onSelected = { idx -> state.availableRaces.getOrNull(idx)?.let(viewModel::selectRace) }
        )

        // 3.1) Подраса (если есть)
        val currentSubraces = state.draft.race?.subraces.orEmpty()
        if (currentSubraces.isNotEmpty()) {
            LabeledDropdown(
                label = "Подраса",
                options = currentSubraces.map { it.nameRu },
                selectedIndex = currentSubraces.indexOf(state.draft.subrace),
                onSelected = { idx -> currentSubraces.getOrNull(idx)?.let(viewModel::selectSubrace) }
            )
        }

        // 4) Класс
        LabeledDropdown(
            label = "Класс",
            options = state.availableClasses.map { it.nameRu },
            selectedIndex = state.availableClasses.indexOf(state.draft.clazz),
            onSelected = { idx -> state.availableClasses.getOrNull(idx)?.let(viewModel::selectClass) }
        )

        // 4.1) Подкласс (если есть)
        val currentSubclasses = state.draft.clazz?.subclasses.orEmpty()
        if (currentSubclasses.isNotEmpty()) {
            LabeledDropdown(
                label = "Подкласс",
                options = currentSubclasses.map { it.nameRu },
                selectedIndex = currentSubclasses.indexOf(state.draft.subclass),
                onSelected = { idx -> currentSubclasses.getOrNull(idx)?.let(viewModel::selectSubclass) }
            )
        }

        // ---- Кнопка бросков кубиков — прямо над "Далее"
        Spacer(modifier = Modifier.weight(1f))

        Box(Modifier.fillMaxWidth()) {
            DiceMenu(
                expanded = diceExpanded,
                onToggle = { diceExpanded = !diceExpanded },
                onRoll = { sides -> onRollDice(sides) },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .zIndex(1f)
            )
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = {
                    fm.clearFocus()
                    onNext(state.draft)
                },
                enabled = state.canContinue
            ) {
                Text("Далее")
            }
        }
    }
}

/**
 * Надёжный дропдаун:
 *  - кликается в любом месте поля (прозрачный overlay поверх TextField)
 *  - стрелка тоже кликабельна
 *  - меню всегда поверх (zIndex)
 */
@Composable
private fun LabeledDropdown(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = options.getOrNull(selectedIndex).orEmpty()

    Box(Modifier.fillMaxWidth()) {
        var iconExpanded by remember { mutableStateOf(false) }

        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            enabled = true,
            label = { Text(label) },
            trailingIcon = {
                Text(
                    if (expanded || iconExpanded) "▲" else "▼",
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .clickable {
                            expanded = true
                            iconExpanded = true
                        }
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Прозрачная кликабельная область поверх поля — гарантирует открытие меню
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                iconExpanded = false
            },
            modifier = Modifier.zIndex(1f)
        ) {
            options.forEachIndexed { idx, text ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        expanded = false
                        onSelected(idx)
                        iconExpanded = false
                    }
                )
            }
        }
    }
}
