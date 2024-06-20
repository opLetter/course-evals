package io.github.opletter.courseevals.site.core.components.widgets

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.Transition
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.RowScope
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.icons.fa.FaRotateRight
import io.github.opletter.courseevals.site.core.components.style.UnsetButtonSize
import io.github.opletter.courseevals.site.core.components.style.UnstyledButtonVariant
import org.jetbrains.compose.web.attributes.ButtonType
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.fr
import org.jetbrains.compose.web.css.ms
import org.jetbrains.compose.web.dom.RangeInput
import org.jetbrains.compose.web.dom.Text

@Composable
fun LabeledSlider(
    rangeValue: Number,
    bounds: Pair<Number, Number>,
    step: Number = 1,
    onRelease: (Number) -> Unit,
    modifier: Modifier = Modifier,
    defaultValue: Number = rangeValue,
    onSlide: (Number) -> Unit = {},
    getText: (Number) -> String = { it.toString() },
    resetContent: (@Composable RowScope.() -> Unit)? = { FaRotateRight() },
) {
    var visualRangeValue by remember { mutableStateOf(rangeValue) }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Slider(
            rangeValue = rangeValue,
            bounds = bounds,
            step = step,
            onRelease = onRelease,
            modifier = Modifier.fillMaxWidth(),
            defaultValue = defaultValue,
            onSlide = {
                visualRangeValue = it
                onSlide(it)
            },
            resetContent = resetContent,
        )
        Text(getText(visualRangeValue))
    }
}

@Composable
fun Slider(
    rangeValue: Number,
    bounds: Pair<Number, Number>,
    step: Number = 1,
    onRelease: (Number) -> Unit,
    modifier: Modifier = Modifier,
    defaultValue: Number = rangeValue,
    onSlide: (Number) -> Unit = {},
    resetContent: (@Composable RowScope.() -> Unit)? = { FaRotateRight() },
) {
    var visualRangeValue by remember { mutableStateOf(rangeValue) }
    Box(
        Modifier
            .gridTemplateColumns { size(1.fr); size(auto); size(1.fr) }
            .columnGap(0.5.cssRem)
            .then(modifier),
        contentAlignment = Alignment.Center,
    ) {
        RangeInput(
            value = visualRangeValue,
            min = bounds.first,
            max = bounds.second,
            step = step,
            attrs = Modifier.gridColumnStart(2).toAttrs {
                onInput {
                    visualRangeValue = it.value!!
                    onSlide(it.value!!)
                }
                onChange { onRelease(visualRangeValue) }
            }
        )
        ClosableTransitionObject(
            open = resetContent != null && rangeValue != defaultValue,
            openModifier = Modifier.opacity(1),
            closedModifier = Modifier.opacity(0.4), // purposely start partially visible to avoid seeming laggy
        ) {
            Button(
                onClick = {
                    visualRangeValue = defaultValue
                    onSlide(defaultValue)
                    onRelease(defaultValue)
                },
                Modifier
                    .gridColumnStart(3)
                    .transition(Transition.of("opacity", 150.ms))
                    .ariaLabel("reset")
                    .then(it),
                variant = UnstyledButtonVariant,
                type = ButtonType.Reset,
                size = UnsetButtonSize,
            ) {
                resetContent!!()
            }
        }
    }
}