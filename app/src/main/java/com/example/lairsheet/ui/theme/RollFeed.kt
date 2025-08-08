package com.example.lairsheet.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.runtime.key

data class RollEntry(
    val id: Long,
    val sides: Int,
    val value: Int
)

@Composable
fun RollFeed(
    items: List<RollEntry>,
    onTimeout: (Long) -> Unit,
    onDismiss: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomStart // левый нижний угол
    ) {
        Column(
            verticalArrangement = Arrangement.Bottom, // снизу вверх
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .padding(start = 8.dp, bottom = 160.dp) // подняли выше кнопок roll
        ) {
            items.forEach { entry ->
                key(entry.id) {
                    RollToast(
                        entry = entry,
                        onTimeout = { onTimeout(entry.id) },
                        onDismiss = { onDismiss(entry.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun RollToast(
    entry: RollEntry,
    onTimeout: () -> Unit,
    onDismiss: () -> Unit,
) {
    var visible by remember { mutableStateOf(true) }

    // автоскрытие через 3 секунды
    LaunchedEffect(entry.id) {
        delay(3000)
        visible = false
        onTimeout()
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value != SwipeToDismissBoxValue.Settled) {
                visible = false
                onDismiss()
                true
            } else false
        }
    )

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { it / 3 } + fadeIn(),
        exit  = slideOutVertically { it / 3 } + fadeOut()
    ) {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {},
            enableDismissFromStartToEnd = true,
            enableDismissFromEndToStart = true
        ) {
            Surface(
                tonalElevation = 3.dp,
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clip(MaterialTheme.shapes.large)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    // Иконка кубика + маленькая цифра граней внутри
                    Box(
                        modifier = Modifier.size(22.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        DiceIcon(
                            sides = entry.sides,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxSize()
                        )
                        Text(
                            text = entry.sides.toString(),
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    // Текст результата рядом
                    Text(
                        text = "d${entry.sides} → ${entry.value}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
                    )
                }
            }
        }
    }
}
