package com.pocketcode.app.ui

/** Compatibility entry point for existing UI callers. */
import androidx.compose.runtime.Composable
import com.pocketcode.app.ui.theme.PocketCodeTheme as Theme

@Composable
fun PocketCodeTheme(content: @Composable () -> Unit) = Theme(content)
