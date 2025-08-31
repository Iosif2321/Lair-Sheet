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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onRollDice: (Int) -> Unit = {},
    characterIdToEdit: Long? = null
) {
    val state = viewModel.uiState
    var diceExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(characterIdToEdit) {
        if (characterIdToEdit != null) {
            viewModel.loadCharacterForEditing(characterIdToEdit)
        }
    }

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

            // 1) Имя персонажа
            OutlinedTextField(
                value = state.draft.playerName,
                onValueChange = viewModel::setPlayerName,
                label = { Text("Имя персонажа") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )

            // 3) Раса
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                PrettyDropdown(
                    label = "Раса",
                    options = state.availableRaces.map { it.nameRu },
                    selectedIndex = state.draft.race?.let { r ->
                        state.availableRaces.indexOfFirst { it.nameRu == r.nameRu || it.nameEn == r.nameEn }
                    } ?: -1,
                    onSelected = { idx -> state.availableRaces.getOrNull(idx)?.let(viewModel::selectRace) }
                )
            }

            // Описание выбранной расы
            if (state.selectedRaceDescription.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(
                        text = state.selectedRaceDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // 3.1) Подраса (если есть)
            if (state.availableSubraces.isNotEmpty()) {
                PrettyDropdown(
                    label = "Подраса",
                    options = state.availableSubraces.map { it.nameRu },
                    selectedIndex = state.draft.subrace?.let { sr ->
                        state.availableSubraces.indexOfFirst { it.nameRu == sr.nameRu || it.nameEn == sr.nameEn }
                    } ?: -1,
                    onSelected = { idx -> state.availableSubraces.getOrNull(idx)?.let(viewModel::selectSubrace) }
                )
            }

            // Описание выбранной подрасы
            if (state.selectedSubraceDescription.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(
                        text = state.selectedSubraceDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // 4) Класс
            PrettyDropdown(
                label = "Класс",
                options = state.availableClasses.map { it.name },
                selectedIndex = state.draft.clazz?.let { c ->
                    state.availableClasses.indexOfFirst { it.name == c.name }
                } ?: -1,
                onSelected = { idx -> state.availableClasses.getOrNull(idx)?.let(viewModel::selectClass) }
            )

            // Описание выбранного класса
            if (state.selectedClassDescription.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(
                        text = state.selectedClassDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // 4.1) Подкласс (если есть)
            if (state.availableSubclasses.isNotEmpty()) {
                PrettyDropdown(
                    label = "Подкласс",
                    options = state.availableSubclasses.map { it.name },
                    selectedIndex = state.draft.subclass?.let { sc ->
                        state.availableSubclasses.indexOfFirst { it.name == sc.name }
                    } ?: -1,
                    onSelected = { idx -> state.availableSubclasses.getOrNull(idx)?.let(viewModel::selectSubclass) }
                )
            }

            // Описание выбранного подкласса
            if (state.selectedSubclassDescription.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(
                        text = state.selectedSubclassDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Добавляем дополнительное пространство для кнопки внизу
            Spacer(Modifier.height(120.dp))
        }

        // === Кнопка "Далее" внизу экрана ===
        Button(
            onClick = {
                // Обновляем черновик с текущими данными
                val updatedDraft = state.draft.copy(
                    playerName = state.draft.playerName,
                    race = state.draft.race,
                    subrace = state.draft.subrace,
                    clazz = state.draft.clazz,
                    subclass = state.draft.subclass
                )
                onNext(updatedDraft)
            },
            enabled = state.canContinue,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(0.8f)
                .padding(bottom = 16.dp)
                .navigationBarsPadding()
                .height(56.dp)
        ) {
            Text("Далее", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                            androidx.compose.material3.HorizontalDivider(
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
