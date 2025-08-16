package com.example.lairsheet

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lairsheet.data.Ruleset
import com.example.lairsheet.ui.create.CharacterCreatorNav
import com.example.lairsheet.ui.theme.AuthorsScreen
import com.example.lairsheet.ui.theme.DiceMenu
import com.example.lairsheet.ui.theme.LairSheetTheme
import com.example.lairsheet.ui.theme.MainScreen
import com.example.lairsheet.ui.theme.RollEntry
import com.example.lairsheet.ui.theme.RollFeed
import com.example.lairsheet.ui.theme.SplashScreen
import kotlinx.coroutines.delay
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LairSheetTheme {
                var showSplash by remember { mutableStateOf(true) }

                // ТВОЙ БД-VM ОСТАЁТСЯ БЕЗ ИЗМЕНЕНИЙ
                val vm: CharacterViewModel = viewModel()
                var ruleset by remember { mutableStateOf(Ruleset.R5E_2014) }

                val importLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.OpenDocument()
                ) { uri -> uri?.let { vm.importCharacter(it, ruleset) } }

                val folderLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.OpenDocumentTree()
                ) {}

                var currentScreen by remember { mutableStateOf<Screen>(Screen.Main) }
                val characters by vm.characters(ruleset).collectAsState(emptyList())

                LaunchedEffect(Unit) {
                    delay(2000)
                    showSplash = false
                }

                if (showSplash) {
                    SplashScreen()
                } else {
                    // Глобальная лента результатов бросков
                    val rollFeed = remember { mutableStateListOf<RollEntry>() }
                    // Плавающее меню кубиков (прячем на экране создания)
                    var diceExpanded by remember { mutableStateOf(false) }

                    // === BACK: логика возврата/выхода ===
                    val activity = LocalContext.current as? Activity
                    var lastBackPress by remember { mutableStateOf(0L) }

                    BackHandler {
                        when (currentScreen) {
                            Screen.Main -> {
                                if (diceExpanded) {
                                    diceExpanded = false
                                    return@BackHandler
                                }
                                val now = System.currentTimeMillis()
                                if (now - lastBackPress < 2000) {
                                    activity?.finish()
                                } else {
                                    lastBackPress = now
                                    Toast.makeText(
                                        activity,
                                        "Нажмите «Назад» ещё раз для выхода",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            else -> currentScreen = Screen.Main
                        }
                    }
                    // === END BACK ===

                    Box(modifier = Modifier.fillMaxSize()) {
                        when (currentScreen) {
                            Screen.Main -> MainScreen(
                                ruleset = ruleset,
                                characters = characters,
                                onRulesetChange = { ruleset = it },
                                onCreateCharacter = { currentScreen = Screen.CreateStep1 },
                                onImportJson = { importLauncher.launch(arrayOf("application/json")) },
                                onOpenDataFolder = { folderLauncher.launch(null) },
                                onShowAuthors = { currentScreen = Screen.Authors }
                            )

                            // <<< НОВЫЙ МАСТЕР СОЗДАНИЯ ПЕРСОНАЖА >>>
                            Screen.CreateStep1 -> CharacterCreatorNav(
                                ruleset = ruleset,
                                onCancel = { currentScreen = Screen.Main },
                                onSave = { character ->
                                    vm.addCharacter(character)
                                    currentScreen = Screen.Main
                                }
                            )

                            Screen.Authors -> AuthorsScreen(onBack = { currentScreen = Screen.Main })
                        }

                        // Скрываем меню кубиков на экране мастера
                        if (currentScreen != Screen.CreateStep1) {
                            DiceMenu(
                                expanded = diceExpanded,
                                onToggle = { diceExpanded = !diceExpanded },
                                onRoll = { sides ->
                                    val value = Random.nextInt(1, sides + 1)
                                    rollFeed.add(
                                        RollEntry(
                                            id = System.nanoTime(),
                                            sides = sides,
                                            value = value
                                        )
                                    )
                                },
                                modifier = Modifier
                            )
                        }

                        RollFeed(
                            items = rollFeed,
                            onTimeout = { id -> rollFeed.removeAll { it.id == id } },
                            onDismiss = { id -> rollFeed.removeAll { it.id == id } },
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }
            }
        }
    }
}

private sealed class Screen {
    data object Main : Screen()
    data object CreateStep1 : Screen()
    data object Authors : Screen()
}
