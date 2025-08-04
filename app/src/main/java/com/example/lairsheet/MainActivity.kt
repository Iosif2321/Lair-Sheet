package com.example.lairsheet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lairsheet.data.Ruleset
import com.example.lairsheet.ui.theme.CharacterCreationScreen
import com.example.lairsheet.ui.theme.LairSheetTheme
import com.example.lairsheet.ui.theme.MainScreen
import com.example.lairsheet.ui.theme.SplashScreen
import com.example.lairsheet.ui.theme.AuthorsScreen
import kotlinx.coroutines.delay

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
                    delay(2000)
                    showSplash = false
                }
                if (showSplash) {
                    SplashScreen()
                } else {
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
                }
            }
        }
    }
}

private sealed class Screen {
    data object Main : Screen()
    data object Create : Screen()
    data object Authors : Screen()
}
