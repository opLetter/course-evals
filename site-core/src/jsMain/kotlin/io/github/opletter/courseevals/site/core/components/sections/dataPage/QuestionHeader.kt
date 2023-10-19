package io.github.opletter.courseevals.site.core.components.sections.dataPage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.key
import com.varabyte.kobweb.compose.css.FontVariantCaps
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.Width
import com.varabyte.kobweb.compose.css.functions.min
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.addVariant
import com.varabyte.kobweb.silk.components.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.components.text.SpanTextStyle
import io.github.opletter.courseevals.site.core.components.widgets.CustomDropDown
import io.github.opletter.courseevals.site.core.components.widgets.SelectStyle
import io.github.opletter.courseevals.site.core.misc.jsBalanceTextById
import io.github.opletter.courseevals.site.core.states.Questions
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px

val HeaderStyle by ComponentStyle {
    base { Modifier.width(95.percent) }
    Breakpoint.LG { Modifier.width(70.percent) }
}

val SpanTextHeaderVariant by SpanTextStyle.addVariant {
    base {
        Modifier
            .fontWeight(FontWeight.Bold)
            .fontVariant(caps = FontVariantCaps.SmallCaps)
            .fontSize(1.5.cssRem)
            .lineHeight(1.5.cssRem)
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

val QuestionSelectVariant by SelectStyle.addVariant {
    base {
        Modifier
            .fontSize(1.cssRem)
            .width(min(400.px, 90.percent))
    }
    Breakpoint.MD {
        Modifier.width(Width.Unset)
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
        key(selectedQ) { // needed for jsBalanceTextById() to work upon text change
            val id = "survey-q"
            Box(HeaderStyle.toModifier(), Alignment.Center) {
                SpanText("\"${questions.full[selectedQ]}\"", Modifier.id(id), SpanTextHeaderVariant)
                SideEffect {
                    jsBalanceTextById(id)
                }
            }
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