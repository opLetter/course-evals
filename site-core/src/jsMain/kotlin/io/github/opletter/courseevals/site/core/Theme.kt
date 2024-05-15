package io.github.opletter.courseevals.site.core

import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.graphics.lightened
import com.varabyte.kobweb.compose.ui.modifiers.fontFamily
import com.varabyte.kobweb.compose.ui.modifiers.setVariable
import com.varabyte.kobweb.silk.components.forms.ButtonSize
import com.varabyte.kobweb.silk.components.forms.ButtonVars
import com.varabyte.kobweb.silk.init.InitSilk
import com.varabyte.kobweb.silk.init.InitSilkContext
import com.varabyte.kobweb.silk.init.registerStyleBase
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import com.varabyte.kobweb.silk.theme.colors.palette.background
import com.varabyte.kobweb.silk.theme.colors.palette.button
import com.varabyte.kobweb.silk.theme.colors.palette.color
import com.varabyte.kobweb.silk.theme.colors.palette.link
import com.varabyte.kobweb.silk.theme.modifyStyleBase
import kotlinx.browser.localStorage
import org.jetbrains.compose.web.css.cssRem

private const val COLOR_MODE_KEY = "course-evals:colorMode"

@InitSilk
fun customizeSilk(ctx: InitSilkContext) {
    ctx.theme.modifyStyleBase(ButtonSize.MD) {
        Modifier
            .setVariable(ButtonVars.Height, 2.25.cssRem)
    }
}

@InitSilk
fun updateTheme(ctx: InitSilkContext) {
    ctx.config.initialColorMode = localStorage.getItem(COLOR_MODE_KEY)?.let { ColorMode.valueOf(it) } ?: ColorMode.LIGHT

    ctx.stylesheet.registerStyleBase("body") {
        Modifier.fontFamily("system-ui", "Segoe UI", "Tahoma", "Helvetica", "sans-serif")
    }

    // NOTE: For now, we use ColorMode.LIGHT to represent the default blue theme
    // and ColorMode.DARK to represent the red theme

    // https://coolors.co/palette/2b2d42-8d99ae-edf2f4-ef233c-d90429
    // maybe: https://coolors.co/2b2d42-647890-8d99ae-dce9fa-edf2f4-f7cad0-ef233c-d90429
    val lightButtonBase = Color.rgb(0xD90429)
    val darkButtonBase = Color.rgb(0xEF233C)
    ctx.theme.palettes.light.apply {
        background = Color.rgb(0xEDF2F4)
        color = Colors.Black // Color.rgb(0x2B2D42),
        button.set(
            default = lightButtonBase,
            hover = lightButtonBase.lightened(byPercent = 0.2f),
            focus = Colors.CornflowerBlue,
            pressed = lightButtonBase.lightened(byPercent = 0.3f),
        )
        link.set(
            default = Colors.Blue,
            visited = Color.rgb(123, 0, 21),
        )
    }
    ctx.theme.palettes.dark.apply {
        background = Color.rgb(0xEDF2F4) // Color.rgb(0x2B2D42)
//        color = Colors.Black
        button.set(
            default = darkButtonBase,
            hover = darkButtonBase.darkened(byPercent = 0.2f),
            focus = Colors.LightSkyBlue,
            pressed = darkButtonBase.darkened(byPercent = 0.3f),
        )
        link.set(
            default = Colors.Blue,
            visited = Color.rgb(217, 4, 41), // Color.rgb(123, 0, 21),
        )
    }
}

class SitePalette(
    val accent: Color,
    val neutral: Color,
    val secondary: Color,
    val gridAccent: Color,
)

object SitePalettes {
    private val sitePalettes = mapOf(
        ColorMode.LIGHT to SitePalette(
            accent = Color.rgb(220, 10, 10),
            neutral = Color.rgb(203, 203, 203), // Color.rgb(190, 190, 190)
            secondary = Color.rgb(223, 239, 255), // Color.rgb(0xDCE9FA)
            gridAccent = Color.rgb(76, 76, 187),
        ),
        ColorMode.DARK to SitePalette(
            accent = Color.rgb(0x333333), // Colors.Black,
            neutral = Color.rgb(203, 203, 203),
            secondary = Color.rgb(241, 222, 218),
            gridAccent = Colors.Red.darkened(0.15f),
        ),
    )

    operator fun get(colorMode: ColorMode) = sitePalettes.getValue(colorMode)
}