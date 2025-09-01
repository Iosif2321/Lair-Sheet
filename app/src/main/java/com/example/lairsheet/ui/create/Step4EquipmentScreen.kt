package com.example.lairsheet.ui.create

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.lairsheet.data.Ruleset
import com.example.lairsheet.data.AbilityType
import com.example.lairsheet.data.dnd.*
import com.example.lairsheet.ui.create.NavBarButtons

@Composable
fun Step4EquipmentScreen(
    state: CharacterCreationState,
    onConfirm: (EquipmentSummary) -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current

    val is2024 = remember(state.selectedSources) { Ruleset.R5E_2024 in state.selectedSources }
    val repo2024 = remember { DndRepository2024(ctx) }
    val repo2014 = remember { DndRepository2014(ctx) }

    val clazz = state.selectedClass
    var starting by remember { mutableStateOf<StartingEquipment?>(null) }
    var itemMap by remember { mutableStateOf<Map<String, Item>>(emptyMap()) }

    LaunchedEffect(clazz, is2024) {
        if (clazz != null) {
            starting = if (is2024) repo2024.getStartingEquipment(clazz) else repo2014.getStartingEquipment(clazz)
            val keys = collectKeys(starting)
            val items = if (is2024) repo2024.getItems(keys) else repo2014.getItems(keys)
            itemMap = items.associateBy { it.key }
        } else {
            starting = null
            itemMap = emptyMap()
        }
    }

    // выбранные опции: choiceId -> индексы
    val picks = remember { mutableStateMapOf<String, MutableSet<Int>>() }

    // Собираем выбранные позиции
    val chosen: List<Pair<String, Int>> = remember(starting, picks, itemMap) {
        val acc = mutableListOf<Pair<String, Int>>()
        starting?.fixed?.forEach { fi -> acc += fi.key to fi.qty }
        starting?.choices?.forEach { ch ->
            val sel = picks[ch.id] ?: emptySet()
            sel.forEach { idx ->
                val opt = ch.options.getOrNull(idx)
                if (opt != null) {
                    if (opt.key != null) acc += opt.key to (opt.qty ?: 1)
                    else opt.bundle?.forEach { b -> acc += b.key to b.qty }
                }
            }
        }
        acc
    }

    val totalGp = remember(chosen, itemMap) { chosen.sumOf { (k, q) -> moneyToGp(itemMap[k]?.price) * q } }
    val totalWeight = remember(chosen, itemMap) { chosen.sumOf { (k, q) -> (itemMap[k]?.weightLb?.toDouble() ?: 0.0) * q } }

    // AC расчёт по выбранным предметам и DEX модификатору
    val dexScore = remember(state.abilityScores) { state.abilityScores[AbilityType.DEXTERITY] ?: 10 }
    val dexMod = remember(dexScore) { java.lang.Math.floorDiv(dexScore - 10, 2) }
    val grouped = remember(chosen) { chosen.groupBy { it.first }.mapValues { e -> e.value.sumOf { it.second } } }

    fun armorBaseFor(item: Item, fallback: Int): Int = item.armorClass ?: fallback

    val computedAc = remember(grouped, itemMap, dexMod) {
        var bestHeavy: Int? = null
        var bestMedium: Int? = null
        var bestLight: Int? = null
        var hasShield = false

        grouped.keys.forEach { key ->
            val it = itemMap[key] ?: return@forEach
            val cat = it.category.lowercase()
            if ("shield" in cat || it.name.contains("shield", ignoreCase = true)) {
                hasShield = true
            }
            if ("armor" in cat) {
                when {
                    "heavy" in cat -> {
                        val base = armorBaseFor(it, 16)
                        bestHeavy = listOfNotNull(bestHeavy, base).maxOrNull() ?: base
                    }
                    "medium" in cat -> {
                        val base = armorBaseFor(it, 12)
                        bestMedium = listOfNotNull(bestMedium, base).maxOrNull() ?: base
                    }
                    "light" in cat -> {
                        val base = armorBaseFor(it, 11)
                        bestLight = listOfNotNull(bestLight, base).maxOrNull() ?: base
                    }
                    else -> {
                        // если категория просто "armor", попробуем по armorClass без типа
                        val base = it.armorClass
                        if (base != null) {
                            // считаем как тяжёлая без модификатора, чтобы не занижать
                            bestHeavy = listOfNotNull(bestHeavy, base).maxOrNull() ?: base
                        }
                    }
                }
            }
        }

        var ac = when {
            bestHeavy != null -> bestHeavy!!
            bestMedium != null -> bestMedium!! + kotlin.math.min(dexMod, 2)
            bestLight != null -> bestLight!! + dexMod
            else -> 10 + dexMod // без брони
        }
        if (hasShield) ac += 2
        ac
    }

    val ready = starting?.choices?.all { ch -> (picks[ch.id]?.size ?: 0) >= ch.pick } == true

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Создание персонажа", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        Text("Шаг 4: стартовое снаряжение", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        when {
            clazz == null -> {
                Text("Сначала выберите класс на предыдущем шаге", style = MaterialTheme.typography.bodyMedium)
            }

            starting == null -> {
                Text("Нет данных стартового снаряжения для выбранного класса", style = MaterialTheme.typography.bodyMedium)
            }

            else -> {
                if (starting?.fixed?.isNotEmpty() == true) {
                    Text("Фиксированные предметы", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    starting!!.fixed.forEach { fi ->
                        val it = itemMap[fi.key]
                        val name = it?.name ?: fi.key
                        val price = formatPrice(it?.price)
                        val w = it?.weightLb?.let { v -> "${trimZero(v.toDouble())} фунт." } ?: "-"
                        Text("• $name ×${fi.qty}  |  $price  |  $w", style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(12.dp))
                    Divider()
                    Spacer(Modifier.height(12.dp))
                }

                starting!!.choices.forEach { ch ->
                    Text("Выбор: ${ch.id} (выберите ${ch.pick})", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    val sel = picks.getOrPut(ch.id) { mutableSetOf() }
                    ch.options.forEachIndexed { idx, opt ->
                        val label = optionLabel(opt, itemMap)
                        val selected = idx in sel
                        val disabled = !selected && sel.size >= ch.pick
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selected,
                                onClick = {
                                    if (selected) sel.remove(idx)
                                    else if (!disabled) sel.add(idx)
                                    picks[ch.id] = sel
                                },
                                enabled = selected || !disabled
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Divider()
                    Spacer(Modifier.height(12.dp))
                }

                Text("Итоговый список", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                val grouped = chosen.groupBy { it.first }.mapValues { e -> e.value.sumOf { it.second } }
                grouped.forEach { (k, q) ->
                    val it = itemMap[k]
                    val name = it?.name ?: k
                    val price = formatPrice(it?.price)
                    val w = it?.weightLb?.let { v -> "${trimZero(v.toDouble())} фунт." } ?: "-"
                    Text("• $name ×$q  |  $price  |  $w", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(12.dp))
                Text("Суммарная стоимость: ${trimZero(totalGp)} зм", style = MaterialTheme.typography.titleMedium)
                Text("Суммарный вес: ${trimZero(totalWeight)} фунт.", style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(Modifier.height(24.dp))
        NavBarButtons(
            onBack = onBack,
            onNext = {
                val names = chosen.map { (k, q) ->
                    val it = itemMap[k]
                    val n = it?.name ?: k
                    if (q > 1) "$n ×$q" else n
                }
                onConfirm(
                    EquipmentSummary(
                        items = names,
                        armorClass = computedAc,
                        totalWeightLb = totalWeight,
                        totalGoldGp = totalGp
                    )
                )
            },
            nextEnabled = starting != null && ready
        )
    }
}

private fun collectKeys(s: StartingEquipment?): Set<String> {
    if (s == null) return emptySet()
    val ks = mutableSetOf<String>()
    s.fixed.forEach { ks += it.key }
    s.choices.forEach { ch ->
        ch.options.forEach { o ->
            o.key?.let { ks += it }
            o.bundle?.forEach { b -> ks += b.key }
        }
    }
    return ks
}

private fun optionLabel(o: EqOption, items: Map<String, Item>): String {
    return if (o.key != null) {
        val it = items[o.key]
        val name = it?.name ?: o.key
        val price = formatPrice(it?.price)
        val w = it?.weightLb?.let { v -> "${trimZero(v.toDouble())} фунт." } ?: "-"
        "$name ×${o.qty ?: 1}  |  $price  |  $w"
    } else {
        val parts = o.bundle?.map { b ->
            val it = items[b.key]
            val name = it?.name ?: b.key
            "$name ×${b.qty}"
        }?.joinToString(" + ") ?: ""
        val gp = o.bundle?.sumOf { b -> moneyToGp(items[b.key]?.price) * b.qty } ?: 0.0
        val w = o.bundle?.sumOf { b -> (items[b.key]?.weightLb?.toDouble() ?: 0.0) * b.qty } ?: 0.0
        "$parts  |  ${trimZero(gp)} зм  |  ${trimZero(w)} фунт."
    }
}

private fun moneyToGp(m: Money?): Double {
    if (m == null) return 0.0
    val cp = (m.cp ?: 0) * 0.01
    val sp = (m.sp ?: 0) * 0.1
    val gp = (m.gp ?: 0)
    return gp + sp + cp
}

private fun formatPrice(m: Money?): String {
    val gp = moneyToGp(m)
    return "${trimZero(gp)} зм"
}

private fun trimZero(v: Double): String {
    val s = "%.2f".format(v)
    return if (s.endsWith(".00")) s.dropLast(3) else s.trimEnd('0').trimEnd('.')
}