package io.github.opletter.courseevals.site.components.sections.dataPage.options

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.VerticalAlign
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.thenIf
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.components.text.SpanText
import io.github.opletter.courseevals.site.misc.smallCapsFont
import io.github.opletter.courseevals.site.states.CampusVM
import io.github.opletter.courseevals.site.states.CheckmarksVM
import io.github.opletter.courseevals.site.states.LevelOfStudyVM
import org.jetbrains.compose.web.css.FlexWrap
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.dom.CheckboxInput
import org.jetbrains.compose.web.dom.Label

@Composable
fun CampusOption(campusState: CampusVM, levelOfStudyState: LevelOfStudyVM) {
    Column(
        ExtraOptionStyle.toModifier(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SpanText(
            "School Filter",
            Modifier
                .fontSize(125.percent)
                .smallCapsFont()
        )
        Row(
            Modifier
                .fillMaxWidth()
                .flexWrap(FlexWrap.Wrap)
                .columnGap(1.cssRem),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                SpanText("Campus", Modifier.fontSize(115.percent))
                CheckMarkList(campusState) { it.fullName }
            }
            Column(horizontalAlignment = Alignment.Start) {
                SpanText("Level", Modifier.fontSize(115.percent))
                CheckMarkList(levelOfStudyState) { it.fullName }
            }
        }
    }
}

@Composable
private fun <T> CheckMarkList(state: CheckmarksVM<T>, getString: (T) -> String) {
    state.checks.forEach { (data, checked) ->
        // disable clicking on checkmark if it is the only one checked -> good idea?
        val disabled = checked && state.onlyOneChecked
        Label(
            attrs = Modifier.thenIf(
                disabled,
                Modifier.title("Select another option to uncheck this one.")
            ).toAttrs()
        ) {
            CheckboxInput(
                checked,
                Modifier
                    .verticalAlign(VerticalAlign.Bottom)
                    .thenIf(disabled, Modifier.disabled())
                    .toAttrs {
                        onInput { state.click(data) }
                    }
            )
            SpanText(getString(data), Modifier.margin(left = 0.5.cssRem))
        }
    }
}