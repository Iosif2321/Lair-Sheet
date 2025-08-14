package com.example.lairsheet.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
fun AuthorsScreen(
    onBack: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Lair Sheet",
                color = colors.primary,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(Modifier.height(8.dp))
            Text(text = "Авторы", fontSize = 22.sp, color = colors.onBackground)

            Spacer(Modifier.height(24.dp))
            Text(text = "Salamander", fontSize = 20.sp, color = colors.primary)
            Text(text = "Tron2321", fontSize = 20.sp, color = colors.primary)

            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                )
            ) {
                Text(text = "Назад", fontSize = 16.sp)
            }
        }
    }
}
