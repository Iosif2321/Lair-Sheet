package com.example.lairsheet.ui.create

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.lairsheet.data.dnd.DndRepository2024
import com.example.lairsheet.data.DndDescriptions
import com.example.lairsheet.ui.create.Race
import com.example.lairsheet.ui.create.Subrace
import com.example.lairsheet.data.dnd.DndClass
import com.example.lairsheet.data.Subclass
import com.example.lairsheet.data.Character
import com.example.lairsheet.data.CharacterRepository
import com.example.lairsheet.data.AppDatabase
import com.example.lairsheet.data.Ruleset
import kotlinx.coroutines.launch

class Step1ViewModel(app: Application) : AndroidViewModel(app) {

    private val repo2024 = DndRepository2024(app)
    private val characterRepository = CharacterRepository(AppDatabase.getDatabase(app).characterDao())

    // ID текущего редактируемого персонажа (null для нового)
    var currentCharacterId: Long? = null
        private set

    var uiState by mutableStateOf(Step1UiState())
        private set

    init {
        loadData()
        // Синхронизируем описания с глобальной настройкой
        updateDescriptionsFromSettings()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                // Загружаем расы
                val races = repo2024.getRaces()
                // Загружаем классы
                val classes = repo2024.getClasses()

                uiState = uiState.copy(
                    availableRaces = races,
                    availableClasses = classes,
                    isLoading = false
                )
            } catch (e: Exception) {
                // В случае ошибки оставляем пустые списки
                uiState = uiState.copy(isLoading = false)
            }
        }
    }

    fun updateDescriptionsFromSettings() {
        // Обновляем текущие описания в соответствии с глобальной настройкой
        val currentRace = uiState.draft.race
        val currentClass = uiState.draft.clazz
        val currentSubrace = uiState.draft.subrace
        val currentSubclass = uiState.draft.subclass

        var newState = uiState

        if (currentRace != null) {
            newState = newState.copy(
                selectedRaceDescription = DndDescriptions.getRaceDescription(currentRace.nameRu, currentRace.description)
            )
        }

        if (currentClass != null) {
            newState = newState.copy(
                selectedClassDescription = DndDescriptions.getClassDescription(currentClass.name, currentClass.system.description.value)
            )
        }

        if (currentSubrace != null) {
            newState = newState.copy(
                selectedSubraceDescription = DndDescriptions.getSubraceDescription(currentSubrace.nameRu, "Описание подрасы ${currentSubrace.nameRu}")
            )
        }

        if (currentSubclass != null) {
            newState = newState.copy(
                selectedSubclassDescription = DndDescriptions.getSubclassDescription(currentSubclass.name, "Описание подкласса ${currentSubclass.name}")
            )
        }

        uiState = newState
    }

    fun setPlayerName(value: String) {
        uiState = uiState.copy(draft = uiState.draft.copy(playerName = value))
        saveCurrentDraft()
    }

    fun selectRace(race: Race) {
        viewModelScope.launch {
            try {
                val subraces = repo2024.getSubraces(race)
                uiState = uiState.copy(
                    draft = uiState.draft.copy(race = race, subrace = null),
                    availableSubraces = subraces,
                    selectedRaceDescription = DndDescriptions.getRaceDescription(race.nameRu, race.description),
                    selectedSubraceDescription = ""
                )
                saveCurrentDraft()
            } catch (e: Exception) {
                uiState = uiState.copy(
                    draft = uiState.draft.copy(race = race, subrace = null),
                    availableSubraces = emptyList(),
                    selectedRaceDescription = DndDescriptions.getRaceDescription(race.nameRu, race.description),
                    selectedSubraceDescription = ""
                )
                saveCurrentDraft()
            }
        }
    }

    fun selectSubrace(subrace: Subrace) {
        uiState = uiState.copy(
            draft = uiState.draft.copy(subrace = subrace),
            selectedSubraceDescription = DndDescriptions.getSubraceDescription(subrace.nameRu, subrace.description)
        )
    }

    fun selectClass(clazz: DndClass) {
        viewModelScope.launch {
            try {
                val subclasses = repo2024.getSubclasses(clazz.name)
                uiState = uiState.copy(
                    draft = uiState.draft.copy(clazz = clazz, subclass = null),
                    availableSubclasses = subclasses,
                    selectedClassDescription = DndDescriptions.getClassDescription(clazz.name, clazz.system.description.value),
                    selectedSubclassDescription = ""
                )
                saveCurrentDraft()
            } catch (e: Exception) {
                uiState = uiState.copy(
                    draft = uiState.draft.copy(clazz = clazz, subclass = null),
                    availableSubclasses = emptyList(),
                    selectedClassDescription = DndDescriptions.getClassDescription(clazz.name, clazz.system.description.value),
                    selectedSubclassDescription = ""
                )
                saveCurrentDraft()
            }
        }
    }

    fun selectSubclass(subclass: Subclass) {
        uiState = uiState.copy(
            draft = uiState.draft.copy(subclass = subclass),
            selectedSubclassDescription = DndDescriptions.getSubclassDescription(subclass.name, "Описание подкласса ${subclass.name}")
        )
        saveCurrentDraft()
    }

    // Автоматическое сохранение черновика персонажа
    private fun saveCurrentDraft() {
        viewModelScope.launch {
            try {
                val draft = uiState.draft
                if (draft.playerName.isNotBlank()) {
                    val character = Character(
                        id = currentCharacterId ?: 0,
                        name = draft.playerName,
                        race = draft.race!!,
                        subrace = draft.subrace,
                        characterClass = draft.clazz!!,
                        subclass = draft.subclass,
                        abilityScores = emptyMap(), // Пока пустые, будут заполнены на следующем шаге
                        ruleset = Ruleset.R5E_2024,
                        creationStep = 1, // Шаг 1: базовая информация
                        isCompleted = false,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )

                    val characterId = characterRepository.insertCharacter(character)
                    if (currentCharacterId == null) {
                        currentCharacterId = characterId
                    }
                }
            } catch (e: Exception) {
                // Обработка ошибок сохранения
            }
        }
    }

    // Загрузка персонажа для редактирования (маппинг к текущим справочникам)
    fun loadCharacterForEditing(characterId: Long) {
        viewModelScope.launch {
            try {
                val it = characterRepository.getCharacterById(characterId) ?: return@launch
                currentCharacterId = it.id

                // Гарантируем, что справочники загружены
                if (uiState.availableRaces.isEmpty() || uiState.availableClasses.isEmpty()) {
                    try {
                        val races = repo2024.getRaces()
                        val classes = repo2024.getClasses()
                        uiState = uiState.copy(
                            availableRaces = races,
                            availableClasses = classes,
                            isLoading = false
                        )
                    } catch (e: Exception) {
                        uiState = uiState.copy(isLoading = false)
                    }
                }

                // Подбор элементов из текущих списков по имени
                val raceMapped = uiState.availableRaces.firstOrNull { r ->
                    r.nameRu.equals(it.race.nameRu, ignoreCase = true) ||
                    r.nameEn.equals(it.race.nameEn, ignoreCase = true)
                } ?: it.race

                val subraces = try {
                    repo2024.getSubraces(raceMapped)
                } catch (_: Exception) { emptyList() }

                val subraceMapped = it.subrace?.let { sr ->
                    subraces.firstOrNull { s ->
                        s.nameRu.equals(sr.nameRu, ignoreCase = true) ||
                        s.nameEn.equals(sr.nameEn, ignoreCase = true)
                    }
                }

                val classMapped = uiState.availableClasses.firstOrNull { c ->
                    c.name.equals(it.characterClass.name, ignoreCase = true)
                } ?: it.characterClass

                val subclasses = try {
                    repo2024.getSubclasses(classMapped.name)
                } catch (_: Exception) { emptyList() }

                val subclassMapped = it.subclass?.let { sc ->
                    subclasses.firstOrNull { s -> s.name.equals(sc.name, ignoreCase = true) }
                }

                // Применяем состояние единым апдейтом
                uiState = uiState.copy(
                    draft = CharacterDraft(
                        playerName = it.name,
                        race = raceMapped,
                        subrace = subraceMapped,
                        clazz = classMapped,
                        subclass = subclassMapped
                    ),
                    availableSubraces = subraces,
                    availableSubclasses = subclasses,
                    selectedRaceDescription = DndDescriptions.getRaceDescription(
                        raceMapped.nameRu, raceMapped.description
                    ),
                    selectedClassDescription = DndDescriptions.getClassDescription(
                        classMapped.name, classMapped.system.description.value
                    ),
                    selectedSubraceDescription = subraceMapped?.let { sr ->
                        DndDescriptions.getSubraceDescription(sr.nameRu, sr.description)
                    } ?: "",
                    selectedSubclassDescription = subclassMapped?.let { sc ->
                        DndDescriptions.getSubclassDescription(sc.name, "Описание подкласса ${sc.name}")
                    } ?: ""
                )
            } catch (_: Exception) {
                // ignore, чтобы не падать в UI
            }
        }
    }

    // Получение текущего персонажа для передачи на следующий шаг
    fun getCurrentCharacter(): Character? {
        val draft = uiState.draft
        return if (draft.playerName.isNotBlank() && draft.race != null && draft.clazz != null) {
            Character(
                id = currentCharacterId ?: 0,
                name = draft.playerName,
                race = draft.race!!,
                subrace = draft.subrace,
                characterClass = draft.clazz!!,
                subclass = draft.subclass,
                abilityScores = emptyMap(),
                ruleset = Ruleset.R5E_2024,
                creationStep = 1,
                isCompleted = false,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        } else null
    }
}

data class Step1UiState(
    val availableRaces: List<Race> = emptyList(),
    val availableClasses: List<DndClass> = emptyList(),
    val availableSubraces: List<Subrace> = emptyList(),
    val availableSubclasses: List<Subclass> = emptyList(),
    val draft: CharacterDraft = CharacterDraft(),
    val selectedRaceDescription: String = "",
    val selectedClassDescription: String = "",
    val selectedSubraceDescription: String = "",
    val selectedSubclassDescription: String = "",
    val isLoading: Boolean = true
) {
    val canContinue: Boolean
        get() = draft.playerName.isNotBlank() &&
                draft.race != null &&
                draft.clazz != null
}
