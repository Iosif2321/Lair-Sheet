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
            Step2MethodScreen(
                onNext = { method ->
                    when (method) {
                        AbilityMethod.STANDARD_ARRAY -> navController.navigate("standard_array")
                        AbilityMethod.POINT_BUY -> navController.navigate("point_buy")
                        AbilityMethod.ROLL -> navController.navigate("roll")
                        AbilityMethod.CUSTOM -> navController.navigate("custom_attributes")
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("standard_array") {
            StandardArrayScreen(
                onConfirm = { _ -> navController.navigate("background") },
                onBack = { navController.navigate("attributes") }
            )
        }
        composable("point_buy") {
            PointBuyScreen(
                onConfirm = { _ -> navController.navigate("background") },
                onBack = { navController.navigate("attributes") }
            )
        }
        composable("roll") {
            RollScreen(
                onConfirm = { _ -> navController.navigate("background") },
                onBack = { navController.navigate("attributes") }
            )
        }
        composable("custom_attributes") {
            CustomAttributesScreen(
                onConfirm = { _ -> navController.navigate("background") },
                onBack = { navController.navigate("attributes") }
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
