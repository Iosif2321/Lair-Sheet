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
fun RaceSelectionScreen(
    viewModel: CharacterViewModel,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Шаг 2: Выберите расу и подрасу (если есть)")
        Spacer(modifier = Modifier.height(16.dp))
        Text("Раса:")
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(viewModel.getRaces()) { race ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = (viewModel.state.selectedRace == race),
                        onClick = { viewModel.selectRace(context, race) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = race)
                }
            }
            if (viewModel.getSubraces().isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Подраса:")
                }
                items(viewModel.getSubraces()) { subrace ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = (viewModel.state.selectedSubrace == subrace),
                            onClick = { viewModel.selectSubrace(subrace) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = subrace)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onNext,
            enabled = viewModel.state.selectedRace.isNotBlank(),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Далее")
        }
    }
}
