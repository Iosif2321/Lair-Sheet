package com.example.lairsheet

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lairsheet.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CharacterViewModel(app: Application) : AndroidViewModel(app) {

    // Репозиторий правил из assets
    private val rulesRepo: RulesRepository = AssetsRulesRepository(app)

    private val _currentRuleset = MutableStateFlow(Ruleset.R5E_2014)
    val currentRuleset: StateFlow<Ruleset> = _currentRuleset

    // Каталоги правил
    private val _races       = MutableStateFlow<List<Race>>(emptyList())
    private val _subraces    = MutableStateFlow<List<Subrace>>(emptyList())
    private val _classes     = MutableStateFlow<List<ClassDef>>(emptyList())
    private val _subclasses  = MutableStateFlow<List<Subclass>>(emptyList())
    private val _backgrounds = MutableStateFlow<List<Background>>(emptyList())

    val races: StateFlow<List<Race>> get() = _races
    val subraces: StateFlow<List<Subrace>> get() = _subraces
    val classes: StateFlow<List<ClassDef>> get() = _classes
    val subclasses: StateFlow<List<Subclass>> get() = _subclasses
    val backgrounds: StateFlow<List<Background>> get() = _backgrounds

    // Список персонажей (пока заглушка — пустой список, чтобы собрать проект;
    // когда подключишь Room, просто заполни его из DAO)
    private val _characters = MutableStateFlow<List<Character>>(emptyList())
    val characters: StateFlow<List<Character>> get() = _characters

    init {
        reloadRules()
        // TODO: загрузить персонажей из БД, если нужно:
        // viewModelScope.launch { _characters.value = dao.getAll() }
    }

    fun setRuleset(r: Ruleset) {
        if (_currentRuleset.value != r) {
            _currentRuleset.value = r
            reloadRules()
        }
    }

    private fun reloadRules() {
        viewModelScope.launch {
            val rs = _currentRuleset.value
            _races.value       = rulesRepo.getRaces(rs)
            _subraces.value    = rulesRepo.getSubraces(rs)
            _classes.value     = rulesRepo.getClasses(rs)     // уже содержит .subclasses
            _subclasses.value  = rulesRepo.getSubclasses(rs)
            _backgrounds.value = rulesRepo.getBackgrounds(rs)
        }
    }

    fun subracesFor(raceName: String): List<Subrace> =
        _subraces.value.filter { it.parent == raceName }.sortedBy { it.name }

    fun subclassesFor(className: String): List<Subclass> =
        _subclasses.value.filter { it.parent == className }.sortedBy { it.name }

    // --- форма создания и валидация ---
    data class CreateCharacterForm(
        val name: String,
        val className: String,
        val race: String,
        val level: Int,
        val background: String,
        val alignment: String,
        val str: Int, val dex: Int, val con: Int,
        val `int`: Int, val wis: Int, val cha: Int,
        val subrace: String? = null,
        val subclass: String? = null
    )

    data class FieldError(val field: String, val message: String)

    fun validate(f: CreateCharacterForm): List<FieldError> {
        val errs = mutableListOf<FieldError>()
        if (f.name.isBlank()) errs += FieldError("name", "Укажите имя")
        if (f.race.isBlank()) errs += FieldError("race", "Выберите расу")
        if (f.className.isBlank()) errs += FieldError("class", "Выберите класс")
        if (f.level !in 1..20) errs += FieldError("level", "Уровень 1..20")
        listOf("STR" to f.str, "DEX" to f.dex, "CON" to f.con, "INT" to f.`int`, "WIS" to f.wis, "CHA" to f.cha)
            .forEach { (n, v) -> if (v !in 1..30) errs += FieldError(n, "1..30") }
        if (subclassesFor(f.className).isNotEmpty() && f.subclass.isNullOrBlank())
            errs += FieldError("subclass", "Выберите подкласс")
        if (subracesFor(f.race).isNotEmpty() && f.subrace.isNullOrBlank())
            errs += FieldError("subrace", "Выберите подрасу")
        return errs
    }

    // Заглушка сохранения
    fun create(
        f: CreateCharacterForm,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // TODO: сохрани в БД, если добавишь поля subrace/subclass в сущность
                onSuccess()
            } catch (t: Throwable) {
                onError(t)
            }
        }
    }
}
