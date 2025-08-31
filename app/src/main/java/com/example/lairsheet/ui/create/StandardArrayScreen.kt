package com.example.lairsheet.ui.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lairsheet.data.AbilityScore
import com.example.lairsheet.data.AbilityType
import com.example.lairsheet.ui.theme.DeepRed

@Composable
fun StandardArrayScreen(
    vm: AttributesViewModel = viewModel(),
    onConfirm: (Map<AbilityType, AbilityScore>) -> Unit,
    onBack: () -> Unit
) {
    val st = vm.state

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Стандартные значения (Standard Array)",
            style = MaterialTheme.typography.titleLarge,
            color = DeepRed
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Назначьте значения из пула характеристикам. Каждый клик на значение присваивает его выбранной характеристике.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(12.dp))

        // Пул значений
        PoolChips(
            pool = st.pool,
            onAssign = { value, type -> vm.assignFromPool(type, value) }
        )
        Spacer(Modifier.height(12.dp))

        // Сетка характеристик
        AbilityGrid(
            totals = st.total,
            base = st.base,
            mode = st.method,
            onPlus = {},
            onMinus = {},
            onCustomChange = { _, _ -> }
        )
        Spacer(Modifier.height(12.dp))

        // Расовые бонусы
        RacialBonusesPanel(
            mode = st.racialMode,
            scheme = st.flexible.scheme,
            bonuses = st.flexible.bonuses,
            onToggleMode = vm::toggleRacialMode,
            onSelectScheme = vm::setFlexibleScheme,
            onChangeBonus = vm::setFlexibleBonus
        )
        Spacer(Modifier.height(12.dp))

        // Итоговые значения
        TotalsCard(st.total)

        Spacer(Modifier.height(16.dp))

        // Кнопки
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Назад") }
            ElevatedButton(
                onClick = { onConfirm(vm.toAbilityScores()) },
                enabled = st.ready,
                modifier = Modifier.weight(1f)
            ) {
                Text("Подтвердить")
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}