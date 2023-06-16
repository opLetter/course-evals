package io.github.opletter.courseevals.site.core.components.sections.dataPage.options

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.components.text.SpanText
import io.github.opletter.courseevals.site.core.components.widgets.LabeledSlider
import io.github.opletter.courseevals.site.core.misc.smallCapsFont
import io.github.opletter.courseevals.site.core.states.MinSemesterVM
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.dom.Text

@Composable
fun MinSemOption(state: MinSemesterVM) {
    Column(
        ExtraOptionStyle.toModifier(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SpanText("Recency Filter", Modifier.fontSize(125.percent).smallCapsFont())
        Text("Hide profs with no data since")
        val initialValue = remember { state.value }
        LabeledSlider(
            initialValue,
            state.bounds,
            defaultValue = state.default,
            onRelease = { state.setValue(it) },
            getText = { state.getText(it) },
        )
    }
}