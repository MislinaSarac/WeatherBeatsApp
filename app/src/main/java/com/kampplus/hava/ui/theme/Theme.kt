package com.kampplus.hava.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kampplus.hava.data.AppThemeSetting
import com.kampplus.hava.data.UserSettingsManager

/**
 * Açık tema paleti.
 */
private val LightColors = lightColorScheme(
    primary = Teal40,
    onPrimary = Neutral99,
    primaryContainer = Teal90,
    onPrimaryContainer = Teal10,
    secondary = Amber40,
    secondaryContainer = Amber90,
    background = Neutral99,
    onBackground = Neutral10,
    surface = Neutral99,
    onSurface = Neutral10,
    surfaceVariant = NeutralVariant90,
    onSurfaceVariant = NeutralVariant30,
    surfaceContainer = Neutral95,
    error = Red40
)

/** Koyu tema paleti. */
private val DarkColors = darkColorScheme(
    primary = Teal80,
    onPrimary = Teal20,
    primaryContainer = Teal30,
    onPrimaryContainer = Teal90,
    secondary = Amber80,
    background = Neutral10,
    onBackground = Neutral90,
    surface = Neutral10,
    onSurface = Neutral90,
    onSurfaceVariant = NeutralVariant90,
    error = Red80
)

/**
 * Uygulamanın teması. [UserSettingsManager.themeSetting] durumuna göre dinamik güncellenir.
 */
@Composable
fun HavaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val themeSetting by UserSettingsManager.themeSetting.collectAsStateWithLifecycle()
    val isDark = when (themeSetting) {
        AppThemeSetting.DARK -> true
        AppThemeSetting.LIGHT -> false
        AppThemeSetting.SYSTEM -> darkTheme
    }

    MaterialTheme(
        colorScheme = if (isDark) DarkColors else LightColors,
        typography = HavaTypography,
        content = content
    )
}
