package com.pocketcode.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val Scheme = darkColorScheme(primary = AppleBlue, secondary = AppleGreen, error = AppleRed, background = AppleBlack, surface = AppleSurface, surfaceVariant = AppleElevated)
@Composable fun PocketCodeTheme(content: @Composable () -> Unit) = MaterialTheme(colorScheme = Scheme, typography = PocketTypography, content = content)
