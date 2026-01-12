package com.bramish.wheelofpasiva.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(
    primary = FortunePrimary,
    primaryVariant = FortunePrimaryVariant,
    secondary = FortuneSecondary,
    background = FortuneBackground,
    surface = FortuneSurface,
    onPrimary = FortuneOnPrimary,
    onSecondary = FortuneOnSecondary,
    onBackground = FortuneOnBackground,
    onSurface = FortuneOnSurface,
    error = FortuneError
)

@Composable
fun WheelOfPasivaTheme(
    content: @Composable () -> Unit
) {
    // Forcing Dark Theme for this specific design request
    // If we wanted to support light mode later, we could toggle it here.
    val colors = DarkColorPalette

    MaterialTheme(
        colors = colors,
        typography = FortuneTypography,
        shapes = FortuneShapes,
        content = content
    )
}
