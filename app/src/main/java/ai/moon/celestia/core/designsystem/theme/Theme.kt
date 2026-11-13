package ai.moon.celestia.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class CelestiaThemeStyle(
    val id: String,
    val displayName: String,
    val description: String,
    val emoji: String
) {
    AppleIos(
        id = "AppleIos",
        displayName = "Apple iOS",
        description = "Clean minimalist Apple HIG styling with grouped cards & system blur",
        emoji = "🍏"
    ),
    NeoBrutalist(
        id = "NeoBrutalist",
        displayName = "Neo-Brutalist",
        description = "High contrast pop colors, thick black borders & 3D drop shadows",
        emoji = "⚡"
    ),
    RetroSynthwave(
        id = "RetroSynthwave",
        displayName = "Retro Synthwave",
        description = "Neon cyan & pink cyberpunk glow on deep space darks",
        emoji = "🌌"
    ),
    SquishyClay(
        id = "SquishyClay",
        displayName = "Squishy Clay",
        description = "Soft pastel candy hues with ultra-rounded inflated 3D pills",
        emoji = "🍬"
    ),
    SpaceHud(
        id = "SpaceHud",
        displayName = "Space HUD",
        description = "Tactical sci-fi command deck with chamfered cut corners",
        emoji = "🛰️"
    ),
    ClassicCosmic(
        id = "ClassicCosmic",
        displayName = "Classic Cosmic",
        description = "Clean modern M3 cosmic purple and cyan space aesthetic",
        emoji = "✨"
    );

    companion object {
        fun fromId(id: String): CelestiaThemeStyle {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: AppleIos
        }
    }
}

@Immutable
data class CelestiaThemeDecoration(
    val style: CelestiaThemeStyle,
    val borderWidth: Dp = 1.dp,
    val borderColor: Color = Color.Transparent,
    val shadowOffset: Dp = 0.dp,
    val shadowColor: Color = Color.Transparent,
    val cardCornerRadius: Dp = 16.dp,
    val isCutCorner: Boolean = false,
    val hasGlow: Boolean = false,
    val glowColor: Color = Color.Transparent
)

val LocalCelestiaThemeDecoration = staticCompositionLocalOf {
    CelestiaThemeDecoration(style = CelestiaThemeStyle.NeoBrutalist)
}

// 6. Apple iOS Color Schemes
private val AppleDarkColorScheme = darkColorScheme(
    primary = AppleSystemBlue,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF0A346E),
    onPrimaryContainer = Color(0xFFD4E3FC),
    secondary = AppleSystemTeal,
    onSecondary = Color(0xFF003344),
    secondaryContainer = Color(0xFF0A4052),
    onSecondaryContainer = Color(0xFFD0F0FF),
    tertiary = AppleSystemPurple,
    onTertiary = Color(0xFFFFFFFF),
    background = AppleSystemBackgroundDark,
    onBackground = Color(0xFFFFFFFF),
    surface = AppleSecondaryBackgroundDark,
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = AppleTertiaryBackgroundDark,
    onSurfaceVariant = Color(0xFF8E8E93),
    outline = Color(0xFF38383A),
    outlineVariant = Color(0xFF2C2C2E)
)

private val AppleLightColorScheme = lightColorScheme(
    primary = AppleSystemBlue,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE8F1FF),
    onPrimaryContainer = Color(0xFF003366),
    secondary = AppleSystemTeal,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE0F4FF),
    onSecondaryContainer = Color(0xFF003344),
    tertiary = AppleSystemPurple,
    onTertiary = Color(0xFFFFFFFF),
    background = AppleSystemBackgroundLight,
    onBackground = Color(0xFF000000),
    surface = AppleSecondaryBackgroundLight,
    onSurface = Color(0xFF000000),
    surfaceVariant = AppleTertiaryBackgroundLight,
    onSurfaceVariant = Color(0xFF8E8E93),
    outline = Color(0xFFC6C6C8),
    outlineVariant = Color(0xFFE5E5EA)
)

// 1. Classic Cosmic
private val ClassicDarkColorScheme = darkColorScheme(
    primary = CosmicPurple80,
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = CelestialCyan80,
    onSecondary = Color(0xFF00363A),
    secondaryContainer = Color(0xFF004F53),
    onSecondaryContainer = Color(0xFFB2EBF2),
    tertiary = NebulaPink80,
    onTertiary = Color(0xFF5B0028),
    tertiaryContainer = Color(0xFF7D0038),
    onTertiaryContainer = Color(0xFFFFD9E2),
    background = SpaceBackgroundDark,
    onBackground = Color(0xFFE6E1E5),
    surface = SpaceSurfaceDark,
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = SpaceSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFCAC4D0),
    surfaceContainerLow = Color(0xFF1E1E2C),
    surfaceContainerHigh = Color(0xFF2B2B3D),
    surfaceContainerHighest = Color(0xFF36364D),
    outline = Color(0xFF49454F),
    outlineVariant = Color(0xFF38384A)
)

private val ClassicLightColorScheme = lightColorScheme(
    primary = CosmicPurple40,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = CelestialCyan40,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFB2EBF2),
    onSecondaryContainer = Color(0xFF002022),
    tertiary = NebulaPink40,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD9E2),
    onTertiaryContainer = Color(0xFF3E001D),
    background = SpaceBackgroundLight,
    onBackground = Color(0xFF1C1B1F),
    surface = SpaceSurfaceLight,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = SpaceSurfaceVariantLight,
    onSurfaceVariant = Color(0xFF49454F),
    surfaceContainerLow = Color(0xFFF3F2F8),
    surfaceContainerHigh = Color(0xFFE5E3ED),
    surfaceContainerHighest = Color(0xFFD9D7E3),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFC4C0C9)
)

// 2. Neo-Brutalist Color Schemes
private val NeoDarkColorScheme = darkColorScheme(
    primary = NeoLimeDark,
    onPrimary = Color(0xFF121212),
    primaryContainer = Color(0xFF2D3B00),
    onPrimaryContainer = NeoLimeDark,
    secondary = NeoPink,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF520026),
    onSecondaryContainer = Color(0xFFFFB0D1),
    tertiary = NeoCyan,
    onTertiary = Color(0xFF00363D),
    background = NeoBgDark,
    onBackground = Color(0xFFFFFFFF),
    surface = NeoSurfaceDark,
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = NeoSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFE0E0E0),
    outline = Color(0xFF000000),
    outlineVariant = Color(0xFF3A3A3A)
)

private val NeoLightColorScheme = lightColorScheme(
    primary = NeoLimeLight,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE6FF80),
    onPrimaryContainer = Color(0xFF1D2600),
    secondary = NeoPink,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFD6E8),
    onSecondaryContainer = Color(0xFF520026),
    tertiary = Color(0xFF00838F),
    onTertiary = Color(0xFFFFFFFF),
    background = NeoBgLight,
    onBackground = Color(0xFF121212),
    surface = NeoSurfaceLight,
    onSurface = Color(0xFF121212),
    surfaceVariant = NeoSurfaceVariantLight,
    onSurfaceVariant = Color(0xFF2C2C2C),
    outline = Color(0xFF000000),
    outlineVariant = Color(0xFF888888)
)

// 3. Retro Synthwave Color Schemes
private val SynthDarkColorScheme = darkColorScheme(
    primary = SynthPink80,
    onPrimary = Color(0xFF4D0025),
    primaryContainer = Color(0xFF7A003D),
    onPrimaryContainer = Color(0xFFFFB3D9),
    secondary = LaserCyan80,
    onSecondary = Color(0xFF003838),
    secondaryContainer = Color(0xFF005959),
    onSecondaryContainer = Color(0xFFB3FFFF),
    tertiary = NeonAmber80,
    onTertiary = Color(0xFF422B00),
    background = SynthBgDark,
    onBackground = Color(0xFFF2ECFA),
    surface = SynthSurfaceDark,
    onSurface = Color(0xFFF2ECFA),
    surfaceVariant = SynthSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFD4C8EA),
    outline = LaserCyan80,
    outlineVariant = SynthPink80
)

private val SynthLightColorScheme = lightColorScheme(
    primary = SynthPink40,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFD6E8),
    onPrimaryContainer = Color(0xFF4D0025),
    secondary = LaserCyan40,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFB3FFFF),
    onSecondaryContainer = Color(0xFF003838),
    tertiary = Color(0xFFB37B00),
    onTertiary = Color(0xFFFFFFFF),
    background = SynthBgLight,
    onBackground = Color(0xFF13132B),
    surface = SynthSurfaceLight,
    onSurface = Color(0xFF13132B),
    surfaceVariant = SynthSurfaceVariantLight,
    onSurfaceVariant = Color(0xFF333355),
    outline = LaserCyan40,
    outlineVariant = SynthPink40
)

// 4. Squishy Claymorphism Color Schemes
private val ClayDarkColorScheme = darkColorScheme(
    primary = ClayPink,
    onPrimary = Color(0xFF4D001F),
    primaryContainer = Color(0xFF730032),
    onPrimaryContainer = Color(0xFFFFB8D2),
    secondary = ClayLavender,
    onSecondary = Color(0xFF2B0066),
    secondaryContainer = Color(0xFF461A93),
    onSecondaryContainer = Color(0xFFE5D8FF),
    tertiary = ClayMint,
    onTertiary = Color(0xFF003828),
    background = ClayBgDark,
    onBackground = Color(0xFFFAF7FF),
    surface = ClaySurfaceDark,
    onSurface = Color(0xFFFAF7FF),
    surfaceVariant = ClaySurfaceVariantDark,
    onSurfaceVariant = Color(0xFFDCD2F0),
    outline = ClayLavender,
    outlineVariant = ClayPink
)

private val ClayLightColorScheme = lightColorScheme(
    primary = Color(0xFFD6336C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFD6E5),
    onPrimaryContainer = Color(0xFF4D001F),
    secondary = Color(0xFF7048E8),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE5D8FF),
    onSecondaryContainer = Color(0xFF2B0066),
    tertiary = Color(0xFF0CA678),
    onTertiary = Color(0xFFFFFFFF),
    background = ClayBgLight,
    onBackground = Color(0xFF181721),
    surface = ClaySurfaceLight,
    onSurface = Color(0xFF181721),
    surfaceVariant = ClaySurfaceVariantLight,
    onSurfaceVariant = Color(0xFF45425A),
    outline = Color(0xFFB197FC),
    outlineVariant = Color(0xFFFF8AB3)
)

// 5. Space HUD Color Schemes
private val HudDarkColorScheme = darkColorScheme(
    primary = HudCyan80,
    onPrimary = Color(0xFF003648),
    primaryContainer = Color(0xFF004D65),
    onPrimaryContainer = Color(0xFFB8F2FF),
    secondary = HudMagenta80,
    onSecondary = Color(0xFF500028),
    secondaryContainer = Color(0xFF78003E),
    onSecondaryContainer = Color(0xFFFFB0D5),
    tertiary = HudAmber80,
    onTertiary = Color(0xFF003544),
    background = HudBgDark,
    onBackground = Color(0xFFEBF2FA),
    surface = HudSurfaceDark,
    onSurface = Color(0xFFEBF2FA),
    surfaceVariant = HudSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFB8C8E8),
    outline = HudCyan80,
    outlineVariant = HudMagenta80
)

private val HudLightColorScheme = lightColorScheme(
    primary = HudCyan40,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB8F2FF),
    onPrimaryContainer = Color(0xFF001F2B),
    secondary = Color(0xFFC2185B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFD9E8),
    onSecondaryContainer = Color(0xFF3E001A),
    tertiary = Color(0xFF0077B6),
    onTertiary = Color(0xFFFFFFFF),
    background = HudBgLight,
    onBackground = Color(0xFF0B132B),
    surface = HudSurfaceLight,
    onSurface = Color(0xFF0B132B),
    surfaceVariant = HudSurfaceVariantLight,
    onSurfaceVariant = Color(0xFF3A4B6E),
    outline = HudCyan40,
    outlineVariant = Color(0xFFC2185B)
)

@Composable
fun CelestiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    themeStyle: CelestiaThemeStyle = CelestiaThemeStyle.NeoBrutalist,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val colorScheme: ColorScheme = when {
        dynamicColor && themeStyle == CelestiaThemeStyle.ClassicCosmic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> when (themeStyle) {
            CelestiaThemeStyle.AppleIos -> if (darkTheme) AppleDarkColorScheme else AppleLightColorScheme
            CelestiaThemeStyle.NeoBrutalist -> if (darkTheme) NeoDarkColorScheme else NeoLightColorScheme
            CelestiaThemeStyle.RetroSynthwave -> if (darkTheme) SynthDarkColorScheme else SynthLightColorScheme
            CelestiaThemeStyle.SquishyClay -> if (darkTheme) ClayDarkColorScheme else ClayLightColorScheme
            CelestiaThemeStyle.SpaceHud -> if (darkTheme) HudDarkColorScheme else HudLightColorScheme
            CelestiaThemeStyle.ClassicCosmic -> if (darkTheme) ClassicDarkColorScheme else ClassicLightColorScheme
        }
    }

    val decoration = when (themeStyle) {
        CelestiaThemeStyle.AppleIos -> CelestiaThemeDecoration(
            style = themeStyle,
            borderWidth = 0.5.dp,
            borderColor = colorScheme.outline,
            shadowOffset = 0.dp,
            cardCornerRadius = 18.dp,
            isCutCorner = false,
            hasGlow = false
        )
        CelestiaThemeStyle.NeoBrutalist -> CelestiaThemeDecoration(
            style = themeStyle,
            borderWidth = 2.dp,
            borderColor = if (darkTheme) Color.Black else Color.Black,
            shadowOffset = 4.dp,
            shadowColor = Color.Black,
            cardCornerRadius = 8.dp,
            isCutCorner = false,
            hasGlow = false
        )
        CelestiaThemeStyle.RetroSynthwave -> CelestiaThemeDecoration(
            style = themeStyle,
            borderWidth = 1.5.dp,
            borderColor = colorScheme.outline,
            shadowOffset = 0.dp,
            cardCornerRadius = 16.dp,
            isCutCorner = false,
            hasGlow = true,
            glowColor = colorScheme.primary
        )
        CelestiaThemeStyle.SquishyClay -> CelestiaThemeDecoration(
            style = themeStyle,
            borderWidth = 0.dp,
            borderColor = Color.Transparent,
            shadowOffset = 6.dp,
            shadowColor = if (darkTheme) Color(0xFF000000) else Color(0x33A288E3),
            cardCornerRadius = 28.dp,
            isCutCorner = false,
            hasGlow = false
        )
        CelestiaThemeStyle.SpaceHud -> CelestiaThemeDecoration(
            style = themeStyle,
            borderWidth = 1.dp,
            borderColor = colorScheme.primary.copy(alpha = 0.6f),
            shadowOffset = 2.dp,
            shadowColor = colorScheme.primary.copy(alpha = 0.2f),
            cardCornerRadius = 12.dp,
            isCutCorner = true,
            hasGlow = true,
            glowColor = colorScheme.primary
        )
        CelestiaThemeStyle.ClassicCosmic -> CelestiaThemeDecoration(
            style = themeStyle,
            borderWidth = 1.dp,
            borderColor = colorScheme.outlineVariant,
            shadowOffset = 0.dp,
            cardCornerRadius = 16.dp,
            isCutCorner = false,
            hasGlow = false
        )
    }

    CompositionLocalProvider(
        LocalCelestiaThemeDecoration provides decoration
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
