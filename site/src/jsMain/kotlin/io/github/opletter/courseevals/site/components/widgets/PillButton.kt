package io.github.opletter.courseevals.site.components.widgets

import androidx.compose.runtime.Composable
import androidx.compose.web.events.SyntheticMouseEvent
import com.varabyte.kobweb.compose.css.CSSTransition
import com.varabyte.kobweb.compose.css.TransitionTimingFunction
import com.varabyte.kobweb.compose.foundation.layout.BoxScope
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.attrsModifier
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.graphics.lightened
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.forms.ButtonStyle
import com.varabyte.kobweb.silk.components.style.active
import com.varabyte.kobweb.silk.components.style.addVariant
import com.varabyte.kobweb.silk.components.style.focus
import com.varabyte.kobweb.silk.components.style.hover
import com.varabyte.kobweb.silk.theme.toSilkPalette
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.s

private val basePillButtonModifier = Modifier
    .padding(topBottom = 0.25.cssRem, leftRight = 1.5.cssRem)
    .borderRadius(1.5.cssRem)
    .fontFamily("inherit")
    .styleModifier { property("line-height", "normal") }
    .styleModifier { property("font-size", "inherit") }
    .transition(CSSTransition("background-color", 0.1.s, TransitionTimingFunction.EaseInOut))

val PillButtonVariant by ButtonStyle.addVariant(Modifier.attrsModifier { attr("type", "button") }) {
    val baseColor = Color.rgb(190, 190, 190)
    base {
        basePillButtonModifier.backgroundColor(baseColor.lightened(0.2f))
    }
    hover {
        Modifier.backgroundColor(baseColor)
    }
    focus {
        Modifier.backgroundColor(baseColor)
    }
    active {
        Modifier.backgroundColor(baseColor.darkened(0.2f))
    }
}

val PillButtonSelectedVariant by ButtonStyle.addVariant(Modifier.attrsModifier { attr("type", "button") }) {
    val colorModifier = Modifier.backgroundColor(colorMode.toSilkPalette().button.default)
    base { basePillButtonModifier.then(colorModifier) }
    hover { colorModifier }
    focus { colorModifier }
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