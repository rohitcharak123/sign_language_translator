package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ElectricTeal,
    onPrimary = Color(0xFF003641),
    primaryContainer = Color(0xFF004E5E),
    onPrimaryContainer = SoftCyan,
    secondary = GlowAmber,
    onSecondary = Color(0xFF432C00),
    secondaryContainer = Color(0xFF604100),
    onSecondaryContainer = Color(0xFFFFDEA8),
    tertiary = AlertCoral,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = DeepTeal,
    onPrimary = Color.White,
    primaryContainer = SoftCyan,
    onPrimaryContainer = Color(0xFF001F28),
    secondary = DeepAmber,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDEA8),
    onSecondaryContainer = Color(0xFF281800),
    tertiary = AlertCoral,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightBorder
)

@Composable
fun SignBridgeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to prioritize high-contrast SignBridge colors
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
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    SignBridgeTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
