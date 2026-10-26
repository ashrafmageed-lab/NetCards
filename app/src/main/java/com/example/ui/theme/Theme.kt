package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueLight,
    onPrimary = LightSurface,
    primaryContainer = PrimaryBlueDark,
    onPrimaryContainer = PrimaryBlueContainer,
    secondary = SecondaryTeal,
    onSecondary = LightSurface,
    secondaryContainer = SecondaryTealContainer,
    onSecondaryContainer = OnSecondaryTealContainer,
    tertiary = TertiaryAmber,
    onTertiary = LightSurface,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurface,
    outline = DarkOutline,
    error = ErrorRed,
    errorContainer = ErrorRedContainer
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = LightSurface,
    primaryContainer = PrimaryBlueContainer,
    onPrimaryContainer = OnPrimaryBlueContainer,
    secondary = SecondaryTeal,
    onSecondary = LightSurface,
    secondaryContainer = SecondaryTealContainer,
    onSecondaryContainer = OnSecondaryTealContainer,
    tertiary = TertiaryAmber,
    onTertiary = LightSurface,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurface,
    outline = LightOutline,
    error = ErrorRed,
    errorContainer = ErrorRedContainer
)

@Composable
fun CardsAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
