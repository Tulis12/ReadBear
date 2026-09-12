package dev.tulis.readbear.ui.theme.catppuccin

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

data class CatppuccinPalette(
    val rosewater: Color,
    val flamingo: Color,
    val pink: Color,
    val mauve: Color,
    val red: Color,
    val maroon: Color,
    val peach: Color,
    val yellow: Color,
    val green: Color,
    val teal: Color,
    val sky: Color,
    val sapphire: Color,
    val blue: Color,
    val lavender: Color,

    val text: Color,
    val subtext1: Color,
    val subtext0: Color,

    val overlay2: Color,
    val overlay1: Color,
    val overlay0: Color,

    val surface2: Color,
    val surface1: Color,
    val surface0: Color,

    val base: Color,
    val mantle: Color,
    val crust: Color,
)

private fun ColorScheme.withCatppuccinDefaults(
    c: CatppuccinPalette
): ColorScheme = copy(
    primary = c.mauve,
    onPrimary = c.base,
    primaryContainer = c.surface0,
    onPrimaryContainer = c.text,

    secondary = c.blue,
    onSecondary = c.base,
    secondaryContainer = c.surface0,
    onSecondaryContainer = c.text,

    tertiary = c.teal,
    onTertiary = c.base,
    tertiaryContainer = c.surface0,
    onTertiaryContainer = c.text,

    error = c.red,
    onError = c.base,
    errorContainer = c.surface0,
    onErrorContainer = c.text,

    background = c.base,
    onBackground = c.text,

    surface = c.base,
    onSurface = c.text,

    surfaceVariant = c.surface0,
    onSurfaceVariant = c.subtext0,

    outline = c.overlay0,
    outlineVariant = c.overlay1,

    inverseSurface = c.text,
    inverseOnSurface = c.base,
    inversePrimary = c.lavender,
)

val CatppuccinLatte = CatppuccinPalette(
    rosewater = Color(0xFFDC8A78),
    flamingo = Color(0xFFDD7878),
    pink = Color(0xFFEA76CB),
    mauve = Color(0xFF8839EF),
    red = Color(0xFFD20F39),
    maroon = Color(0xFFE64553),
    peach = Color(0xFFFE640B),
    yellow = Color(0xFFDF8E1D),
    green = Color(0xFF40A02B),
    teal = Color(0xFF179299),
    sky = Color(0xFF04A5E5),
    sapphire = Color(0xFF209FB5),
    blue = Color(0xFF1E66F5),
    lavender = Color(0xFF7287FD),

    text = Color(0xFF4C4F69),
    subtext1 = Color(0xFF5C5F77),
    subtext0 = Color(0xFF6C6F85),

    overlay2 = Color(0xFF7C7F93),
    overlay1 = Color(0xFF8C8FA1),
    overlay0 = Color(0xFF9CA0B0),

    surface2 = Color(0xFFACB0BE),
    surface1 = Color(0xFFBCC0CC),
    surface0 = Color(0xFFCCD0DA),

    base = Color(0xFFEFF1F5),
    mantle = Color(0xFFE6E9EF),
    crust = Color(0xFFDCE0E8),
)

val CatppuccinFrappe = CatppuccinPalette(
    rosewater = Color(0xFFF2D5CF),
    flamingo = Color(0xFFEEBEBE),
    pink = Color(0xFFF4B8E4),
    mauve = Color(0xFFCA9EE6),
    red = Color(0xFFE78284),
    maroon = Color(0xFFEA999C),
    peach = Color(0xFFEF9F76),
    yellow = Color(0xFFE5C890),
    green = Color(0xFFA6D189),
    teal = Color(0xFF81C8BE),
    sky = Color(0xFF99D1DB),
    sapphire = Color(0xFF85C1DC),
    blue = Color(0xFF8CAAEE),
    lavender = Color(0xFFBABBF1),

    text = Color(0xFFC6D0F5),
    subtext1 = Color(0xFFB5BFE2),
    subtext0 = Color(0xFFA5ADCE),

    overlay2 = Color(0xFF949CBB),
    overlay1 = Color(0xFF838BA7),
    overlay0 = Color(0xFF737994),

    surface2 = Color(0xFF626880),
    surface1 = Color(0xFF51576D),
    surface0 = Color(0xFF414559),

    base = Color(0xFF303446),
    mantle = Color(0xFF292C3C),
    crust = Color(0xFF232634),
)

val CatppuccinMacchiato = CatppuccinPalette(
    rosewater = Color(0xFFF4DBD6),
    flamingo = Color(0xFFF0C6C6),
    pink = Color(0xFFF5BDE6),
    mauve = Color(0xFFC6A0F6),
    red = Color(0xFFED8796),
    maroon = Color(0xFFEE99A0),
    peach = Color(0xFFF5A97F),
    yellow = Color(0xFFEED49F),
    green = Color(0xFFA6DA95),
    teal = Color(0xFF8BD5CA),
    sky = Color(0xFF91D7E3),
    sapphire = Color(0xFF7DC4E4),
    blue = Color(0xFF8AADF4),
    lavender = Color(0xFFB7BDF8),

    text = Color(0xFFCAD3F5),
    subtext1 = Color(0xFFB8C0E0),
    subtext0 = Color(0xFFA5ADCB),

    overlay2 = Color(0xFF939AB7),
    overlay1 = Color(0xFF8087A2),
    overlay0 = Color(0xFF6E738D),

    surface2 = Color(0xFF5B6078),
    surface1 = Color(0xFF494D64),
    surface0 = Color(0xFF363A4F),

    base = Color(0xFF24273A),
    mantle = Color(0xFF1E2030),
    crust = Color(0xFF181926),
)

val CatppuccinMocha = CatppuccinPalette(
    rosewater = Color(0xFFF5E0DC),
    flamingo = Color(0xFFF2CDCD),
    pink = Color(0xFFF5C2E7),
    mauve = Color(0xFFCBA6F7),
    red = Color(0xFFF38BA8),
    maroon = Color(0xFFEBA0AC),
    peach = Color(0xFFFAB387),
    yellow = Color(0xFFF9E2AF),
    green = Color(0xFFA6E3A1),
    teal = Color(0xFF94E2D5),
    sky = Color(0xFF89DCEB),
    sapphire = Color(0xFF74C7EC),
    blue = Color(0xFF89B4FA),
    lavender = Color(0xFFB4BEFE),

    text = Color(0xFFCDD6F4),
    subtext1 = Color(0xFFBAC2DE),
    subtext0 = Color(0xFFA6ADC8),

    overlay2 = Color(0xFF9399B2),
    overlay1 = Color(0xFF7F849C),
    overlay0 = Color(0xFF6C7086),

    surface2 = Color(0xFF585B70),
    surface1 = Color(0xFF45475A),
    surface0 = Color(0xFF313244),

    base = Color(0xFF1E1E2E),
    mantle = Color(0xFF181825),
    crust = Color(0xFF11111B),
)

val LightColorScheme = lightColorScheme(
    primary = CatppuccinLatte.mauve,
    onPrimary = CatppuccinLatte.base,
    primaryContainer = CatppuccinLatte.surface0,
    onPrimaryContainer = CatppuccinLatte.text,

    secondary = CatppuccinLatte.blue,
    onSecondary = CatppuccinLatte.base,
    secondaryContainer = CatppuccinLatte.surface0,
    onSecondaryContainer = CatppuccinLatte.text,

    tertiary = CatppuccinLatte.teal,
    onTertiary = CatppuccinLatte.base,
    tertiaryContainer = CatppuccinLatte.surface0,
    onTertiaryContainer = CatppuccinLatte.text,

    error = CatppuccinLatte.red,
    onError = CatppuccinLatte.base,
    errorContainer = CatppuccinLatte.surface0,
    onErrorContainer = CatppuccinLatte.text,

    background = CatppuccinLatte.base,
    onBackground = CatppuccinLatte.text,

    surface = CatppuccinLatte.base,
    onSurface = CatppuccinLatte.text,

    surfaceVariant = CatppuccinLatte.surface0,
    onSurfaceVariant = CatppuccinLatte.subtext0,

    outline = CatppuccinLatte.overlay0,
    outlineVariant = CatppuccinLatte.overlay1,

    inverseSurface = CatppuccinLatte.text,
    inverseOnSurface = CatppuccinLatte.base,
    inversePrimary = CatppuccinLatte.lavender,
)

val FrappeColorScheme = darkColorScheme(
    primary = CatppuccinFrappe.mauve,
    onPrimary = CatppuccinFrappe.crust,
    primaryContainer = CatppuccinFrappe.surface0,
    onPrimaryContainer = CatppuccinFrappe.text,

    secondary = CatppuccinFrappe.blue,
    onSecondary = CatppuccinFrappe.crust,
    secondaryContainer = CatppuccinFrappe.surface0,
    onSecondaryContainer = CatppuccinFrappe.text,

    tertiary = CatppuccinFrappe.teal,
    onTertiary = CatppuccinFrappe.crust,
    tertiaryContainer = CatppuccinFrappe.surface0,
    onTertiaryContainer = CatppuccinFrappe.text,

    error = CatppuccinFrappe.red,
    onError = CatppuccinFrappe.crust,
    errorContainer = CatppuccinFrappe.surface0,
    onErrorContainer = CatppuccinFrappe.text,

    background = CatppuccinFrappe.base,
    onBackground = CatppuccinFrappe.text,

    surface = CatppuccinFrappe.base,
    onSurface = CatppuccinFrappe.text,

    surfaceVariant = CatppuccinFrappe.surface0,
    onSurfaceVariant = CatppuccinFrappe.subtext0,

    outline = CatppuccinFrappe.overlay0,
    outlineVariant = CatppuccinFrappe.overlay1,

    inverseSurface = CatppuccinFrappe.text,
    inverseOnSurface = CatppuccinFrappe.base,
    inversePrimary = CatppuccinFrappe.lavender,
)

val MacchiatoColorScheme = darkColorScheme(
    primary = CatppuccinMacchiato.mauve,
    onPrimary = CatppuccinMacchiato.crust,
    primaryContainer = CatppuccinMacchiato.surface0,
    onPrimaryContainer = CatppuccinMacchiato.text,

    secondary = CatppuccinMacchiato.blue,
    onSecondary = CatppuccinMacchiato.crust,
    secondaryContainer = CatppuccinMacchiato.surface0,
    onSecondaryContainer = CatppuccinMacchiato.text,

    tertiary = CatppuccinMacchiato.teal,
    onTertiary = CatppuccinMacchiato.crust,
    tertiaryContainer = CatppuccinMacchiato.surface0,
    onTertiaryContainer = CatppuccinMacchiato.text,

    error = CatppuccinMacchiato.red,
    onError = CatppuccinMacchiato.crust,
    errorContainer = CatppuccinMacchiato.surface0,
    onErrorContainer = CatppuccinMacchiato.text,

    background = CatppuccinMacchiato.base,
    onBackground = CatppuccinMacchiato.text,

    surface = CatppuccinMacchiato.base,
    onSurface = CatppuccinMacchiato.text,

    surfaceVariant = CatppuccinMacchiato.surface0,
    onSurfaceVariant = CatppuccinMacchiato.subtext0,

    outline = CatppuccinMacchiato.overlay0,
    outlineVariant = CatppuccinMacchiato.overlay1,

    inverseSurface = CatppuccinMacchiato.text,
    inverseOnSurface = CatppuccinMacchiato.base,
    inversePrimary = CatppuccinMacchiato.lavender,
)

val MochaColorScheme = darkColorScheme(
    primary = CatppuccinMocha.mauve,
    onPrimary = CatppuccinMocha.crust,
    primaryContainer = CatppuccinMocha.surface0,
    onPrimaryContainer = CatppuccinMocha.text,

    secondary = CatppuccinMocha.blue,
    onSecondary = CatppuccinMocha.crust,
    secondaryContainer = CatppuccinMocha.surface0,
    onSecondaryContainer = CatppuccinMocha.text,

    tertiary = CatppuccinMocha.teal,
    onTertiary = CatppuccinMocha.crust,
    tertiaryContainer = CatppuccinMocha.surface0,
    onTertiaryContainer = CatppuccinMocha.text,

    error = CatppuccinMocha.red,
    onError = CatppuccinMocha.crust,
    errorContainer = CatppuccinMocha.surface0,
    onErrorContainer = CatppuccinMocha.text,

    background = CatppuccinMocha.base,
    onBackground = CatppuccinMocha.text,

    surface = CatppuccinMocha.base,
    onSurface = CatppuccinMocha.text,

    surfaceVariant = CatppuccinMocha.surface0,
    onSurfaceVariant = CatppuccinMocha.subtext0,

    outline = CatppuccinMocha.overlay0,
    outlineVariant = CatppuccinMocha.overlay1,

    inverseSurface = CatppuccinMocha.text,
    inverseOnSurface = CatppuccinMocha.base,
    inversePrimary = CatppuccinMocha.lavender,
)