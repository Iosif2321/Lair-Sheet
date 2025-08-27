package com.example.lairsheet.ui.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun BackgroundScreen(
    viewModel: CharacterViewModel,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { viewModel.loadBackgrounds(context) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Шаг 5: Выберите предысторию")
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(viewModel.getBackgrounds()) { bg ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = (viewModel.state.selectedBackground == bg),
                        onClick = { viewModel.selectBackground(bg) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = bg)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onNext,
            enabled = viewModel.state.selectedBackground.isNotBlank(),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Далее")
        }
    }
}
