package com.example.lairsheet.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lairsheet.R

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightPink),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.ic_dragon_logo),
                contentDescription = "Логотип",
                modifier = Modifier.size(128.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Lair Sheet",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = DeepRed
            )
        }
    }
}