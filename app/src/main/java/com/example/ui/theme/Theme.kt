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
    primary = DarkProfessionalPrimary,
    onPrimary = DarkProfessionalOnPrimary,
    primaryContainer = DarkProfessionalPrimaryContainer,
    onPrimaryContainer = DarkProfessionalOnPrimaryContainer,
    secondary = DarkMedicalTealSecondary,
    onSecondary = DarkMedicalTealOnSecondary,
    secondaryContainer = DarkMedicalTealContainer,
    onSecondaryContainer = DarkMedicalTealOnContainer,
    tertiary = DarkAmberTertiary,
    onTertiary = DarkAmberOnContainer,
    tertiaryContainer = DarkAmberContainer,
    onTertiaryContainer = DarkAmberOnContainer,
    background = MedicalBackgroundDark,
    surface = MedicalSurfaceDark,
    surfaceVariant = MedicalSurfaceVariantDark,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ProfessionalPrimary,
    onPrimary = ProfessionalOnPrimary,
    primaryContainer = ProfessionalPrimaryContainer,
    onPrimaryContainer = ProfessionalOnPrimaryContainer,
    secondary = MedicalTealSecondary,
    onSecondary = MedicalTealOnSecondary,
    secondaryContainer = MedicalTealContainer,
    onSecondaryContainer = MedicalTealOnContainer,
    tertiary = AmberTertiary,
    onTertiary = ProfessionalOnPrimary,
    tertiaryContainer = AmberContainer,
    onTertiaryContainer = AmberOnContainer,
    background = MedicalBackgroundLight,
    surface = MedicalSurfaceLight,
    surfaceVariant = MedicalSurfaceVariantLight,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
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
