package io.github.opletter.courseevals.site.core.components.widgets

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.browser.dom.clearFocus
import com.varabyte.kobweb.compose.css.FontVariantCaps
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.dom.ref
import com.varabyte.kobweb.compose.dom.registerRefScope
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.style.ComponentKind
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.CssStyleVariant
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import com.varabyte.kobweb.silk.theme.colors.palette.color
import com.varabyte.kobweb.silk.theme.colors.palette.toPalette
import io.github.opletter.courseevals.site.core.SitePalettes
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Option
import org.jetbrains.compose.web.dom.Select
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLOptionElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.get

interface SelectKind : ComponentKind

val SelectStyle = CssStyle<SelectKind> {
    base {
        Modifier
            .padding(4.px)
            .borderRadius(6.px)
            .border(width = 0.px)
            .color(ColorMode.LIGHT.toPalette().color)
            .backgroundColor(SitePalettes[colorMode].secondary)
            .fontFamily("inherit") // this is not the default for some reason
            .fontWeight(FontWeight.Bold)
            .fontVariant(caps = FontVariantCaps.SmallCaps)
    }
    cssRule("option") {
        Modifier.fontWeight(FontWeight.Normal)
    }
}

@Composable
fun <T> CustomDropDown(
    list: Collection<T>,
    onSelect: (String) -> Unit,
    hint: T? = null,
    selectModifier: Modifier = Modifier,
    selectVariant: CssStyleVariant<SelectKind>? = null,
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
        ref(selected, list) { selectElement: HTMLSelectElement ->
            val index = list.indexOfFirst { selected == getValue(it) }
                .takeIf { it >= 0 && hint == null } ?: 0
            (selectElement.options[index] as HTMLOptionElement).selected = true
        }.let { registerRefScope(it) }
        hint?.let {
            Option("none", optionModifier.hidden().disabled().toAttrs()) {
                Text(getText(it))
            }
        }
        list.forEach {
            Option(
                getValue(it),
                optionModifier.toAttrs(),
            ) { Text(getText(it)) }
        }
    }
}