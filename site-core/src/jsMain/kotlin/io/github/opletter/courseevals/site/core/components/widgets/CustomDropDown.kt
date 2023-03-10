package io.github.opletter.courseevals.site.core.components.widgets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.dom.clearFocus
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.ComponentVariant
import com.varabyte.kobweb.silk.components.style.base
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import com.varabyte.kobweb.silk.theme.toSilkPalette
import io.github.opletter.courseevals.site.core.misc.smallCapsFont
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.vmin
import org.jetbrains.compose.web.dom.Option
import org.jetbrains.compose.web.dom.Select
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLOptionElement
import org.w3c.dom.get

val SelectStyle by ComponentStyle.base {
    val background = if (colorMode == ColorMode.LIGHT) Color.rgb(0xDCE9FA) else Color.rgb(241, 222, 218)

    Modifier
        .padding(4.px)
        .borderRadius(1.vmin)
        .border(width = 0.px)
        .color(ColorMode.LIGHT.toSilkPalette().color)
        .backgroundColor(background)
        .fontFamily("inherit") // this is not the default for some reason
        .fontWeight(FontWeight.Bolder)
        .smallCapsFont()
}

@Composable
fun <T> CustomDropDown(
    list: Collection<T>,
    onSelect: (String) -> Unit,
    hint: T? = null,
    selectModifier: Modifier = Modifier,
    selectVariant: ComponentVariant? = null,
    optionModifier: Modifier = Modifier,
    getText: (T) -> String = { it.toString() },
    getValue: (T) -> String = getText,
    selected: String? = null,
) {
    Select(
        SelectStyle.toModifier(selectVariant)
            .then(selectModifier)
            .toAttrs {
                onChange {
                    it.target.clearFocus()
                    onSelect(it.value ?: error("No value selected"))
                    if (hint != null) (it.target.options[0] as HTMLOptionElement).selected = true
                }
            }
    ) {
        DisposableEffect(selected, list) {
            val index = list.indexOfFirst { selected == getValue(it) }
                .takeIf { it >= 0 && hint == null } ?: 0
            (scopeElement.options[index] as HTMLOptionElement).selected = true
            onDispose { }
        }
        hint?.let {
            Option(
                "none",
                optionModifier.toAttrs() {
                    disabled()
                    hidden()
                },
            ) { Text(getText(it)) }
        }
        list.forEach {
            Option(
                getValue(it),
                optionModifier.toAttrs(),
            ) { Text(getText(it)) }
        }
    }
}