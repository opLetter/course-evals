package io.github.opletter.courseevals.site.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.thenIf
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.components.navigation.LinkStyle
import com.varabyte.kobweb.silk.components.navigation.UndecoratedLinkVariant
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.components.text.SpanText
import io.github.opletter.courseevals.site.core.components.sections.Footer
import io.github.opletter.courseevals.site.core.components.sections.SubHeadVariant
import io.github.opletter.courseevals.site.core.components.sections.dataPage.MainNavStyle
import io.github.opletter.courseevals.site.core.components.sections.dataPage.QuestionHeader
import io.github.opletter.courseevals.site.core.components.sections.dataPage.SpanTextHeaderVariant
import io.github.opletter.courseevals.site.core.components.widgets.BarGraph
import io.github.opletter.courseevals.site.core.components.widgets.Logo
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.s

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
    val ctx = rememberPageContext()
    var routing by remember { mutableStateOf(false) }

    LaunchedEffect(window.location.href) {
        // See kobweb config in build.gradle.kts which sets up highlight.js
        js("hljs.highlightAll()")
    }

    remember {
        document.title = "EVALS"
    }

    Box(
        MainNavStyle.toModifier()
            .fillMaxWidth()
            .minHeight(100.percent)
            .padding(top = 0.5.cssRem)
            .gridTemplateRows("1fr auto")
            .transition(CSSTransition("opacity", 0.25.s, TransitionTimingFunction.EaseInOut))
            .thenIf(routing, Modifier.opacity(0))
            .onTransitionEnd {
                if (routing) ctx.router.tryRoutingTo("/")
            }
    ) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Logo()
            SpanText(
                text = "View course evaluation results in an easy-to-read format.",
                variant = SubHeadVariant
            )

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
                    Link(path = "fsu", variant = MyLinkVariant) {
                        SpanText("Florida State University", Modifier.classNames("hello").id("hello"))
                    }
                    Link(path = "usf", variant = MyLinkVariant) {
                        SpanText("University of South Florida")
                    }
                }
            }

            Spacer()

//            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
//                SpanText(
//                    "\"Quality of this website\"",
//                    Modifier.margin(topBottom = 0.5.cssRem),
//                    variant = SpanTextHeaderVariant,
//                )
//                BarGraph(
//                    ratings = listOf(0, 0, 1, 4, 15),
//                    label = "Poor -> Excellent",
//                    Modifier.width(33.percent)
//                        .background(CSSBackground(color = Colors.Black.copyf(alpha = 0.5f)))
//                )
//            }
        }
        // Associate the footer with the row that will get pushed off the bottom of the page if it can't fit.
        Footer(
            Modifier
                .margin(topBottom = 1.cssRem)
                .align(Alignment.Center)
                .gridRowStart(2)
                .gridRowEnd(3)
        )
    }
}