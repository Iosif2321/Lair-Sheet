package com.example.lairsheet.ui.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.lairsheet.data.dnd.*

@Composable
fun EquipmentScreen(
    viewModel: CharacterViewModel,
    onNext: () -> Unit
) {
    val ctx = LocalContext.current

    val is2024 = remember(viewModel.state.selectedSources) {
        viewModel.state.selectedSources.contains("Основные правила 2024")
    }
    val repo2024 = remember { DndRepository2024(ctx) }
    val repo2014 = remember { DndRepository2014(ctx) }
    val repo = if (is2024) repo2024 else repo2014

    val className = viewModel.state.selectedClass

    var starting by remember { mutableStateOf<StartingEquipment?>(null) }
    var itemMap by remember { mutableStateOf<Map<String, Item>>(emptyMap()) }

    LaunchedEffect(className, is2024) {
        if (className.isNotBlank()) {
            val classes = if (is2024) repo2024.getClasses() else repo2014.getClasses()
            val clazz = classes.find { it.name == className || it.type.equals(className, ignoreCase = true) }
            starting = clazz?.let { if (is2024) repo2024.getStartingEquipment(it) else repo2014.getStartingEquipment(it) }
            val keys = collectKeys(starting)
            val items = if (is2024) repo2024.getItems(keys) else repo2014.getItems(keys)
            itemMap = items.associateBy { it.key }
        } else {
            starting = null
            itemMap = emptyMap()
        }
    }

    val picks = remember { mutableStateMapOf<String, MutableSet<Int>>() }

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

    val ready = starting?.choices?.all { ch -> (picks[ch.id]?.size ?: 0) >= ch.pick } == true

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Шаг 6: Стартовое снаряжение", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        when {
            className.isBlank() -> {
                Text("Сначала выберите класс", style = MaterialTheme.typography.bodyMedium)
            }
            starting == null -> {
                Text("Нет данных по стартовому снаряжению для: $className", style = MaterialTheme.typography.bodyMedium)
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
                        val checked = idx in sel
                        val disabled = !checked && sel.size >= ch.pick
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .toggleable(
                                    value = checked,
                                    enabled = !disabled,
                                    onValueChange = {
                                        if (it) sel.add(idx) else sel.remove(idx)
                                        picks[ch.id] = sel
                                    }
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { on ->
                                    if (on && !disabled) sel.add(idx) else sel.remove(idx)
                                    picks[ch.id] = sel
                                },
                                enabled = !disabled
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
        Button(
            onClick = {
                val names = chosen.map { (k, q) ->
                    val it = itemMap[k]
                    val n = it?.name ?: k
                    if (q > 1) "$n ×$q" else n
                }
                viewModel.setEquipment(names)
                onNext()
            },
            enabled = starting != null && ready,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Далее")
        }
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
