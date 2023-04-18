package io.github.opletter.courseevals.site.core.components.widgets

import androidx.compose.runtime.Composable
import androidx.compose.web.events.SyntheticMouseEvent
import com.varabyte.kobweb.compose.css.CSSTransition
import com.varabyte.kobweb.compose.css.TransitionTimingFunction
import com.varabyte.kobweb.compose.foundation.layout.BoxScope
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.forms.ButtonStyle
import com.varabyte.kobweb.silk.components.style.active
import com.varabyte.kobweb.silk.components.style.addVariant
import com.varabyte.kobweb.silk.components.style.focusVisible
import com.varabyte.kobweb.silk.components.style.hover
import com.varabyte.kobweb.silk.theme.toSilkPalette
import io.github.opletter.courseevals.site.core.SitePalettes
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.s

// todo: make this private once overrides can be based on original
val basePillButtonModifier = Modifier
    .padding(topBottom = 0.25.cssRem, leftRight = 1.5.cssRem)
    .flexShrink(0)
    .borderRadius(8.px)
    .fontFamily("inherit")
    .styleModifier { property("line-height", "normal") }
    .styleModifier { property("font-size", "inherit") }
    .transition(
        // shamelessly stolen from yt website
        CSSTransition(
            property = "background-color",
            duration = 0.3.s,
            timingFunction = TransitionTimingFunction.cubicBezier(0.05, 0.0, 0.0, 1.0)
        ),
    )

val PillButtonVariant by ButtonStyle.addVariant {
    val baseColor = SitePalettes[colorMode].neutral
    base {
        basePillButtonModifier.backgroundColor(baseColor)
    }
    hover {
        Modifier.backgroundColor(baseColor.darkened(0.1f))
    }
    focusVisible {
        Modifier.boxShadow(spreadRadius = 3.px, color = colorMode.toSilkPalette().button.focus, inset = true)
    }
    active {
        Modifier.backgroundColor(baseColor.darkened(0.2f))
    }
}

val PillButtonSelectedVariant by ButtonStyle.addVariant(Modifier.tabIndex(-1)) {
    val colorModifier = Modifier
        .backgroundColor(SitePalettes[colorMode].accent)
        .color(colorMode.toSilkPalette().background)
    base { basePillButtonModifier.then(colorModifier) }
    hover { colorModifier }
    active { colorModifier }
}

@Composable
fun PillButton(
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: (SyntheticMouseEvent) -> Unit,
    content: @Composable BoxScope.() -> Unit = {},
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        variant = if (selected) PillButtonSelectedVariant else PillButtonVariant,
        content = content
    )
}