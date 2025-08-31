package com.example.lairsheet.ui.create

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
import com.example.lairsheet.data.Ruleset
import com.example.lairsheet.data.Subclass
import com.example.lairsheet.data.dnd.DndClass
import com.example.lairsheet.data.dnd.DndRepository2014
import com.example.lairsheet.data.dnd.DndRepository2024
import com.example.lairsheet.data.dnd.Effects

@Composable
fun Step3ClassScreen(
    state: CharacterCreationState,
    onClassSelected: (DndClass) -> Unit,
    onSubclassSelected: (Subclass) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current

    val is2024 = remember(state.selectedSources) { Ruleset.R5E_2024 in state.selectedSources }
    val repo2024 = remember { DndRepository2024(ctx) }
    val repo2014 = remember { DndRepository2014(ctx) }

    val classes = remember(state.selectedSources) {
        if (is2024) repo2024.getClasses() else repo2014.getClasses()
    }

    val subclasses by remember(state.selectedClass, is2024) {
        mutableStateOf(
            when (val cl = state.selectedClass) {
                null -> emptyList()
                else -> {
                    if (is2024) repo2024.getSubclasses(cl.name)
                    else repo2014.getSubclasses(cl.name)
                }
            }
        )
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
                TextButton(onClick = onBack) { Text("Назад") }
                Spacer(Modifier.weight(1f))
                Button(
                    enabled = state.selectedClass != null,
                    onClick = onNext
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
            Text("Шаг 3: класс", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            if (classes.isEmpty()) {
                Text("Нет доступных классов для выбранных источников")
                return@Column
            }

            LazyColumn(Modifier.weight(1f)) {
                items(classes) { cl ->
                    val eff = if (is2024) repo2024.getClassEffects(cl, null) else repo2014.getClassEffects(cl, null)
                    val summary = effectsSummary(eff)
                    ClassCard(
                        clazz = cl,
                        selected = state.selectedClass?.type == cl.type && state.selectedClass?.name == cl.name,
                        summary = summary,
                        onClick = { onClassSelected(cl) }
                    )
                    Spacer(Modifier.height(8.dp))
                }

                if (state.selectedClass != null && subclasses.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(12.dp))
                        Text("Подкласс:", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                    }
                    items(subclasses) { sub ->
                        val selected = state.selectedSubclass?.classIdentifier == sub.classIdentifier ||
                                state.selectedSubclass?.name == sub.name
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSubclassSelected(sub) }
                                .padding(vertical = 6.dp)
                        ) {
                            RadioButton(
                                selected = selected,
                                onClick = { onSubclassSelected(sub) }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(sub.name)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassCard(clazz: DndClass, selected: Boolean, summary: String?, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = if (selected) CardDefaults.cardColors()
        else CardDefaults.cardColors()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(clazz.name, style = MaterialTheme.typography.titleMedium)
            val desc = clazz.system.description.value
            if (desc.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(desc, style = MaterialTheme.typography.bodyMedium)
            }
            if (!summary.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(summary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

/* ---------- Краткая сводка эффектов ---------- */

private fun effectsSummary(e: Effects?): String {
    if (e == null) return ""
    val parts = mutableListOf<String>()

    e.abilityBonuses?.takeIf { it.isNotEmpty() }?.let { ab ->
        parts += "Характеристики: " + ab.entries.joinToString { (k, v) -> "$k+$v" }
    }

    e.proficiencies?.let { p ->
        val w = p.weapons?.takeIf { it.isNotEmpty() }?.joinToString()
        val a = p.armor?.takeIf { it.isNotEmpty() }?.joinToString()
        val s = p.savingThrows?.takeIf { it.isNotEmpty() }?.joinToString()
        val sk = p.skills?.takeIf { it.isNotEmpty() }?.joinToString()
        val items = listOfNotNull(
            w?.let { "оружие($it)" },
            a?.let { "доспехи($it)" },
            s?.let { "спасброски($it)" },
            sk?.let { "навыки($it)" }
        )
        if (items.isNotEmpty()) parts += "Владения: ${items.joinToString()}"
    }

    e.senses?.let { s ->
        val senses = listOfNotNull(
            s.darkvision?.let { "Темновидение $it фт" },
            s.blindsight?.let { "Слеповидение $it фт" },
            s.tremorsense?.let { "Чувств. вибраций $it фт" },
            s.truesight?.let { "Истинное зрение $it фт" }
        )
        if (senses.isNotEmpty()) parts += senses.joinToString()
    }

    e.speed?.let { sp ->
        val speeds = listOfNotNull(
            sp.walk?.let { "Скорость $it фт" },
            sp.climb?.let { "Лазание $it фт" },
            sp.swim?.let { "Плавание $it фт" },
            sp.fly?.let { "Полет $it фт" }
        )
        if (speeds.isNotEmpty()) parts += speeds.joinToString()
    }

    e.resistances?.takeIf { it.isNotEmpty() }?.let { parts += "Сопротивления: ${it.joinToString()}" }
    e.vulnerabilities?.takeIf { it.isNotEmpty() }?.let { parts += "Уязвимости: ${it.joinToString()}" }

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

    e.size?.let { parts += "Размер: $it" }

    return parts.joinToString(" • ")
}