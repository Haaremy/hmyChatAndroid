package de.haaremy.hmychat.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = HmyPink,
    onPrimary = Color.White,
    primaryContainer = HmyPurple,
    onPrimaryContainer = Color.White,
    secondary = HmyPurpleLight,
    onSecondary = Color.White,
    secondaryContainer = HmyPurpleDark,
    onSecondaryContainer = HmyPinkLight,
    tertiary = HmyPinkLight,
    onTertiary = Color.White,
    background = HmyBackground,
    onBackground = HmyOnSurface,
    surface = HmySurface,
    onSurface = HmyOnSurface,
    surfaceVariant = HmySurfaceVariant,
    onSurfaceVariant = HmyOnSurfaceVariant,
    outline = HmyOnSurfaceVariant,
    inverseSurface = HmySurfaceLight,
    inverseOnSurface = HmyOnSurfaceLight
)

private val LightColorScheme = lightColorScheme(
    primary = HmyPurple,
    onPrimary = Color.White,
    primaryContainer = HmyPinkLight,
    onPrimaryContainer = HmyPurpleDark,
    secondary = HmyPink,
    onSecondary = Color.White,
    secondaryContainer = HmyPinkLight,
    onSecondaryContainer = HmyPurpleDark,
    tertiary = HmyPurpleLight,
    onTertiary = Color.White,
    background = HmyBackgroundLight,
    onBackground = HmyOnSurfaceLight,
    surface = HmySurfaceLight,
    onSurface = HmyOnSurfaceLight,
    surfaceVariant = HmySurfaceVariantLight,
    onSurfaceVariant = HmyOnSurfaceVariantLight,
    outline = HmyOnSurfaceVariantLight,
    inverseSurface = HmySurface,
    inverseOnSurface = HmyOnSurface
)

@Composable
fun HmyChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
