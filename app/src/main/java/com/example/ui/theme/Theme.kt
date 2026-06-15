package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = GoldPrimary,
    secondary = AccentGold,
    tertiary = AccentTeal,
    background = DarkSlateBg,
    surface = DarkSlateSurface,
    onPrimary = DarkSlateBg,
    onSecondary = DarkSlateBg,
    onTertiary = DarkSlateBg,
    onBackground = ParchmentBg,
    onSurface = ParchmentBg,
    surfaceVariant = DarkSlateCard
  )

private val LightColorScheme =
  lightColorScheme(
    primary = GoldPrimary,
    secondary = GoldSecondary,
    tertiary = AccentMaroon,
    background = ParchmentBg,
    surface = ParchmentSurface,
    onPrimary = DarkSlateBg,
    onSecondary = ParchmentBg,
    onTertiary = ParchmentBg,
    onBackground = WarmSlate,
    onSurface = WarmSlate,
    surfaceVariant = ParchmentSurface
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Set to false to prioritize our brand's unique church design
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
