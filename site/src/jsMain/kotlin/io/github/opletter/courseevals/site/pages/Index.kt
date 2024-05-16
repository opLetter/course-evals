package io.github.opletter.courseevals.site.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.components.navigation.UndecoratedLinkVariant
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.extendedBy
import com.varabyte.kobweb.silk.style.selectors.hover
import com.varabyte.kobweb.silk.style.selectors.link
import com.varabyte.kobweb.silk.style.selectors.visited
import com.varabyte.kobweb.silk.style.toModifier
import io.github.opletter.courseevals.site.core.components.layouts.HomePageLayout
import io.github.opletter.courseevals.site.core.components.sections.dataPage.options.DarkBackgroundBoxStyle
import io.github.opletter.courseevals.site.core.components.widgets.LogoWithSubhead
import io.github.opletter.courseevals.site.core.misc.College
import kotlinx.browser.document
import org.jetbrains.compose.web.css.cssRem

val SubpageLinkVariant = UndecoratedLinkVariant.extendedBy {
    link {
        Modifier.color(Colors.White)
    }
    visited {
        Modifier.color(Colors.White)
    }
    hover {
        Modifier.color(Colors.DarkRed)
    }
}

@Page
@Composable
fun HomePage() {
    remember {
        document.title = "EVALS"
    }

    HomePageLayout {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LogoWithSubhead()

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SpanText(
                    text = "Select a school",
                    modifier = Modifier
                        .fontSize(2.cssRem)
                        .fontWeight(FontWeight.Bold)
                        .margin(top = 2.5.cssRem)
                )
                Column(
                    DarkBackgroundBoxStyle.toModifier()
                        .margin(topBottom = 0.5.cssRem)
                        .textAlign(TextAlign.Center)
                        .fontSize(1.5.cssRem)
                        .fontWeight(FontWeight.Bold)
                        .padding(1.5.cssRem)
                        .rowGap(1.cssRem),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    listOf(College.TXST, College.FSU, College.USF).sortedBy { it.fullName }.forEach { college ->
                        Link(path = college.urlPath, variant = SubpageLinkVariant) {
                            SpanText(college.fullName)
                        }
                    }
                }
            }
            Spacer()
        }
    }
}