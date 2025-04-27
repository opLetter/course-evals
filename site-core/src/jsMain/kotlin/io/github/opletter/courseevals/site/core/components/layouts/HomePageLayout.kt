package io.github.opletter.courseevals.site.core.components.layouts

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.PageContext
import com.varabyte.kobweb.core.data.addIfAbsent
import com.varabyte.kobweb.core.data.getValue
import com.varabyte.kobweb.core.init.InitRoute
import com.varabyte.kobweb.core.init.InitRouteContext
import com.varabyte.kobweb.core.layout.Layout
import com.varabyte.kobweb.silk.style.toModifier
import io.github.opletter.courseevals.site.core.components.sections.Footer
import io.github.opletter.courseevals.site.core.components.sections.OppositeLinkVariant
import io.github.opletter.courseevals.site.core.components.sections.dataPage.MainNavStyle
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.fr
import org.jetbrains.compose.web.css.percent

class HomePageData(val produceModifier: () -> Modifier = { Modifier })

@InitRoute
fun initHomePageLayout(ctx: InitRouteContext) {
    ctx.data.addIfAbsent { HomePageData() }
}

@Layout
@Composable
fun HomePageLayout(ctx: PageContext, content: @Composable () -> Unit) {
    Box(
        MainNavStyle.toModifier()
            .fillMaxWidth()
            .minHeight(100.percent)
            .padding(top = 0.5.cssRem)
            .gridTemplateRows { size(1.fr); size(auto) }
            .then(ctx.data.getValue<HomePageData>().produceModifier())
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