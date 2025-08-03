package com.example.lairsheet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.lairsheet.ui.theme.LairSheetTheme
import com.example.lairsheet.ui.theme.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LairSheetTheme {
                MainScreen()
            }
        }
    }
}
