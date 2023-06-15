package io.github.opletter.courseevals.site.core.components.widgets

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.BoxScope
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.forms.ButtonStyle
import com.varabyte.kobweb.silk.components.icons.fa.FaRotateRight
import com.varabyte.kobweb.silk.components.style.active
import com.varabyte.kobweb.silk.components.style.addVariant
import com.varabyte.kobweb.silk.components.style.hover
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.ms
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.RangeInput
import org.jetbrains.compose.web.dom.Text

val UnstyledButtonVariant by ButtonStyle.addVariant {
    base {
        Modifier
            .color(CSSColor.Unset)
            .backgroundColor(BackgroundColor.Unset)
            .lineHeight(LineHeight.Unset)
            .fontSize(FontSize.Unset)
            .padding(0.px)
            .borderRadius(0.px)
    }
    hover {
        Modifier.backgroundColor(BackgroundColor.Unset)
    }
    active {
        Modifier.backgroundColor(BackgroundColor.Unset)
    }
}

@Composable
fun LabeledSlider(
    startValue: Number,
    bounds: Pair<Number, Number>,
    step: Number = 1,
    onRelease: (Number) -> Unit,
    modifier: Modifier = Modifier,
    onSlide: (Number) -> Unit = {},
    onReset: (() -> Unit)? = null,
    getText: (Number) -> String = { it.toString() },
    resetContent: @Composable BoxScope.() -> Unit = { FaRotateRight() },
) {
    var rangeValue by remember { mutableStateOf(startValue) }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Slider(
            startValue,
            bounds,
            step,
            onRelease,
            onSlide = {
                rangeValue = it
                onSlide(it)
            },
            onReset = onReset?.let {
                {
                    rangeValue = startValue
                    it()
                }
            },
            resetContent = resetContent,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(getText(rangeValue))
    }
}

@Composable
fun Slider(
    startValue: Number,
    bounds: Pair<Number, Number>,
    step: Number = 1,
    onRelease: (Number) -> Unit,
    modifier: Modifier = Modifier,
    onSlide: (Number) -> Unit = {},
    onReset: (() -> Unit)? = null,
    resetContent: @Composable BoxScope.() -> Unit = { FaRotateRight() },
) {
    var rangeValue by remember { mutableStateOf(startValue) }
    var releaseValue by remember { mutableStateOf(startValue) } // used to determine if we should show the reset button
    Box(
        Modifier
            .gridTemplateColumns("1fr auto 1fr")
            .columnGap(0.5.cssRem)
            .then(modifier),
        contentAlignment = Alignment.Center,
    ) {
        RangeInput(
            value = rangeValue,
            min = bounds.first,
            max = bounds.second,
            step = step,
            attrs = Modifier.gridColumn("2").toAttrs {
                onInput {
                    rangeValue = it.value!!
                    onSlide(it.value!!)
                }
                onChange {
                    releaseValue = rangeValue
                    onRelease(rangeValue)
                }
            }
        )
        ClosableTransitionObject(
            open = onReset != null && releaseValue != startValue,
            openModifier = Modifier.opacity(1),
            closedModifier = Modifier.opacity(0),
        ) {
            Button(
                onClick = {
                    rangeValue = startValue
                    releaseValue = startValue
                    onReset!!()
                },
                Modifier
                    .gridColumn("3")
                    .transition(CSSTransition("opacity", 150.ms))
                    .then(it),
                variant = UnstyledButtonVariant,
            ) {
                resetContent()
            }
        }
    }
}