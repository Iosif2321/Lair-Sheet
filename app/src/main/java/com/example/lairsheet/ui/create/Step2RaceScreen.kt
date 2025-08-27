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

@Composable
fun Step2RaceScreen(
    state: CharacterCreationState,
    onRaceSelected: (Race) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val races = remember(state.selectedSources) {
        loadRacesFromSources(LocalContext.current, state.selectedSources)
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
                    RaceCard(
                        race = race,
                        selected = state.selectedRace?.nameRu == race.nameRu,
                        onClick = { onRaceSelected(race) }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun RaceCard(race: Race, selected: Boolean, onClick: () -> Unit) {
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
        }
    }
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
    Ruleset.PHB2014 -> "Основные правила 2014"
    Ruleset.PHB2024 -> "Основные правила 2024"
    Ruleset.EXPANDED -> "Расширенные источники"
}

private fun loadRacesFromSources(
    context: Context,
    sources: Set<Ruleset>
): List<Race> {
    val json = JSONObject(readRaw(context, R.raw.dnd_sources))
    val result = mutableListOf<Race>()
    sources.forEach { ruleset ->
        json.optJSONObject(rulesetKey(ruleset))?.let { block ->
            result += block.extractRaces()
        }
    }
    return result.distinctBy { it.nameRu }
}
