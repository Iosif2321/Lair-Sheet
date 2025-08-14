package com.example.lairsheet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lairsheet.ui.CharacterCreationScreen
import com.example.lairsheet.ui.MainScreen
import com.example.lairsheet.ui.SplashScreen
import com.example.lairsheet.ui.theme.LairSheetTheme

class MainActivity : ComponentActivity() {

    // ✅ Дефолтная фабрика подходит для AndroidViewModel
    private val viewModel: CharacterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LairSheetTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val nav = rememberNavController()
                    NavHost(
                        navController = nav,
                        startDestination = "splash"
                    ) {
                        composable("splash") {
                            SplashScreen(onFinished = {
                                nav.navigate("home") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            })
                        }
                        composable("home") {
                            MainScreen(
                                vm = viewModel,
                                onCreateClicked = { nav.navigate("create") }
                            )
                        }
                        composable("create") {
                            CharacterCreationScreen(
                                vm = viewModel,
                                onDone = { nav.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
