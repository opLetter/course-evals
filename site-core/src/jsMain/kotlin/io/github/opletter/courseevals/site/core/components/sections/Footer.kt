package io.github.opletter.courseevals.site.core.components.sections

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.AlignSelf
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.dom.ref
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.attrsModifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.alignSelf
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.rowGap
import com.varabyte.kobweb.compose.ui.modifiers.textAlign
import com.varabyte.kobweb.silk.components.icons.fa.FaGithub
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.components.navigation.LinkStyle
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.theme.toSilkPalette
import io.github.opletter.courseevals.site.core.misc.goatCounterClick
import io.github.opletter.courseevals.site.core.misc.jsGoatBindEvents
import org.jetbrains.compose.web.css.cssRem

val FooterStyle by ComponentStyle.base {
    Modifier
        .alignSelf(AlignSelf.Center)
        .textAlign(TextAlign.Center)
        .rowGap(0.1.cssRem)
}

val OppositeLinkVariant by LinkStyle.addVariant {
    val linkColors = colorMode.opposite.toSilkPalette().link
    link {
        Modifier.color(Colors.Cyan)
    }
    visited {
        Modifier.color(linkColors.visited.darkened(0.1f))
    }
}

@Composable
fun Footer(modifier: Modifier = Modifier, linkVariant: ComponentVariant? = null) {
    Column(
        FooterStyle.toModifier().then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        ref = ref { jsGoatBindEvents() }
    ) {
        Link(
            path = "https://forms.gle/4WyzSbUx5Ym5n2NY9",
            text = "Have feedback?",
            variant = linkVariant,
        )
        SpanText("Not affiliated with any university")
        Row(verticalAlignment = Alignment.CenterVertically) {
            FaGithub()
            SpanText(" This site is ")
            Link(
                path = "https://github.com/opLetter/course-evals",
                text = "open source",
                modifier = Modifier.goatCounterClick("gh-link", title = "GitHub Link", referrer = "footer"),
                variant = linkVariant,
            )
        }
        Row {
            SpanText("Made with ")
            Link(
                path = "https://github.com/varabyte/kobweb",
                text = "Kobweb",
                modifier = Modifier
                    .goatCounterClick("kobweb-link", title = "Kobweb Link", referrer = "footer")
                    .attrsModifier { attr("referrerpolicy", "no-referrer") },
                variant = linkVariant,
            )
        }
    }
}