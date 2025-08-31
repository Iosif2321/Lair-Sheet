package com.example.lairsheet.ui.create

import android.os.Bundle
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.os.bundleOf
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import com.example.lairsheet.data.AppDatabase
import com.example.lairsheet.data.Character
import com.example.lairsheet.data.CharacterRepository
import com.example.lairsheet.data.Ruleset
import com.example.lairsheet.data.AbilityScore
import com.example.lairsheet.data.AbilityType
import kotlinx.coroutines.launch

/**
 * Корневой навигатор мастера.
 * Держит общее состояние и передаёт его на экраны.
 */
@Composable
fun CharacterCreatorNav(
    onClose: () -> Unit
) {
    val nav = rememberNavController()
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { CharacterRepository(AppDatabase.getDatabase(ctx).characterDao()) }

    // Простое rememberSaveable через кастомный Saver.
    var state by rememberSaveable(
        stateSaver = Saver(
            save = { s: CharacterCreationState ->
                bundleOf(
                    "sources" to s.selectedSources.map { it.name }.toTypedArray(),
                    "race_ru" to s.selectedRace?.nameRu,
                    "race_en" to s.selectedRace?.nameEn,
                    "name" to s.characterName,
                    "charId" to s.characterId
                )
            },
            restore = { b: Bundle ->
                val sources = (b.getStringArray("sources") ?: emptyArray())
                    .map { Ruleset.valueOf(it) }
                    .toSet()
                val name = b.getString("name").orEmpty()
                val charId = b.getLong("charId", 0L).let { if (it == 0L) null else it }
                CharacterCreationState(
                    selectedSources = sources,
                    characterName = name,
                    characterId = charId
                )
            }
        )
    ) { mutableStateOf(CharacterCreationState()) }

    fun saveDraft(step: Int? = null) {
        val name = state.characterName.trim()
        val race = state.selectedRace
        val clazz = state.selectedClass
        val ruleset = state.selectedSources.firstOrNull() ?: Ruleset.R5E_2024
        if (name.length >= 3 && race != null && clazz != null) {
            val now = System.currentTimeMillis()
            val abilityMap: Map<AbilityType, AbilityScore> =
                if (state.abilityScores.isNotEmpty())
                    state.abilityScores.mapValues { (k, v) -> AbilityScore(k, v) }
                else emptyMap()
            val base = Character(
                id = state.characterId ?: 0L,
                name = name,
                race = race,
                subrace = state.selectedSubrace,
                characterClass = clazz,
                subclass = state.selectedSubclass,
                abilityMethod = state.abilityMethod?.routeKey,
                abilityScores = abilityMap,
                ruleset = ruleset,
                creationStep = step ?: 1,
                isCompleted = false,
                createdAt = now,
                updatedAt = now
            )
            scope.launch {
                val currentId = state.characterId
                if (currentId == null) {
                    val newId = repo.insertCharacter(base.copy(id = 0))
                    state = state.copy(characterId = newId)
                } else {
                    repo.updateCharacter(base.copy(id = currentId))
                }
            }
        }
    }

    NavHost(navController = nav, startDestination = "sources") {
        composable("sources") {
            Step1SourcesScreen(
                state = state,
                onToggle = { ruleset, checked ->
                    val m = state.selectedSources.toMutableSet()
                    if (checked) m.add(ruleset) else m.remove(ruleset)
                    state = state.copy(selectedSources = m)
                },
                onNext = { nav.navigate("race_class") },
                onCancel = { onClose() }
            )
        }
        composable("race_class") {
            Step2RaceClassScreen(
                state = state,
                onNameChanged = { name ->
                    state = state.copy(characterName = name)
                    saveDraft(step = 1)
                },
                onRaceSelected = { race ->
                    state = state.copy(selectedRace = race, selectedSubrace = null)
                    saveDraft(step = 1)
                },
                onSubraceSelected = { sub ->
                    state = state.copy(selectedSubrace = sub)
                    saveDraft(step = 1)
                },
                onClassSelected = { clazz ->
                    state = state.copy(selectedClass = clazz, selectedSubclass = null)
                    saveDraft(step = 1)
                },
                onSubclassSelected = { sub ->
                    state = state.copy(selectedSubclass = sub)
                    saveDraft(step = 1)
                },
                onConfirm = {
                    saveDraft(step = 1)
                    nav.navigate("attributes")
                },
                onBack = { nav.popBackStack() }
            )
        }
        composable("attributes") {
            Step2MethodScreen(
                initialMethod = state.abilityMethod,
                onNext = { method ->
                    // сохраняем выбор метода и уходим на соответствующий шаг 3
                    state = state.copy(abilityMethod = method)
                    saveDraft(step = 1)
                    val route = when (method) {
                        AbilityMethod.STANDARD_ARRAY -> "step3/abilities/standard_array"
                        AbilityMethod.POINT_BUY -> "step3/abilities/point_buy"
                        AbilityMethod.ROLL -> "step3/abilities/roll"
                        AbilityMethod.CUSTOM -> "step3/abilities/manual"
                    }
                    nav.navigate(route)
                },
                onBack = { nav.popBackStack() }
            )
        }

        // ===== Шаг 3: распределение характеристик (4 экрана) =====

        composable("step3/abilities/standard_array") {
            Step3AbilitiesStandardArrayScreen(
                initial = state,
                onBack = { nav.popBackStack() },
                onStateChanged = { ui ->
                    state = state.copy(
                        abilityMethod = ui.method,
                        abilityScores = ui.total.mapValues { it.value },
                        standardArrayPool = ui.pool,
                        abilityStepCompleted = false
                    )
                    saveDraft(step = 2)
                },
                onDone = { totals ->
                    state = state.copy(
                        abilityScores = totals,
                        abilityStepCompleted = true
                    )
                    saveDraft(step = 2)
                    nav.navigate("equipment")
                }
            )
        }

        composable("step3/abilities/point_buy") {
            Step3AbilitiesPointBuyScreen(
                initial = state,
                onBack = { nav.popBackStack() },
                onStateChanged = { ui ->
                    state = state.copy(
                        abilityMethod = ui.method,
                        abilityScores = ui.total.mapValues { it.value },
                        pointBuyRemaining = ui.remainingBudget,
                        pointBuyValues = ui.base,
                        abilityStepCompleted = false
                    )
                    saveDraft(step = 2)
                },
                onDone = { totals ->
                    state = state.copy(
                        abilityScores = totals,
                        abilityStepCompleted = true
                    )
                    saveDraft(step = 2)
                    nav.navigate("equipment")
                }
            )
        }

        composable("step3/abilities/roll") {
            Step3AbilitiesRollScreen(
                initial = state,
                onBack = { nav.popBackStack() },
                onStateChanged = { ui ->
                    state = state.copy(
                        abilityMethod = ui.method,
                        abilityScores = ui.total.mapValues { it.value },
                        // сохраняем текущий пул как снимок результатов; при назначении он должен пустеть
                        rollResults = ui.pool,
                        abilityStepCompleted = false
                    )
                    saveDraft(step = 2)
                },
                onDone = { totals ->
                    state = state.copy(
                        abilityScores = totals,
                        abilityStepCompleted = true
                    )
                    saveDraft(step = 2)
                    nav.navigate("equipment")
                }
            )
        }

        composable("step3/abilities/manual") {
            Step3AbilitiesManualScreen(
                initial = state,
                onBack = { nav.popBackStack() },
                onStateChanged = { ui ->
                    state = state.copy(
                        abilityMethod = ui.method,
                        abilityScores = ui.total.mapValues { it.value },
                        manualValues = ui.base,
                        abilityStepCompleted = false
                    )
                    saveDraft(step = 2)
                },
                onDone = { totals ->
                    state = state.copy(
                        abilityScores = totals,
                        abilityStepCompleted = true
                    )
                    saveDraft(step = 2)
                    nav.navigate("equipment")
                }
            )
        }
        composable("equipment") {
            Step4EquipmentScreen(
                state = state,
                onConfirm = { items ->
                    state = state.copy(selectedEquipment = items)
                    saveDraft(step = 3)
                    // здесь можно перейти на итоговый экран/сохранение
                    nav.popBackStack() // возвращаемся назад или добавим summary позже
                },
                onBack = { nav.popBackStack() }
            )
        }
    }
}
