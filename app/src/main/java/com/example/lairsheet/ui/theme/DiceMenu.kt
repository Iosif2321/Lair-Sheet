package com.example.lairsheet.ui.theme

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun DiceMenu() {
    var sh by remember { mutableStateOf(false) }
    val ctx = LocalContext.current
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        if (sh) {
            Card(
                colors = CardDefaults.cardColors(containerColor = LightPink),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(end = 16.dp, bottom = 80.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row { DBtn(4, ctx); Spacer(Modifier.width(8.dp)); DBtn(6, ctx) }
                    Spacer(Modifier.height(8.dp))
                    Row { DBtn(8, ctx); Spacer(Modifier.width(8.dp)); DBtn(10, ctx) }
                    Spacer(Modifier.height(8.dp))
                    Row { DBtn(12, ctx); Spacer(Modifier.width(8.dp)); DBtn(20, ctx) }
                }
            }
        }
        FloatingActionButton(
            onClick = { sh = !sh },
            containerColor = DeepRed,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(16.dp)
        ) { Text("d8") }
    }
}

@Composable
private fun DBtn(s: Int, ctx: Context) {
    Button(
        onClick = {
            Toast.makeText(ctx, (1..s).random().toString(), Toast.LENGTH_SHORT).show()
        },
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.size(48.dp)
    ) { Text("d$s") }
}
