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
import android.app.Application
import com.example.lairsheet.data.Ruleset
import com.example.lairsheet.ui.create.Step1Screen
import com.example.lairsheet.ui.create.Step2RaceScreen
import com.example.lairsheet.ui.create.Step2MethodScreen
import com.example.lairsheet.ui.create.StandardArrayScreen
import com.example.lairsheet.ui.create.PointBuyScreen
import com.example.lairsheet.ui.create.RollScreen
import com.example.lairsheet.ui.create.CustomAttributesScreen
import com.example.lairsheet.ui.create.CharacterDraft
import com.example.lairsheet.ui.create.CharacterCreationState
import com.example.lairsheet.ui.create.AbilityMethod
import com.example.lairsheet.ui.theme.AuthorsScreen
import com.example.lairsheet.ui.theme.DiceMenu
import com.example.lairsheet.ui.theme.LairSheetTheme
import com.example.lairsheet.ui.theme.MainScreen
import com.example.lairsheet.ui.create.CharacterCreatorNav
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

                // Храним черновик персонажа для передачи между шагами
                var characterDraft by remember { mutableStateOf(CharacterDraft()) }
                // ID персонажа для редактирования (передаём на Step1Screen)
                var editCharacterId by remember { mutableStateOf<Long?>(null) }

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
                                // Сначала закрываем меню кубиков, если открыто
                                if (diceExpanded) {
                                    diceExpanded = false
                                    return@BackHandler
                                }
                                // Двойное "Назад" для выхода
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
                            Screen.CreateStep2, Screen.Step2Method, Screen.StandardArray, Screen.PointBuy, Screen.Roll, Screen.CustomAttributes -> {
                                // С шага 2 и его подэкранов возвращаемся на шаг 1
                                currentScreen = Screen.CreateStep1
                            }
                            else -> {
                                // На других внутренних экранах — вернуться на главный
                                currentScreen = Screen.Main
                            }
                        }
                    }
                    // === END BACK ===

                    Box(modifier = Modifier.fillMaxSize()) {
                        when (val scr = currentScreen) {
                            Screen.Main -> MainScreen(
                                ruleset = ruleset,
                                characters = characters,
                                onRulesetChange = { ruleset = it },
                                onCreateCharacter = { currentScreen = Screen.NewCreator },
                                onImportJson = { importLauncher.launch(arrayOf("application/json")) },
                                onOpenDataFolder = { folderLauncher.launch(null) },
                                onValidateData = {
                                    val reports = com.example.lairsheet.data.dnd.DndDataValidator.validateAll(this@MainActivity)
                                    val ok = reports.all { it.ok }
                                    Toast.makeText(
                                        this@MainActivity,
                                        if (ok) "Данные валидны" else "Найдены проблемы — см. Logcat",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onShowAuthors = { currentScreen = Screen.Authors },
                                onOpenCharacter = { c -> currentScreen = Screen.Detail(c.id) }
                            )

                            Screen.CreateStep1 -> Step1Screen(
                                characterIdToEdit = editCharacterId,
                                onNext = { draft ->
                                    characterDraft = draft
                                    // после входа в шаг редактирования можно сбросить id
                                    editCharacterId = null
                                    currentScreen = Screen.Step2Method
                                },
                                onRollDice = { sides ->
                                    val value = Random.nextInt(1, sides + 1)
                                    rollFeed.add(
                                        RollEntry(
                                            id = System.nanoTime(),
                                            sides = sides,
                                            value = value
                                        )
                                    )
                                }
                            )

                            Screen.Step2Method -> Step2MethodScreen(
                                onNext = { method ->
                                    // Выбираем метод и переходим на соответствующий экран
                                    when (method) {
                                        AbilityMethod.STANDARD_ARRAY -> currentScreen = Screen.StandardArray
                                        AbilityMethod.POINT_BUY -> currentScreen = Screen.PointBuy
                                        AbilityMethod.ROLL -> currentScreen = Screen.Roll
                                        AbilityMethod.CUSTOM -> currentScreen = Screen.CustomAttributes
                                    }
                                },
                                onBack = { currentScreen = Screen.CreateStep1 }
                            )

                            Screen.StandardArray -> StandardArrayScreen(
                                onConfirm = { abilityScores ->
                                    // Сохраняем в draft
                                    val str = abilityScores[com.example.lairsheet.data.AbilityType.STRENGTH]?.value ?: 8
                                    val dex = abilityScores[com.example.lairsheet.data.AbilityType.DEXTERITY]?.value ?: 8
                                    val con = abilityScores[com.example.lairsheet.data.AbilityType.CONSTITUTION]?.value ?: 8
                                    val intV = abilityScores[com.example.lairsheet.data.AbilityType.INTELLIGENCE]?.value ?: 8
                                    val wis = abilityScores[com.example.lairsheet.data.AbilityType.WISDOM]?.value ?: 8
                                    val cha = abilityScores[com.example.lairsheet.data.AbilityType.CHARISMA]?.value ?: 8
                                    characterDraft = characterDraft.copy(
                                        abilityScoreState = com.example.lairsheet.data.AbilityScoreState(
                                            strength = str,
                                            dexterity = dex,
                                            constitution = con,
                                            intelligence = intV,
                                            wisdom = wis,
                                            charisma = cha
                                        )
                                    )
                                    currentScreen = Screen.Main
                                },
                                onBack = { currentScreen = Screen.Step2Method }
                            )

                            Screen.PointBuy -> PointBuyScreen(
                                onConfirm = { abilityScores ->
                                    val str = abilityScores[com.example.lairsheet.data.AbilityType.STRENGTH]?.value ?: 8
                                    val dex = abilityScores[com.example.lairsheet.data.AbilityType.DEXTERITY]?.value ?: 8
                                    val con = abilityScores[com.example.lairsheet.data.AbilityType.CONSTITUTION]?.value ?: 8
                                    val intV = abilityScores[com.example.lairsheet.data.AbilityType.INTELLIGENCE]?.value ?: 8
                                    val wis = abilityScores[com.example.lairsheet.data.AbilityType.WISDOM]?.value ?: 8
                                    val cha = abilityScores[com.example.lairsheet.data.AbilityType.CHARISMA]?.value ?: 8
                                    characterDraft = characterDraft.copy(
                                        abilityScoreState = com.example.lairsheet.data.AbilityScoreState(
                                            strength = str,
                                            dexterity = dex,
                                            constitution = con,
                                            intelligence = intV,
                                            wisdom = wis,
                                            charisma = cha
                                        )
                                    )
                                    currentScreen = Screen.Main
                                },
                                onBack = { currentScreen = Screen.Step2Method }
                            )

                            Screen.Roll -> RollScreen(
                                onConfirm = { abilityScores ->
                                    val str = abilityScores[com.example.lairsheet.data.AbilityType.STRENGTH]?.value ?: 8
                                    val dex = abilityScores[com.example.lairsheet.data.AbilityType.DEXTERITY]?.value ?: 8
                                    val con = abilityScores[com.example.lairsheet.data.AbilityType.CONSTITUTION]?.value ?: 8
                                    val intV = abilityScores[com.example.lairsheet.data.AbilityType.INTELLIGENCE]?.value ?: 8
                                    val wis = abilityScores[com.example.lairsheet.data.AbilityType.WISDOM]?.value ?: 8
                                    val cha = abilityScores[com.example.lairsheet.data.AbilityType.CHARISMA]?.value ?: 8
                                    characterDraft = characterDraft.copy(
                                        abilityScoreState = com.example.lairsheet.data.AbilityScoreState(
                                            strength = str,
                                            dexterity = dex,
                                            constitution = con,
                                            intelligence = intV,
                                            wisdom = wis,
                                            charisma = cha
                                        )
                                    )
                                    currentScreen = Screen.Main
                                },
                                onBack = { currentScreen = Screen.Step2Method }
                            )

                            Screen.CustomAttributes -> CustomAttributesScreen(
                                onConfirm = { abilityScores ->
                                    val str = abilityScores[com.example.lairsheet.data.AbilityType.STRENGTH]?.value ?: 8
                                    val dex = abilityScores[com.example.lairsheet.data.AbilityType.DEXTERITY]?.value ?: 8
                                    val con = abilityScores[com.example.lairsheet.data.AbilityType.CONSTITUTION]?.value ?: 8
                                    val intV = abilityScores[com.example.lairsheet.data.AbilityType.INTELLIGENCE]?.value ?: 8
                                    val wis = abilityScores[com.example.lairsheet.data.AbilityType.WISDOM]?.value ?: 8
                                    val cha = abilityScores[com.example.lairsheet.data.AbilityType.CHARISMA]?.value ?: 8
                                    characterDraft = characterDraft.copy(
                                        abilityScoreState = com.example.lairsheet.data.AbilityScoreState(
                                            strength = str,
                                            dexterity = dex,
                                            constitution = con,
                                            intelligence = intV,
                                            wisdom = wis,
                                            charisma = cha
                                        )
                                    )
                                    currentScreen = Screen.Main
                                },
                                onBack = { currentScreen = Screen.Step2Method }
                            )


                            is Screen.Detail -> com.example.lairsheet.ui.theme.CharacterDetailScreen(
                                id = scr.id,
                                vm = vm,
                                onBack = { currentScreen = Screen.Main },
                                onEdit = { id ->
                                    editCharacterId = id
                                    currentScreen = Screen.CreateStep1
                                },
                                onDelete = { id ->
                                    vm.deleteCharacterById(id)
                                    currentScreen = Screen.Main
                                }
                            )

                            Screen.Authors -> AuthorsScreen(onBack = { currentScreen = Screen.Main })
                            Screen.NewCreator -> CharacterCreatorNav(
                                onClose = { currentScreen = Screen.Main }
                            )
                            else -> { }
                        }

                        if (currentScreen != Screen.CreateStep1 && currentScreen != Screen.Step2Method && currentScreen != Screen.StandardArray && currentScreen != Screen.PointBuy && currentScreen != Screen.Roll && currentScreen != Screen.CustomAttributes) {
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
    data object NewCreator : Screen()
    data object CreateStep1 : Screen()
    data object CreateStep2 : Screen()
    data object Authors : Screen()
    data class Detail(val id: Long) : Screen()
    data object Step2Method : Screen()
    data object StandardArray : Screen()
    data object PointBuy : Screen()
    data object Roll : Screen()
    data object CustomAttributes : Screen()
}
