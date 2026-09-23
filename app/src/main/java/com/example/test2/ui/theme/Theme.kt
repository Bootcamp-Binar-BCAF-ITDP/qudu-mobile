package com.example.test2.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import com.example.test2.R

@Composable
fun Test2Theme(
    content: @Composable () -> Unit,
) {
    val colorScheme = lightColorScheme(
        primary = colorResource(R.color.brand_800),
        onPrimary = colorResource(R.color.white),
        primaryContainer = colorResource(R.color.brand_100),
        onPrimaryContainer = colorResource(R.color.brand_900),

        secondary = colorResource(R.color.brand_500),
        onSecondary = colorResource(R.color.white),
        secondaryContainer = colorResource(R.color.brand_200),
        onSecondaryContainer = colorResource(R.color.brand_900),

        tertiary = colorResource(R.color.brand_olive),
        onTertiary = colorResource(R.color.white),
        tertiaryContainer = colorResource(R.color.brand_olive_soft),
        onTertiaryContainer = colorResource(R.color.text_primary),

        background = colorResource(R.color.screen_bg),
        onBackground = colorResource(R.color.text_primary),
        surface = colorResource(R.color.card_bg),
        onSurface = colorResource(R.color.text_primary),
        surfaceVariant = colorResource(R.color.brand_50),
        onSurfaceVariant = colorResource(R.color.text_secondary),
        outline = colorResource(R.color.card_border),
        outlineVariant = colorResource(R.color.field_border),

        error = colorResource(R.color.danger),
        onError = colorResource(R.color.white),
        errorContainer = colorResource(R.color.danger_bg),
        onErrorContainer = colorResource(R.color.danger),
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
