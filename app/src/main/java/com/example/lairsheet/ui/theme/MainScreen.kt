package com.example.lairsheet.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lairsheet.R

@Composable
fun MainScreen() {
    Header()
}

@Composable
fun Header() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(LightPink, shape = RoundedCornerShape(12.dp))
            .padding(12.dp)
            .fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_dragon_logo),
            contentDescription = "Dragon Logo",
            modifier = Modifier.size(60.dp)
        )
        Spacer(modifier = Modifier.width(15.dp))
        Text(
            text = "Lair Sheet",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = DeepRed
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
    LairSheetTheme { MainScreen() }
}
