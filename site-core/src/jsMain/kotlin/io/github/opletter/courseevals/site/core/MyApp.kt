package io.github.opletter.courseevals.site.core

import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.graphics.lightened
import com.varabyte.kobweb.compose.ui.modifiers.fontFamily
import com.varabyte.kobweb.silk.components.style.breakpoint.BreakpointSizes
import com.varabyte.kobweb.silk.init.InitSilk
import com.varabyte.kobweb.silk.init.InitSilkContext
import com.varabyte.kobweb.silk.init.registerBaseStyle
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import com.varabyte.kobweb.silk.theme.colors.MutableSilkPalette
import com.varabyte.kobweb.silk.theme.colors.MutableSilkPalettes
import kotlinx.browser.localStorage
import org.jetbrains.compose.web.css.cssRem

private const val COLOR_MODE_KEY = "course-evals:colorMode"

@InitSilk
fun updateTheme(ctx: InitSilkContext) {
    ctx.config.initialColorMode = localStorage.getItem(COLOR_MODE_KEY)?.let { ColorMode.valueOf(it) } ?: ColorMode.LIGHT

    ctx.stylesheet.registerBaseStyle("body") {
        Modifier.fontFamily("Segoe UI", "Helvetica", "Tahoma", "sans-serif")
    }

    // Breakpoints defined where layout breaks
    // SideNav is MD+, while LG & XL are used
    ctx.theme.breakpoints = BreakpointSizes(
        sm = 30.cssRem,
        md = 48.cssRem,
        lg = 73.cssRem,
        xl = 79.cssRem,
    )

    // https://coolors.co/palette/2b2d42-8d99ae-edf2f4-ef233c-d90429
    // maybe: https://coolors.co/2b2d42-647890-8d99ae-dce9fa-edf2f4-f7cad0-ef233c-d90429
    val lightButtonBase = Color.rgb(0xD90429)
    val darkButtonBase = Color.rgb(0xEF233C)
    ctx.theme.palettes = MutableSilkPalettes(
        light = MutableSilkPalette(
            background = Color.rgb(0xEDF2F4),
            color = Colors.Black,//Color.rgb(0x2B2D42),
            link = MutableSilkPalette.Link(
                default = Colors.Blue,
                visited = Color.rgb(123, 0, 21),
            ),
            button = MutableSilkPalette.Button(
                default = lightButtonBase,
                hover = lightButtonBase.lightened(byPercent = 0.2f),
                focus = Colors.CornflowerBlue,
                pressed = lightButtonBase.lightened(byPercent = 0.3f),
            ),
            border = Color.rgb(76, 76, 187),
        ),
        dark = MutableSilkPalette(
            background = Color.rgb(0x2B2D42),
            color = Colors.Black,
            link = MutableSilkPalette.Link(
                default = Colors.Cyan,
                visited = Color.rgb(217, 4, 41),
            ),
            button = MutableSilkPalette.Button(
                default = darkButtonBase,
                hover = darkButtonBase.darkened(byPercent = 0.2f),
                focus = Colors.LightSkyBlue,
                pressed = darkButtonBase.darkened(byPercent = 0.3f),
            ),
            border = Colors.Red.darkened(0.15f),
        )
    )
}

class SitePalette(
    val accent: Color,
    val neutral: Color,
)

object SitePalettes {
    private val sitePalettes = mapOf(
        ColorMode.LIGHT to SitePalette(
            accent = Color.rgb(220, 10, 10),
            neutral = Color.rgb(203, 203, 203), // Color.rgb(190, 190, 190)
        ),
        ColorMode.DARK to SitePalette(
            accent = Colors.White,
            neutral = Color.rgb(203, 203, 203),
        ),
    )

    operator fun get(colorMode: ColorMode) = sitePalettes.getValue(colorMode)
}