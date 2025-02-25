package io.github.opletter.courseevals.site.core.components.sections.dataPage

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.FontVariantCaps
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.functions.min
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.components.text.SpanTextStyle
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.addVariant
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.style.until
import io.github.opletter.courseevals.site.core.components.widgets.CustomDropDown
import io.github.opletter.courseevals.site.core.components.widgets.SelectStyle
import io.github.opletter.courseevals.site.core.states.Questions
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px

val HeaderStyle = CssStyle {
    base { Modifier.width(95.percent) }
    Breakpoint.LG { Modifier.width(70.percent) }
}

val SpanTextHeaderVariant = SpanTextStyle.addVariant {
    base {
        Modifier
            .fontWeight(FontWeight.Bold)
            .fontVariant(caps = FontVariantCaps.SmallCaps)
            .fontSize(1.5.cssRem)
            .lineHeight(1.5.cssRem)
            .styleModifier { property("text-wrap", "balance") }
    }
    Breakpoint.MD {
        Modifier
            .fontSize(1.75.cssRem)
            .lineHeight(2.cssRem)
    }
    Breakpoint.LG {
        Modifier
            .fontSize(2.cssRem)
            .lineHeight(2.25.cssRem)
    }
}

val QuestionSelectVariant = SelectStyle.addVariant {
    base {
        Modifier.fontSize(1.cssRem)
    }
    until(Breakpoint.MD) {
        Modifier.width(min(400.px, 90.percent))
    }
}

@Composable
fun QuestionHeader(
    questions: Questions,
    selectedQ: Int,
    modifier: Modifier = Modifier,
    onSelectedQDropDownChange: (Int) -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .rowGap(0.5.cssRem)
            .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(HeaderStyle.toModifier(), Alignment.Center) {
            SpanText("\"${questions.full[selectedQ]}\"", variant = SpanTextHeaderVariant)
        }
        CustomDropDown(
            hint = "Choose another question...",
            selectModifier = Modifier.ariaLabel("question"),
            selectVariant = QuestionSelectVariant,
            list = questions.short,
            onSelect = { onSelectedQDropDownChange(questions.short.indexOf(it)) },
        )
    }
}