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
import com.varabyte.kobweb.silk.components.navigation.LinkStyle
import com.varabyte.kobweb.silk.components.navigation.UndecoratedLinkVariant
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.components.text.SpanText
import io.github.opletter.courseevals.site.core.components.layouts.HomePageLayout
import io.github.opletter.courseevals.site.core.components.widgets.LogoWithSubhead
import io.github.opletter.courseevals.site.core.misc.College
import kotlinx.browser.document
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.px

val MyLinkVariant by LinkStyle.addVariant(
    extraModifiers = { LinkStyle.toModifier(UndecoratedLinkVariant) }
) {
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
                    Modifier
                        .margin(topBottom = 0.5.cssRem)
                        .textAlign(TextAlign.Center)
                        .fontSize(1.5.cssRem)
                        .fontWeight(FontWeight.Bold)
                        .backgroundColor(Colors.Black.copyf(alpha = 0.5f))
                        .borderRadius(12.px)
                        .padding(1.5.cssRem)
                        .rowGap(1.cssRem),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    listOf(College.FSU, College.USF).forEach { college ->
                        Link(path = college.urlPath, variant = MyLinkVariant) {
                            SpanText(college.fullName)
                        }
                    }
                }
            }
            Spacer()
        }
    }
}