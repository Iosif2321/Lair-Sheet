package com.example.lairsheet.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun DataCatalogScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val files = remember { context.assets.list("data")?.toList() ?: emptyList() }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = onBack) { Text("Назад") }
        Spacer(Modifier.height(16.dp))
        files.forEach { Text(it) }
    }
}
