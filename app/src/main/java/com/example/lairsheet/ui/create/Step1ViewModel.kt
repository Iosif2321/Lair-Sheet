package com.example.lairsheet.ui.create

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.lairsheet.data.dnd.DndClass
import com.example.lairsheet.data.dnd.DndRepository
import com.example.lairsheet.data.dnd.DndSource
import com.example.lairsheet.data.dnd.DndSources
import com.example.lairsheet.data.dnd.Race
import com.example.lairsheet.data.dnd.Subclass
import com.example.lairsheet.data.dnd.Subrace

class Step1ViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = DndRepository(app)

    var uiState by mutableStateOf(Step1UiState())
        private set

    init {
        val all: DndSources = repo.load()
        val first: DndSource? = all.sources.firstOrNull()
        uiState = uiState.copy(
            allSources = all.sources,
            selectedSourceIndex = 0,
            availableRaces = first?.races ?: emptyList(),
            availableClasses = first?.classes ?: emptyList()
        )
    }

    fun setPlayerName(value: String) {
        uiState = uiState.copy(draft = uiState.draft.copy(playerName = value))
    }

    fun setSource(index: Int) {
        val source = uiState.allSources.getOrNull(index) ?: return
        uiState = uiState.copy(
            selectedSourceIndex = index,
            availableRaces = source.races,
            availableClasses = source.classes,
            draft = uiState.draft.copy(race = null, subrace = null, clazz = null, subclass = null)
        )
    }

    fun selectRace(race: Race) {
        uiState = uiState.copy(draft = uiState.draft.copy(race = race, subrace = null))
    }

    fun selectSubrace(subrace: Subrace?) {
        uiState = uiState.copy(draft = uiState.draft.copy(subrace = subrace))
    }

    fun selectClass(clazz: DndClass) {
        uiState = uiState.copy(draft = uiState.draft.copy(clazz = clazz, subclass = null))
    }

    fun selectSubclass(subclass: Subclass?) {
        uiState = uiState.copy(draft = uiState.draft.copy(subclass = subclass))
    }
}

data class Step1UiState(
    val allSources: List<DndSource> = emptyList(),
    val selectedSourceIndex: Int = 0,
    val availableRaces: List<Race> = emptyList(),
    val availableClasses: List<DndClass> = emptyList(),
    val draft: CharacterDraft = CharacterDraft()
) {
    val canContinue: Boolean =
        draft.playerName.isNotBlank() && draft.race != null && draft.clazz != null
}
