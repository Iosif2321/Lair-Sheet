package com.example.lairsheet.ui.create

import android.content.Context
import androidx.annotation.RawRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import com.example.lairsheet.R
import com.example.lairsheet.data.Ruleset
import com.example.lairsheet.data.dnd.DndRepository2024
import com.example.lairsheet.data.dnd.DndRepository2014
import com.example.lairsheet.data.dnd.Effects

@Composable
fun Step2RaceScreen(
    state: CharacterCreationState,
    onRaceSelected: (Race) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val is2024 = remember(state.selectedSources) { Ruleset.R5E_2024 in state.selectedSources }
    val repo2024 = remember { DndRepository2024(context) }
    val repo2014 = remember { DndRepository2014(context) }
    val races = remember(state.selectedSources) {
        if (is2024) repo2024.getRaces()
        else loadRacesFromSources(context, state.selectedSources)
    }

    Scaffold(
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) { Text("Отмена") }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = onNext,
                    enabled = state.selectedRace != null
                ) { Text("Далее") }
            }
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp)
        ) {
            Text("Создание персонажа", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(24.dp))
            Text("Шаг 2: раса", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            if (races.isEmpty()) {
                Text("Нет доступных рас для выбранных источников")
                return@Column
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(races) { race ->
                    val eff = if (Ruleset.R5E_2024 in state.selectedSources)
                        repo2024.getRaceEffects(race, null)
                    else
                        repo2014.getRaceEffects(race, null)
                    val summary = effectsSummary(eff)

                    RaceCard(
                        race = race,
                        selected = state.selectedRace?.nameRu == race.nameRu,
                        summary = summary,
                        onClick = { onRaceSelected(race) }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun RaceCard(race: Race, selected: Boolean, summary: String?, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = if (selected) CardDefaults.cardColors()
        else CardDefaults.cardColors()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(race.nameRu, style = MaterialTheme.typography.titleMedium)
            if (race.subraces.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    race.subraces.joinToString { it.nameRu },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (!summary.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(summary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

/* ---------- Формирование краткой сводки эффектов ---------- */

private fun effectsSummary(e: Effects?): String {
    if (e == null) return ""
    val parts = mutableListOf<String>()

    // Бонусы к характеристикам
    e.abilityBonuses?.takeIf { it.isNotEmpty() }?.let { ab ->
        parts += "Характеристики: " + ab.entries.joinToString { (k, v) -> "$k+$v" }
    }

    // Чувства
    e.senses?.let { s ->
        val senses = listOfNotNull(
            s.darkvision?.let { "Темновидение $it фт" },
            s.blindsight?.let { "Слеповидение $it фт" },
            s.tremorsense?.let { "Чувств. вибраций $it фт" },
            s.truesight?.let { "Истинное зрение $it фт" }
        )
        if (senses.isNotEmpty()) parts += senses.joinToString()
    }

    // Скорости
    e.speed?.let { sp ->
        val speeds = listOfNotNull(
            sp.walk?.let { "Скорость $it фт" },
            sp.climb?.let { "Лазание $it фт" },
            sp.swim?.let { "Плавание $it фт" },
            sp.fly?.let { "Полет $it фт" }
        )
        if (speeds.isNotEmpty()) parts += speeds.joinToString()
    }

    // Сопротивления/уязвимости
    e.resistances?.takeIf { it.isNotEmpty() }?.let { parts += "Сопротивления: ${it.joinToString()}" }
    e.vulnerabilities?.takeIf { it.isNotEmpty() }?.let { parts += "Уязвимости: ${it.joinToString()}" }

    // Преимущества/помехи
    e.advantageOn?.let { adv ->
        val items = listOfNotNull(
            adv.checks?.takeIf { it.isNotEmpty() }?.let { "проверки(${it.joinToString()})" },
            adv.saves?.takeIf { it.isNotEmpty() }?.let { "спасброски(${it.joinToString()})" }
        )
        if (items.isNotEmpty()) parts += "Преимущество: ${items.joinToString()}"
    }
    e.disadvantageOn?.let { dis ->
        val items = listOfNotNull(
            dis.checks?.takeIf { it.isNotEmpty() }?.let { "проверки(${it.joinToString()})" },
            dis.saves?.takeIf { it.isNotEmpty() }?.let { "спасброски(${it.joinToString()})" }
        )
        if (items.isNotEmpty()) parts += "Помеха: ${items.joinToString()}"
    }

    return parts.joinToString(" • ")
}

/* ---------- Загрузка данных из res/raw/dnd_sources.json ---------- */

private fun readRaw(context: Context, @RawRes resId: Int): String =
    context.resources.openRawResource(resId).bufferedReader().use { it.readText() }

private fun JSONArray.toSubraces(): List<Subrace> =
    List(length()) { i ->
        val o = getJSONObject(i)
        Subrace(nameRu = o.optString("name_ru"), nameEn = o.optString("name_en"))
    }

private fun JSONObject.extractRaces(): List<Race> {
    val arr = optJSONArray("races") ?: return emptyList()
    return List(arr.length()) { i ->
        val o = arr.getJSONObject(i)
        val subs = o.optJSONArray("subraces")?.toSubraces() ?: emptyList()
        Race(
            nameRu = o.optString("name_ru"),
            nameEn = o.optString("name_en"),
            subraces = subs
        )
    }
}

private fun rulesetKey(r: Ruleset) = when (r) {
    Ruleset.R5E_2014 -> "Основные правила 2014"
    Ruleset.R5E_2024 -> "Основные правила 2024"
    Ruleset.EXPANDED -> "Расширенные источники"
}

private fun loadRacesFromSources(
    context: Context,
    sources: Set<Ruleset>
): List<Race> {
    if (Ruleset.R5E_2014 !in sources) return emptyList()
    return try {
        val json = readRaw(context, R.raw.br_2014_races)
        val arr = JSONArray(json)
        List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            val subsArr = o.optJSONArray("subraces")
            val subs = if (subsArr != null) {
                List(subsArr.length()) { j ->
                    val s = subsArr.getJSONObject(j)
                    val sName = s.optString("name", s.optString("name_ru", s.optString("name_en", "")))
                    val sKey = s.optString("key", s.optString("name_en", sName))
                    val sDesc = s.optString("description", "")
                    Subrace(nameRu = sName, nameEn = sKey, description = sDesc)
                }
            } else emptyList()
            val name = o.optString("name", o.optString("name_ru", o.optString("name_en", "")))
            val key = o.optString("key", o.optString("name_en", name))
            val desc = o.optString("description", "")
            Race(
                nameRu = name,
                nameEn = key,
                description = desc,
                subraces = subs
            )
        }
    } catch (_: Exception) {
        emptyList()
    }
}
