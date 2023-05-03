package io.github.opletter.courseevals.site.core.components.layouts

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.BoxScope
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.style.toModifier
import io.github.opletter.courseevals.site.core.components.sections.Footer
import io.github.opletter.courseevals.site.core.components.sections.OppositeLinkVariant
import io.github.opletter.courseevals.site.core.components.sections.dataPage.MainNavStyle
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.percent

@Composable
fun HomePageLayout(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        MainNavStyle.toModifier()
            .fillMaxWidth()
            .minHeight(100.percent)
            .padding(top = 0.5.cssRem)
            .gridTemplateRows("1fr auto")
            .then(modifier)
    ) {
        content()
        // Associate the footer with the row that will get pushed off the bottom of the page if it can't fit.
        Footer(
            Modifier
                .margin(topBottom = 1.cssRem)
                .align(Alignment.Center)
                .gridRow(2, 3),
            OppositeLinkVariant,
        )
    }
}