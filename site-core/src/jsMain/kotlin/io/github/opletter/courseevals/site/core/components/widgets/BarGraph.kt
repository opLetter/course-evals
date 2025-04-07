package io.github.opletter.courseevals.site.core.components.widgets

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.css.functions.RadialGradient
import com.varabyte.kobweb.compose.css.functions.min
import com.varabyte.kobweb.compose.css.functions.radialGradient
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.thenIf
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.components.text.SpanTextStyle
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.addVariantBase
import com.varabyte.kobweb.silk.style.base
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.theme.colors.palette.background
import com.varabyte.kobweb.silk.theme.colors.palette.toPalette
import io.github.opletter.courseevals.site.core.SitePalettes
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Text
import kotlin.math.roundToInt

val BarGraphStyle = CssStyle {
    val backgroundGradient = if (colorMode.isLight) {
        radialGradient(
            Color.rgb(14, 14, 42), // rgb(41, 41, 46)
            Color.rgb(50, 57, 84), // rgb(25, 25, 28)
            RadialGradient.Shape.Circle,
            CSSPosition(Edge.Bottom) // none
        )
    } else {
        radialGradient(
            Color.rgb(186, 79, 69),
            Color.rgb(255, 96, 63),
            RadialGradient.Shape.Circle,
            CSSPosition(Edge.Bottom)
        )
    }

    base {
        Modifier
            .fillMaxWidth()
            .fontSize(min(4.1.vw, 1.2.cssRem))
            .aspectRatio(4, 3)
            .padding(topBottom = 0.33.cssRem, leftRight = 0.75.cssRem)
            .borderRadius(12.px)
            .color(colorMode.toPalette().background)
            .backgroundImage(backgroundGradient)
    }
    Breakpoint.LG {
        Modifier
            .width(Width.Unset)
            .fontSize(1.5.cssRem)
            .padding(topBottom = 0.33.cssRem, leftRight = 1.cssRem)
            .aspectRatio(3.5, 3)
    }
    Breakpoint.XL {
        Modifier.aspectRatio(4, 3)
    }
}

val BarGraphBarStyle = CssStyle.base {
    val barColor = colorMode.toPalette().background
    Modifier
        .width(75.percent)
        .backgroundColor(barColor)
        .borderBottom(1.px, LineStyle.Solid, barColor) // needed for when num is 0
}

val BarGraphLabelVariant = SpanTextStyle.addVariantBase {
    Modifier
        .color(SitePalettes[colorMode].accent)
        .fontWeight(FontWeight.Bold)
}

@Composable
fun BarGraph(
    ratings: List<Int>,
    label: String,
    modifier: Modifier = Modifier,
    max: Int = ratings.maxOrNull() ?: 0,
) {
    var barAnimFactor by remember { mutableStateOf(0) }
    var mouseOver: Boolean by remember { mutableStateOf(false) }

    Column(BarGraphStyle.toModifier().then(modifier)) {
        SpanText(
            label,
            Modifier
                .fontSize(175.percent)
                .fontVariant(caps = FontVariantCaps.SmallCaps),
            BarGraphLabelVariant,
        )

        Row(Modifier.fillMaxWidth().flexGrow(1)) {
            ratings.forEachIndexed { index, num ->
                Column(
                    Modifier.fillMaxHeight().flex(1),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    val transition = Transition.of("flex-grow", 0.3.s, TransitionTimingFunction.EaseOut)
                    val barWeight = num.toDouble() / max * barAnimFactor
                    Box(Modifier.flexGrow(1 - barWeight).transition(transition))
                    Text(
                        if (mouseOver) num.toString()
                        else (num.toDouble() / ratings.sum() * 100).roundToInt().let { "$it%" }
                    )
                    Box(
                        BarGraphBarStyle.toModifier()
                            .flexGrow(barWeight)
                            .transition(transition)
                            .thenIf(barWeight > 0, Modifier.borderBottom(width = 0.px))
                            .onMouseEnter { mouseOver = true }
                            .onMouseLeave { mouseOver = false }
                    )
                    SpanText(
                        (index + 1).toString(),
                        Modifier.fontSize(133.percent),
                        BarGraphLabelVariant
                    )
                }
            }
        }
    }
    LaunchedEffect(Unit) { // needed for animation to run AFTER initial recomposition
        barAnimFactor = 1
    }
}