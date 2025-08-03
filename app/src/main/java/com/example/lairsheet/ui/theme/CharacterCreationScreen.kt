package com.example.lairsheet.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lairsheet.data.Character
import com.example.lairsheet.data.Ruleset

@Composable
fun CharacterCreationScreen(
    ruleset: Ruleset,
    onSave: (Character) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var className by remember { mutableStateOf("") }
    var race by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("1") }
    var background by remember { mutableStateOf("") }
    var alignment by remember { mutableStateOf("") }
    var str by remember { mutableStateOf("10") }
    var dex by remember { mutableStateOf("10") }
    var con by remember { mutableStateOf("10") }
    var intScore by remember { mutableStateOf("10") }
    var wis by remember { mutableStateOf("10") }
    var cha by remember { mutableStateOf("10") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Создание персонажа",
            fontSize = 24.sp,
            color = DeepRed
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Имя") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = className,
            onValueChange = { className = it },
            label = { Text("Класс") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = race,
            onValueChange = { race = it },
            label = { Text("Раса") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = level,
            onValueChange = { level = it.filter { ch -> ch.isDigit() } },
            label = { Text("Уровень") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = background,
            onValueChange = { background = it },
            label = { Text("Предыстория") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = alignment,
            onValueChange = { alignment = it },
            label = { Text("Мировоззрение") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = str,
                onValueChange = { str = it.filter { ch -> ch.isDigit() } },
                label = { Text("STR") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = dex,
                onValueChange = { dex = it.filter { ch -> ch.isDigit() } },
                label = { Text("DEX") },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = con,
                onValueChange = { con = it.filter { ch -> ch.isDigit() } },
                label = { Text("CON") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = intScore,
                onValueChange = { intScore = it.filter { ch -> ch.isDigit() } },
                label = { Text("INT") },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = wis,
                onValueChange = { wis = it.filter { ch -> ch.isDigit() } },
                label = { Text("WIS") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = cha,
                onValueChange = { cha = it.filter { ch -> ch.isDigit() } },
                label = { Text("CHA") },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LightPink,
                    contentColor = DeepRed
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Отмена")
            }
            Button(
                onClick = {
                    onSave(
                        Character(
                            name = name,
                            className = className,
                            race = race,
                            level = level.toIntOrNull() ?: 1,
                            background = background,
                            alignment = alignment,
                            strength = str.toIntOrNull() ?: 10,
                            dexterity = dex.toIntOrNull() ?: 10,
                            constitution = con.toIntOrNull() ?: 10,
                            intelligence = intScore.toIntOrNull() ?: 10,
                            wisdom = wis.toIntOrNull() ?: 10,
                            charisma = cha.toIntOrNull() ?: 10,
                            ruleset = ruleset
                        )
                    )
                },
                enabled = name.isNotBlank() && className.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepRed,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Сохранить")
            }
        }
    }
}