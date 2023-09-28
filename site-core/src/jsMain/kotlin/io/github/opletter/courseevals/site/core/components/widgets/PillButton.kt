package io.github.opletter.courseevals.site.core.components.widgets

import androidx.compose.runtime.Composable
import androidx.compose.web.events.SyntheticMouseEvent
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.foundation.layout.RowScope
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.forms.ButtonStyle
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.theme.colors.palette.background
import com.varabyte.kobweb.silk.theme.colors.palette.button
import com.varabyte.kobweb.silk.theme.colors.palette.toPalette
import io.github.opletter.courseevals.site.core.SitePalettes
import io.github.opletter.courseevals.site.core.components.style.SmediumButtonSize
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.s

val PillButtonVariant by ButtonStyle.addVariant {
    val baseColor = SitePalettes[colorMode].neutral
    base {
        Modifier
            .padding(topBottom = 0.25.cssRem, leftRight = 1.5.cssRem)
            .flexShrink(0)
            .borderRadius(8.px)
            .fontWeight(FontWeight.Normal)
            .fontFamily("unset")
            .lineHeight(LineHeight.Unset)
            .fontSize(FontSize.Unset)
            .transition(
                // shamelessly stolen from yt website
                CSSTransition(
                    property = "background-color",
                    duration = 0.3.s,
                    timingFunction = TransitionTimingFunction.cubicBezier(0.05, 0.0, 0.0, 1.0)
                ),
            ).backgroundColor(baseColor)
    }
    hover {
        Modifier.backgroundColor(baseColor.darkened(0.1f))
    }
    focusVisible {
        Modifier.boxShadow(spreadRadius = 3.px, color = colorMode.toPalette().button.focus, inset = true)
    }
    active {
        Modifier.backgroundColor(baseColor.darkened(0.2f))
    }
}

val PillButtonSelectedVariant by ButtonStyle.addVariant(
    extraModifiers = { ButtonStyle.toModifier(PillButtonVariant).tabIndex(-1) }
) {
    val colorModifier = Modifier
        .backgroundColor(SitePalettes[colorMode].accent)
        .color(colorMode.toPalette().background)
    base { colorModifier }
    hover { colorModifier }
    active { colorModifier }
}

@Composable
fun PillButton(
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: (SyntheticMouseEvent) -> Unit,
    content: @Composable RowScope.() -> Unit = {},
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        variant = if (selected) PillButtonSelectedVariant else PillButtonVariant,
        size = SmediumButtonSize,
        content = content
    )
}