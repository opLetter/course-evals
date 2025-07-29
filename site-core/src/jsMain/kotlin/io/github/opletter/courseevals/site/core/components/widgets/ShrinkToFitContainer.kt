package io.github.opletter.courseevals.site.core.components.widgets

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.CSSLengthOrPercentageNumericValue
import com.varabyte.kobweb.compose.css.StyleVariable
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.grid
import com.varabyte.kobweb.compose.ui.modifiers.minWidth
import com.varabyte.kobweb.compose.ui.modifiers.setVariable
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.selectors.children
import com.varabyte.kobweb.silk.style.toModifier
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Div

private val MinWidth by StyleVariable<CSSLengthOrPercentageNumericValue>(0.px)

val ShrinkToFitContainerStyle = CssStyle {
    base {
        Modifier.grid { columns { minmax(MinWidth.value(), maxContent) } }
    }
    children("*") {
        Modifier.minWidth(0.px)
    }
}

/**
 * A wrapper to let an element be as wide as its content, but shrink to fit its container instead of causing parent
 * overflow, while not requiring a fixed width for the parent.
 */
@Composable
fun ShrinkToFitContainer(
    modifier: Modifier = Modifier,
    minWidth: CSSLengthOrPercentageNumericValue? = null,
    content: @Composable () -> Unit,
) {
    Div(
        ShrinkToFitContainerStyle.toModifier()
            .setVariable(MinWidth, minWidth)
            .then(modifier)
            .toAttrs()
    ) {
        content()
    }
}