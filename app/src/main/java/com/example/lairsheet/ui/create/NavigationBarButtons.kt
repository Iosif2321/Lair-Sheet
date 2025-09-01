package com.example.lairsheet.ui.create

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NavBarButtons(
    modifier: Modifier = Modifier,
    backText: String = "Назад",
    nextText: String = "Далее",
    onBack: (() -> Unit)?,
    onNext: (() -> Unit)?,
    nextEnabled: Boolean = true
) {
    Surface(tonalElevation = 2.dp) {
        Row(
            modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                TextButton(onClick = onBack) { Text(backText) }
            } else {
                Spacer(Modifier.width(8.dp))
            }
            Spacer(Modifier.weight(1f))
            if (onNext != null) {
                Button(onClick = onNext, enabled = nextEnabled) { Text(nextText) }
            }
        }
    }
}