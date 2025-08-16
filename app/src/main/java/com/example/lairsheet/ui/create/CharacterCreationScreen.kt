package com.example.lairsheet.ui.create

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun CharacterCreationScreen() {
    val navController = rememberNavController()
    val vm: CharacterViewModel = viewModel()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = "sources") {
        composable("sources") {
            SourcesScreen(
                viewModel = vm,
                onNext = {
                    vm.loadRacesAndClasses(context)
                    navController.navigate("race")
                }
            )
        }
        composable("race") {
            RaceSelectionScreen(
                viewModel = vm,
                onNext = { navController.navigate("class") }
            )
        }
        composable("class") {
            ClassSelectionScreen(
                viewModel = vm,
                onNext = { navController.navigate("attributes") }
            )
        }
        composable("attributes") {
            AttributesScreen(
                viewModel = vm,
                onNext = { navController.navigate("background") }
            )
        }
        composable("background") {
            BackgroundScreen(
                viewModel = vm,
                onNext = { navController.navigate("equipment") }
            )
        }
        composable("equipment") {
            EquipmentScreen(
                viewModel = vm,
                onNext = { navController.navigate("summary") }
            )
        }
        composable("summary") {
            SummaryScreen(
                viewModel = vm,
                onFinish = {
                    vm.saveCharacter() // безопасная заглушка
                }
            )
        }
    }
}
