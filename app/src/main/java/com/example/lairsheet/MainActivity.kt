package com.example.lairsheet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lairsheet.data.Ruleset
import com.example.lairsheet.ui.theme.AuthorsScreen
import com.example.lairsheet.ui.theme.CharacterCreationScreen
import com.example.lairsheet.ui.theme.DiceMenu
import com.example.lairsheet.ui.theme.LairSheetTheme
import com.example.lairsheet.ui.theme.MainScreen
import com.example.lairsheet.ui.theme.RollEntry
import com.example.lairsheet.ui.theme.RollFeed
import com.example.lairsheet.ui.theme.SplashScreen
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Main entry point for the Lair Sheet application.
 * Содержит навигацию по экранам и оверлеи (меню кубиков и ленту результатов бросков).
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

                // Небольшая задержка для Splash
                LaunchedEffect(Unit) {
                    delay(2000)
                    showSplash = false
                }

                if (showSplash) {
                    SplashScreen()
                } else {
                    // Состояние меню кубиков
                    var diceExpanded by remember { mutableStateOf(false) }

                    // Лента результатов бросков (новые элементы добавляются ВНИЗ списка)
                    val rollFeed = remember { mutableStateListOf<RollEntry>() }

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

                        // Плавающее меню кубиков (иконка d8 + панель d4/d6/d8/d10/d12/d20)
                        DiceMenu(
                            expanded = diceExpanded,
                            onToggle = { diceExpanded = !diceExpanded },
                            onRoll = { sides ->
                                val value = Random.nextInt(1, sides + 1)
                                // Новый элемент добавляем в конец — новый отрисуется ниже предыдущих
                                rollFeed.add(
                                    RollEntry(
                                        id = System.nanoTime(),
                                        sides = sides,
                                        value = value
                                    )
                                )
                                // Если нужно ограничить длину ленты, раскомментируй:
                                // if (rollFeed.size > 4) rollFeed.removeAt(0)
                            },
                            modifier = Modifier
                        )

                        // Лента “тостов” с результатами (свайп для удаления, авто-скрытие 1.5 c)
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

/**
 * Верхнеуровневые экраны приложения.
 */
private sealed class Screen {
    data object Main : Screen()
    data object Create : Screen()
    data object Authors : Screen()
}
