package com.example.ideajar.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    background = BlackBackground,
    surface = SurfaceDark,
    primary = NeonBlue,
    secondary = NeonPurple,
    error = NeonRed,
    onBackground = StarWhite
)

@Composable
fun IdeaJarTheme(
    content: @Composable () -> Unit
) {
    // Force DarkColorScheme always - The Void Awaits
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}