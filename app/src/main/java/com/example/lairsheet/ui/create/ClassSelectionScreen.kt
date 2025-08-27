package com.example.lairsheet.ui.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun ClassSelectionScreen(
    viewModel: CharacterViewModel,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Шаг 3: Выберите класс и подкласс (если есть)")
        Spacer(modifier = Modifier.height(16.dp))
        Text("Класс:")
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(viewModel.getClasses()) { clazz ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = (viewModel.state.selectedClass == clazz),
                        onClick = { viewModel.selectClass(context, clazz) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = clazz)
                }
            }
            if (viewModel.getSubclasses().isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Подкласс:")
                }
                items(viewModel.getSubclasses()) { subcls ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = (viewModel.state.selectedSubclass == subcls),
                            onClick = { viewModel.selectSubclass(subcls) } // ← ВАЖНО
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = subcls)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onNext,
            enabled = viewModel.state.selectedClass.isNotBlank(),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Далее")
        }
    }
}
