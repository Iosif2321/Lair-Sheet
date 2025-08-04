package com.example.lairsheet.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AuthorsScreen(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Авторы",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = DeepRed
            )
            Spacer(Modifier.height(24.dp))
            Text(text = "Salamander", fontSize = 20.sp, color = DeepRed)
            Text(text = "Tron2321", fontSize = 20.sp, color = DeepRed)
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepRed,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(text = "Назад", fontSize = 16.sp)
            }
        }
    }
}