package io.github.opletter.courseevals.site.core.components.widgets

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.CSSTransition
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.TransitionTimingFunction
import com.varabyte.kobweb.compose.css.functions.RadialGradient
import com.varabyte.kobweb.compose.css.functions.radialGradient
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.components.text.SpanText
import io.github.opletter.courseevals.site.core.misc.jsFormatNum
import io.github.opletter.courseevals.site.core.misc.smallCapsFont
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Text

val BarGraphStyle by ComponentStyle {
    base {
        Modifier
            .styleModifier { property("aspect-ratio", "4 / 3") }
            .padding(topBottom = 0.33.cssRem, leftRight = 0.5.cssRem)
            .borderRadius(12.px)
            .color(Colors.White)
//            .backgroundImage(
//                radialGradient(
//                    RadialGradient.Shape.Circle,
//                    rgb(14, 14, 42),
//                    rgb(50, 57, 84),
//                    CSSPosition(Edge.CenterX, Edge.Bottom),
//                )
//            )
            .backgroundImage(
                radialGradient(
                    RadialGradient.Shape.Circle,
                    rgb(41, 41, 46),
                    rgb(25, 25, 28)
                )
            )
    }
    Breakpoint.LG {
        Modifier
            .styleModifier { property("aspect-ratio", "3.5 / 3") }
    }
    Breakpoint.XL {
        Modifier
            .styleModifier { property("aspect-ratio", "4 / 3") }
    }
}

val BarGraphBarStyle by ComponentStyle {
    val barColor = Color.rgb(250, 250, 250)
    base {
        Modifier
            .width(75.percent)
            .backgroundColor(barColor)
            .borderBottom(1.px, LineStyle.Solid, barColor) // needed for when num is 0
            .transition(CSSTransition("height", 0.3.s, TransitionTimingFunction.EaseOut))
    }
}

@Composable
fun BarGraph(
    ratings: List<Int>,
    label: String,
    modifier: Modifier, // should have either height or width set & font size
    max: Int = ratings.maxOrNull() ?: 0,
) {
    var barAnimHeight by remember { mutableStateOf(0.percent) }
    var mouseOver: Boolean by remember { mutableStateOf(false) }

    val labelTextColor = Color.rgb(202, 0, 0)

    Column(BarGraphStyle.toModifier().then(modifier)) {
        SpanText(
            label,
            Modifier
                .margin(left = 0.75.cssRem)
                .color(labelTextColor)
                .fontSize(175.percent)
                .fontWeight(FontWeight.Bolder)
                .smallCapsFont()
        )

        Row(Modifier.fillMaxWidth().flexGrow(1)) {
            ratings.forEachIndexed { index, num ->
                Column(
                    Modifier.fillMaxHeight().flex(1),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    val barHeight = num.toDouble() / max * barAnimHeight
                    Box(Modifier.height(100.percent - barHeight)) // needed so that real bar is correct height
                    Text(
                        if (mouseOver) num.toString()
                        else jsFormatNum(num = num.toDouble() / ratings.sum() * 100, decDigits = 0).let { "$it%" }
                    )
                    Box(
                        BarGraphBarStyle.toModifier()
                            .height(barHeight)
                            .onMouseEnter { mouseOver = true }
                            .onMouseLeave { mouseOver = false }
                    )
                    SpanText(
                        (index + 1).toString(),
                        Modifier
                            .fontSize(133.percent)
                            .fontWeight(FontWeight.Bolder)
                            .color(labelTextColor)
                    )
                }
            }
        }
    }
    SideEffect { // needed for animation to run AFTER initial recomposition
        barAnimHeight = 100.percent
    }
}
