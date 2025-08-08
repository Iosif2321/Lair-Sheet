package com.example.lairsheet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lairsheet.data.Ruleset
import com.example.lairsheet.ui.theme.CharacterCreationScreen
import com.example.lairsheet.ui.theme.LairSheetTheme
import com.example.lairsheet.ui.theme.MainScreen
import com.example.lairsheet.ui.theme.SplashScreen
import com.example.lairsheet.ui.theme.AuthorsScreen
import com.example.lairsheet.ui.theme.DiceMenu
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Main entry point for the Lair Sheet application.  This activity hosts the
 * various screens and overlays a floating dice roller that is available on
 * every page once the splash screen has completed.  Rolls are displayed
 * through a Snackbar for quick feedback.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LairSheetTheme {
                var showSplash by remember { mutableStateOf(true) }
                val vm: CharacterViewModel = viewModel()
                var ruleset by remember { mutableStateOf(Ruleset.R5E_2014) }
                val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                    uri?.let { vm.importCharacter(it, ruleset) }
                }
                val folderLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) {}
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Main) }
                val characters by vm.characters(ruleset).collectAsState(emptyList())
                LaunchedEffect(Unit) {
                    // Delay briefly to show the splash screen
                    delay(2000)
                    showSplash = false
                }
                if (showSplash) {
                    SplashScreen()
                } else {
                    // State controlling whether the dice menu is expanded
                    var diceExpanded by remember { mutableStateOf(false) }
                    // Snackbar host to display dice roll results
                    val snackbarHostState = remember { SnackbarHostState() }
                    val coroutineScope = rememberCoroutineScope()

                    Box(modifier = Modifier.fillMaxSize()) {
                        when (currentScreen) {
                            Screen.Main -> MainScreen(
                                ruleset = ruleset,
                                characters = characters,
                                onRulesetChange = { ruleset = it },
                                onCreateCharacter = { currentScreen = Screen.Create },
                                onImportJson = { importLauncher.launch(arrayOf("application/json")) },
                                onOpenDataFolder = { folderLauncher.launch(null) },
                                onShowAuthors = { currentScreen = Screen.Authors }
                            )
                            Screen.Create -> CharacterCreationScreen(
                                ruleset = ruleset,
                                onSave = {
                                    vm.addCharacter(it)
                                    currentScreen = Screen.Main
                                },
                                onCancel = { currentScreen = Screen.Main }
                            )
                            Screen.Authors -> AuthorsScreen(onBack = { currentScreen = Screen.Main })
                        }
                        // Overlay the dice roller menu on every screen
                        DiceMenu(
                            expanded = diceExpanded,
                            onToggle = { diceExpanded = !diceExpanded },
                            onRoll = { sides ->
                                val result = Random.nextInt(1, sides + 1)
                                coroutineScope.launch {
                                    // Use normal string interpolation so the number of sides and result appear correctly
                                    snackbarHostState.showSnackbar("Результат d${sides}: ${result}")
                                }
                            },
                            modifier = Modifier
                        )
                        // Snackbar host anchored to bottom centre to display roll results
                        SnackbarHost(
                            hostState = snackbarHostState,
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Represents the different high‑level screens of the application.  Sealed to
 * ensure exhaustive when statements.
 */
private sealed class Screen {
    data object Main : Screen()
    data object Create : Screen()
    data object Authors : Screen()
}