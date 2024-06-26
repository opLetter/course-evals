package io.github.opletter.courseevals.site.core.components.sections.dataPage.options

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.FontVariantCaps
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.fontVariant
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.toModifier
import io.github.opletter.courseevals.site.core.components.widgets.LabeledSlider
import io.github.opletter.courseevals.site.core.states.MinSemesterVM
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.dom.Text

@Composable
fun MinSemOption(state: MinSemesterVM) {
    Column(
        ExtraOptionStyle.toModifier(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SpanText("Recency Filter", Modifier.fontSize(125.percent).fontVariant(caps = FontVariantCaps.SmallCaps))
        Text("Hide profs with no data since")
        LabeledSlider(
            state.value,
            state.bounds,
            defaultValue = state.default,
            onRelease = { state.setValue(it) },
            getText = { state.getText(it) },
        )
    }
}