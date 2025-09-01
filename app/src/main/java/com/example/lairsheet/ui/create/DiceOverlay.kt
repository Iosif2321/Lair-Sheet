package com.example.lairsheet.ui.create

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DiceOverlay(
    modifier: Modifier = Modifier,
    onRoll: (sides: Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        FloatingActionButton(onClick = { expanded = !expanded }) {
            Icon(Icons.Filled.Casino, contentDescription = "Dice")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf(4, 6, 8, 10, 12, 20, 100).forEach { s ->
                DropdownMenuItem(
                    text = { Text("d$s") },
                    onClick = {
                        expanded = false
                        onRoll(s)
                    }
                )
            }
        }
    }
}