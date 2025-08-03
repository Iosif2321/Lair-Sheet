package com.example.lairsheet.ui.theme

// import android.app.Activity // Этот импорт не используется в предоставленном коде, можно удалить
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color // <<< ДОБАВЛЕН ЭТОТ ИМПОРТ
import androidx.compose.ui.platform.LocalContext

// Эти цвета должны быть определены в вашем файле Color.kt в пакете com.example.lairsheet.ui.theme
// Например:
// val Purple80 = Color(0xFFD0BCFF)
// val PurpleGrey80 = Color(0xFFCCC2DC)
// val Pink80 = Color(0xFFEFB8C8)
// val DeepRed = Color(0xFFB9375D)
// val CoralRed = Color(0xFFD25D5D)
// val LightPink = Color(0xFFE7D3D3)
// val LightGrayBG = Color(0xFFEEEEEE)

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = DeepRed,
    secondary = CoralRed,
    tertiary = LightPink,
    background = LightGrayBG,
    surface = Color.White, // Теперь Color.White будет распознан
    onPrimary = Color.White, // Теперь Color.White будет распознан
    onSecondary = Color.White, // Теперь Color.White будет распознан
    onBackground = Color.Black, // Теперь Color.Black будет распознан
    onSurface = Color.Black // Теперь Color.Black будет распознан
)

@Composable
fun LairSheetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Убедитесь, что Typography определен (обычно в Type.kt)
        content = content
    )
}
