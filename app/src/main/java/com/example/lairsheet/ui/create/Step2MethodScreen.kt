package com.example.lairsheet.ui.create

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lairsheet.ui.theme.DeepRed
import com.example.lairsheet.ui.theme.LightPink

@Composable
fun Step2MethodScreen(
    initialMethod: AbilityMethod? = null,
    vm: AttributesViewModel = viewModel(),
    onNext: (AbilityMethod) -> Unit,
    onBack: () -> Unit
) {
    val st = vm.state
    var selectedMethod by remember { mutableStateOf(initialMethod ?: st.method) }

    LaunchedEffect(initialMethod) {
        initialMethod?.let { vm.selectMethod(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Шаг 2: Выбор метода распределения характеристик",
            style = MaterialTheme.typography.titleLarge,
            color = DeepRed
        )
        Spacer(Modifier.height(12.dp))

        // Описание шагов
        Text(
            "Выберите метод, по которому будут назначены значения характеристик вашего персонажа. " +
            "Каждый метод имеет свои особенности и подходит для разных стилей игры.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(16.dp))

        // Метод 1: Standard Array
        MethodCard(
            method = AbilityMethod.STANDARD_ARRAY,
            title = "Стандартные значения (Standard Array)",
            description = "Используйте фиксированный набор значений: 15, 14, 13, 12, 10, 8. " +
                "Назначьте каждое значение одной из характеристик. Подходит для сбалансированного персонажа.",
            selected = selectedMethod == AbilityMethod.STANDARD_ARRAY,
            onSelect = { selectedMethod = it }
        )

        Spacer(Modifier.height(12.dp))

        // Метод 2: Point Buy
        MethodCard(
            method = AbilityMethod.POINT_BUY,
            title = "Закупка очков (Point Buy)",
            description = "Распределите бюджет очков (по умолчанию 27) между характеристиками. " +
                "Каждое значение имеет стоимость. Позволяет точно настроить характеристики под концепцию.",
            selected = selectedMethod == AbilityMethod.POINT_BUY,
            onSelect = { selectedMethod = it }
        )

        Spacer(Modifier.height(12.dp))

        // Метод 3: Roll
        MethodCard(
            method = AbilityMethod.ROLL,
            title = "Бросок кубов (Roll)",
            description = "Сгенерируйте значения броском кубов по формуле (по умолчанию 4d6dl1). " +
                "Результаты случайны, что добавляет элемент удачи в создание персонажа.",
            selected = selectedMethod == AbilityMethod.ROLL,
            onSelect = { selectedMethod = it }
        )

        Spacer(Modifier.height(12.dp))

        // Метод 4: Custom
        MethodCard(
            method = AbilityMethod.CUSTOM,
            title = "Собственные значения (Custom)",
            description = "Введите значения характеристик вручную. " +
                "Полная свобода, но требует соблюдения правил диапазона (обычно 8-15 для стандартных персонажей).",
            selected = selectedMethod == AbilityMethod.CUSTOM,
            onSelect = { selectedMethod = it }
        )

        Spacer(Modifier.height(24.dp))

        // Кнопки навигации
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Назад") }
            ElevatedButton(
                onClick = {
                    vm.selectMethod(selectedMethod)
                    onNext(selectedMethod)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Далее")
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun MethodCard(
    method: AbilityMethod,
    title: String,
    description: String,
    selected: Boolean,
    onSelect: (AbilityMethod) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (selected) LightPink.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 4.dp else 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelect(method) }
                .padding(16.dp)
        ) {
            RadioButton(
                selected = selected,
                onClick = null
            )
            Spacer(Modifier.height(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, color = DeepRed)
                Spacer(Modifier.height(4.dp))
                Text(description, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}