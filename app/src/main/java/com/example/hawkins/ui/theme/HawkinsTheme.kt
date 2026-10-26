package com.example.hawkins.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.design.systemColorScheme

// Generated from design/design-spec.json; edit the accepted spec first.
object HawkinsColors {
    val fillPrimary = Color(0xFFD6AF70) // design-style: fixed-figma-color design-spec.json fillPrimary
    val fillSecondary = Color(0xFF625948) // design-style: fixed-figma-color design-spec.json fillSecondary
    val fillTertiary = Color(0xFF34362E) // design-style: fixed-figma-color design-spec.json fillTertiary
    val fillLight = Color(0xFF45463B) // design-style: fixed-figma-color design-spec.json fillLight
    val labelPrimaryLight = Color(0xFF211F19) // design-style: fixed-figma-color design-spec.json labelPrimaryLight
    val labelPrimary = Color(0xFFF1E5CD) // design-style: fixed-figma-color design-spec.json labelPrimary
    val labelSecondary = Color(0xFFC7BEA9) // design-style: fixed-figma-color design-spec.json labelSecondary
    val labelTertiary = Color(0xFFBBB39F) // design-style: fixed-figma-color design-spec.json labelTertiary
    val labelQuaternary = Color(0xFFA9A28E) // design-style: fixed-figma-color design-spec.json labelQuaternary
    val lightenHover = Color(0x14FFFFFF) // design-style: fixed-figma-color design-spec.json lightenHover
    val lightenPressed = Color(0x26FFFFFF) // design-style: fixed-figma-color design-spec.json lightenPressed
    val error = Color(0xFFE7857A) // design-style: fixed-figma-color design-spec.json error
    val alert = Color(0xFFD6AF70) // design-style: fixed-figma-color design-spec.json alert
    val passable = Color(0xFFB8C89A) // design-style: fixed-figma-color design-spec.json passable
    val interaction = Color(0xFFD6AF70) // design-style: fixed-figma-color design-spec.json interaction
    val dividerLine = Color(0xFF625948) // design-style: fixed-figma-color design-spec.json dividerLine
    val midnight = Color(0xFF162C40) // design-style: fixed-figma-color design-spec.json midnight
    val cold_light = Color(0xFF94C6DC) // design-style: fixed-figma-color design-spec.json cold_light
    val rift_red = Color(0xFFA63844) // design-style: fixed-figma-color design-spec.json rift_red
    val signal_amber = Color(0xFFE5B86B) // design-style: fixed-figma-color design-spec.json signal_amber
    val plaster = Color(0xFFB9AA85) // design-style: fixed-figma-color design-spec.json plaster
    val wood = Color(0xFF48392B) // design-style: fixed-figma-color design-spec.json wood
    val sofa = Color(0xFF82614E) // design-style: fixed-figma-color design-spec.json sofa
    val rug = Color(0xFF622E36) // design-style: fixed-figma-color design-spec.json rug
    val ink = Color(0xFF34282A) // design-style: fixed-figma-color design-spec.json ink
    val vine = Color(0xFF361F29) // design-style: fixed-figma-color design-spec.json vine
    val receiver_shell = Color(0xFF242620) // design-style: fixed-figma-color design-spec.json receiver_shell
    val receiver_screen = Color(0xFF151C17) // design-style: fixed-figma-color design-spec.json receiver_screen
    val receiver_red = Color(0xFFDD8275) // design-style: fixed-figma-color design-spec.json receiver_red
    val receiver_dim = Color(0xFF736D53) // design-style: fixed-figma-color design-spec.json receiver_dim
}

@Composable
fun HawkinsTheme(content: @Composable () -> Unit) {
    val system = systemColorScheme(LocalContext.current)
    val colors = system.copy(
        fillPrimary = HawkinsColors.fillPrimary,
        fillSecondary = HawkinsColors.fillSecondary,
        fillTertiary = HawkinsColors.fillTertiary,
        fillLight = HawkinsColors.fillLight,
        labelPrimaryLight = HawkinsColors.labelPrimaryLight,
        labelPrimary = HawkinsColors.labelPrimary,
        labelSecondary = HawkinsColors.labelSecondary,
        labelTertiary = HawkinsColors.labelTertiary,
        labelQuaternary = HawkinsColors.labelQuaternary,
        lightenHover = HawkinsColors.lightenHover,
        lightenPressed = HawkinsColors.lightenPressed,
        error = HawkinsColors.error,
        alert = HawkinsColors.alert,
        passable = HawkinsColors.passable,
        interaction = HawkinsColors.interaction,
        dividerLine = HawkinsColors.dividerLine,
    )
    val base = PicoTheme.typography
    val typography = base.copy(
        displaySmall = base.displaySmall.copy(fontFamily = FontFamily.Serif, fontSize = 30.sp, lineHeight = 34.sp, fontWeight = FontWeight(700)),
        titleSmall = base.titleSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight(600)),
        bodyMedium = base.bodyMedium.copy(fontFamily = FontFamily.SansSerif, fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight(400)),
        labelSmall = base.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp, lineHeight = 18.sp, fontWeight = FontWeight(600)),
        headlineLarge = base.headlineLarge.copy(fontFamily = FontFamily.Monospace, fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight(700)),
    )
    PicoTheme(colorScheme = colors, typography = typography, content = content)
}
