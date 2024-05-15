package io.github.opletter.courseevals.site.core.components.sections.dataPage.options

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.FontVariantCaps
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.thenIf
import com.varabyte.kobweb.silk.components.forms.Checkbox
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.toModifier
import io.github.opletter.courseevals.site.core.states.CampusVM
import io.github.opletter.courseevals.site.core.states.CheckmarksVM
import io.github.opletter.courseevals.site.core.states.LevelOfStudyVM
import org.jetbrains.compose.web.css.FlexWrap
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.dom.Text

@Composable
fun CampusOption(campusState: CampusVM, levelOfStudyState: LevelOfStudyVM) {
    Column(
        ExtraOptionStyle.toModifier(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SpanText("School Filter", Modifier.fontSize(125.percent).fontVariant(caps = FontVariantCaps.SmallCaps))
        Row(
            Modifier
                .fillMaxWidth()
                .flexWrap(FlexWrap.Wrap)
                .columnGap(1.cssRem),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                SpanText("Campus", Modifier.fontSize(115.percent))
                CheckMarkList(campusState, getString = { it.fullName })
            }
            Column(horizontalAlignment = Alignment.Start) {
                SpanText("Level", Modifier.fontSize(115.percent))
                CheckMarkList(levelOfStudyState, getString = { it.fullName })
            }
        }
    }
}

@Composable
private fun <T> CheckMarkList(state: CheckmarksVM<T>, getString: (T) -> String) {
    state.checks.forEach { (data, checked) ->
        // disable clicking on checkmark if it is the only one checked -> good idea?
        val disabled = checked && state.onlyOneChecked
        Checkbox(
            modifier = Modifier.thenIf(
                disabled,
                Modifier
                    .opacity(0.7) // less extreme than default disabled
                    .cursor(Cursor.Default)
                    .title("Select another option to uncheck this one.")
            ),
            checked = checked,
            onCheckedChange = { state.click(data) },
            enabled = !disabled,
            borderColor = Colors.White.copyf(alpha = 0.5f),
        ) { Text(getString(data)) }
    }
}