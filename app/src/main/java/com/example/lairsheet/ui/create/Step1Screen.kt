package com.example.lairsheet.ui.create

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lairsheet.R
import com.example.lairsheet.ui.theme.DiceMenu
import com.example.lairsheet.ui.theme.DeepRed
import com.example.lairsheet.ui.theme.LightPink

@Composable
fun Step1Screen(
    modifier: Modifier = Modifier,
    viewModel: Step1ViewModel = viewModel(),
    onNext: (CharacterDraft) -> Unit = {},
    onRollDice: (Int) -> Unit = {}
) {
    val state = viewModel.uiState
    var diceExpanded by remember { mutableStateOf(false) }

    // Корневой контейнер: контент снизу, поверх — плавающая DiceMenu
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Прокручиваем весь экран (включая развернутые списки)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StepHeader()

            // 1) Источник правил
            PrettyDropdown(
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
            PrettyDropdown(
                label = "Раса",
                options = state.availableRaces.map { it.nameRu },
                selectedIndex = state.availableRaces.indexOf(state.draft.race),
                onSelected = { idx -> state.availableRaces.getOrNull(idx)?.let(viewModel::selectRace) }
            )

            // 3.1) Подраса (если есть)
            val currentSubraces = state.draft.race?.subraces.orEmpty()
            if (currentSubraces.isNotEmpty()) {
                PrettyDropdown(
                    label = "Подраса",
                    options = currentSubraces.map { it.nameRu },
                    selectedIndex = currentSubraces.indexOf(state.draft.subrace),
                    onSelected = { idx -> currentSubraces.getOrNull(idx)?.let(viewModel::selectSubrace) }
                )
            }

            // 4) Класс
            PrettyDropdown(
                label = "Класс",
                options = state.availableClasses.map { it.nameRu },
                selectedIndex = state.availableClasses.indexOf(state.draft.clazz),
                onSelected = { idx -> state.availableClasses.getOrNull(idx)?.let(viewModel::selectClass) }
            )

            // 4.1) Подкласс (если есть)
            val currentSubclasses = state.draft.clazz?.subclasses.orEmpty()
            if (currentSubclasses.isNotEmpty()) {
                PrettyDropdown(
                    label = "Подкласс",
                    options = currentSubclasses.map { it.nameRu },
                    selectedIndex = currentSubclasses.indexOf(state.draft.subclass),
                    onSelected = { idx -> currentSubclasses.getOrNull(idx)?.let(viewModel::selectSubclass) }
                )
            }

            Spacer(Modifier.height(24.dp))

            // Кнопка "Далее" с отступом справа, чтобы не перекрывалась плавающей кнопкой
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(end = 88.dp), // место под DiceMenu
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { onNext(state.draft) },
                    enabled = state.canContinue
                ) {
                    Text("Далее")
                }
            }
        }

        // === Плавающее меню кубиков: фиксировано поверх всего ===
        DiceMenu(
            expanded = diceExpanded,
            onToggle = { diceExpanded = !diceExpanded },
            onRoll = { sides -> onRollDice(sides) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(16.dp)
                .zIndex(2f) // гарантированно выше контента
        )
    }
}

@Composable
private fun StepHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LightPink)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_dragon_logo),
            contentDescription = "Dragon Logo",
            tint = DeepRed,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = "Создание персонажа",
            style = MaterialTheme.typography.titleLarge,
            color = DeepRed
        )
    }
}

/**
 * Inline-dropdown без собственной прокрутки:
 *  • клики ловятся накладкой на всё поле
 *  • expanded сохраняется через rememberSaveable
 *  • список рисуем обычным Column (без LazyColumn), чтобы вся страница скроллилась
 *  • нижний контент раздвигается (animateContentSize у контейнера)
 */
@Composable
private fun PrettyDropdown(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val selectedText = options.getOrNull(selectedIndex).orEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        // Якорь: TextField + прозрачная накладка поверх
        Box(Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                trailingIcon = { Text(if (expanded) "▲" else "▼") },
                modifier = Modifier.fillMaxWidth()
            )

            // Прозрачная кликабельная накладка на всё поле
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { expanded = !expanded }
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    options.forEachIndexed { index, text ->
                        val selected = index == selectedIndex
                        val rowColor =
                            if (selected) MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.surface

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(rowColor)
                                .clickable {
                                    onSelected(index)
                                    expanded = false
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (selected)
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (index < options.lastIndex) {
                            Divider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                thickness = 1.dp
                            )
                        }
                    }
                }
            }
        }
    }
}
