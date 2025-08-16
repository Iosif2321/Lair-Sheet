package com.example.lairsheet.ui.create

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import android.os.Bundle
import androidx.core.os.bundleOf

/**
 * Корневой навигатор мастера.
 * Держит общее состояние и передаёт его на экраны.
 */
@Composable
fun CharacterCreatorNav() {
    val nav = rememberNavController()

    // Простое rememberSaveable через кастомный Saver.
    var state by rememberSaveable(
        stateSaver = Saver(
            save = { s: CharacterCreationState ->
                bundleOf(
                    "sources" to s.selectedSources.map { it.name }.toTypedArray(),
                    "race_ru" to s.selectedRace?.nameRu,
                    "race_en" to s.selectedRace?.nameEn
                )
            },
            restore = { b: Bundle ->
                val sources = (b.getStringArray("sources") ?: emptyArray())
                    .map { Ruleset.valueOf(it) }
                    .toSet()
                CharacterCreationState(selectedSources = sources)
            }
        )
    ) { mutableStateOf(CharacterCreationState()) }

    NavHost(navController = nav, startDestination = "sources") {
        composable("sources") {
            Step1SourcesScreen(
                state = state,
                onToggle = { ruleset, checked ->
                    val m = state.selectedSources.toMutableSet()
                    if (checked) m.add(ruleset) else m.remove(ruleset)
                    state = state.copy(selectedSources = m)
                },
                onNext = { nav.navigate("race") },
                onCancel = { nav.popBackStack() }
            )
        }
        composable("race") {
            Step2RaceScreen(
                state = state,
                onRaceSelected = { race -> state = state.copy(selectedRace = race) },
                onNext = { /* nav.navigate("class") */ },
                onBack = { nav.popBackStack() }
            )
        }
    }
}
