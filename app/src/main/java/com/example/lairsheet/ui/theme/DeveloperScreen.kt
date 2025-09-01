package com.example.lairsheet.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DeveloperScreen(
    onBack: () -> Unit,
    onClear2014: () -> Unit,
    onClear2024: () -> Unit,
    onClearAll: () -> Unit,
    onQuickCreate2014: () -> Unit,
    onQuickCreate2024: () -> Unit
) {
    var showClearDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Developer Mode",
                fontSize = 28.sp,
                color = DeepRed
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Скрытый экран разработчика",
                fontSize = 16.sp,
                color = DeepRed
            )

            Spacer(Modifier.height(24.dp))

            // Очистка памяти
            Button(
                onClick = { showClearDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepRed,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Очистка памяти")
            }

            Spacer(Modifier.height(12.dp))

            // Быстрый старт — выбор правил здесь (этап 1)
            Button(
                onClick = onQuickCreate2014,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepRed,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Быстро создать (2014)")
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onQuickCreate2024,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepRed,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Быстро создать (2024)")
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepRed,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Назад")
            }
        }

        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                title = { Text("Очистка персонажей") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                showClearDialog = false
                                onClear2014()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DeepRed,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Очистить R5E 2014") }

                        Button(
                            onClick = {
                                showClearDialog = false
                                onClear2024()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DeepRed,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Очистить R5E 2024") }

                        Button(
                            onClick = {
                                showClearDialog = false
                                onClearAll()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DeepRed,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Очистить всех") }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showClearDialog = false }) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}