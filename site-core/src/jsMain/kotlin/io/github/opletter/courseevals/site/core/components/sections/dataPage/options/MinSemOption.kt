package io.github.opletter.courseevals.site.core.components.sections.dataPage.options

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.icons.fa.FaRotateRight
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.components.text.SpanText
import io.github.opletter.courseevals.site.core.misc.smallCapsFont
import io.github.opletter.courseevals.site.core.states.MinSemesterVM
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.dom.RangeInput
import org.jetbrains.compose.web.dom.Text

@Composable
fun MinSemOption(state: MinSemesterVM) {
    Column(
        ExtraOptionStyle.toModifier(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SpanText("Recency Filter", Modifier.fontSize(125.percent).smallCapsFont())
        Text("Hide profs with no data since")
        Row(
            Modifier
                .fillMaxWidth()
                .columnGap(0.5.cssRem),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer()
            RangeInput(
                value = state.rangeValue,
                min = state.bounds.first,
                max = state.bounds.second,
                attrs = {
                    // live update range, but only change data on release
                    onInput { state.setRangeValue(it.value) }
                    onChange { state.setValue(it.value) }
                }
            )
            if (state.showResetButton) {
                FaRotateRight(
                    Modifier
                        .flex(1)
                        .cursor(Cursor.Pointer)
                        .onClick { state.reset() }
                )
            } else Spacer() // to keep slider centered
        }
        Text(state.text)
    }
}